import sys
import re
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests


# ============================================================
# 配置
# ============================================================

WORKER_URL = "https://ip-jc.mofa.kdns.fr"

MAX_WORKERS = 16

TIMEOUT = 30


# ============================================================
# 国家名称 → 国家代码
#
# Worker 可能返回：
#
# Singapore
# Japan
# United States
# India
#
# 也兼容：
#
# SG
# JP
# US
# IN
# ============================================================

COUNTRY_MAP = {

    "HONG KONG": "HK",
    "TAIWAN": "TW",
    "SINGAPORE": "SG",
    "JAPAN": "JP",
    "UNITED STATES": "US",
    "INDIA": "IN",

    "HK": "HK",
    "TW": "TW",
    "SG": "SG",
    "JP": "JP",
    "US": "US",
    "IN": "IN",
}


# ============================================================
# 国家代码 → 国家名称
# ============================================================

COUNTRY_NAME = {

    "HK": "Hong Kong",
    "TW": "Taiwan",
    "SG": "Singapore",
    "JP": "Japan",
    "US": "United States",
    "IN": "India",
}


# ============================================================
# 固定国家排序
#
# HK → TW → SG → JP → US → IN
# ============================================================

COUNTRY_ORDER = {

    "HK": 0,
    "TW": 1,
    "SG": 2,
    "JP": 3,
    "US": 4,
    "IN": 5,
}


# ============================================================
# 从文件中提取 IP:端口
#
# 支持：
#
# 18.138.57.221:443
#
# 18.138.57.221:443#SG Singapore AS16509 Amazon.com
#
# 自动忽略 # 后面的内容
# ============================================================

def load_targets(filename):

    targets = []

    seen = set()


    try:

        with open(
            filename,
            "r",
            encoding="utf-8"
        ) as f:

            for line in f:

                line = line.strip()


                if not line:
                    continue


                # ------------------------------------------------
                # 去掉 # 后面的备注
                # ------------------------------------------------

                target = (
                    line
                    .split("#", 1)[0]
                    .strip()
                )


                if not target:
                    continue


                # ------------------------------------------------
                # 只接受：
                #
                # IPv4:端口
                # ------------------------------------------------

                if not is_valid_target(target):

                    print(
                        f"[跳过] {filename} "
                        f"无效目标：{target}"
                    )

                    continue


                # ------------------------------------------------
                # 去重
                # ------------------------------------------------

                if target in seen:
                    continue


                seen.add(target)

                targets.append(target)


    except FileNotFoundError:

        print(
            f"[提示] 文件不存在：{filename}"
        )

        return []


    return targets


# ============================================================
# 验证 IP:端口
# ============================================================

def is_valid_target(target):

    match = re.fullmatch(
        r"(\d{1,3}(?:\.\d{1,3}){3}):(\d{1,5})",
        target
    )


    if not match:
        return False


    ip = match.group(1)

    port = int(match.group(2))


    # --------------------------------------------------------
    # IP
    # --------------------------------------------------------

    parts = ip.split(".")


    if len(parts) != 4:
        return False


    for part in parts:

        number = int(part)

        if number < 0 or number > 255:
            return False


    # --------------------------------------------------------
    # 端口
    # --------------------------------------------------------

    if port < 1 or port > 65535:
        return False


    return True


# ============================================================
# 合并两个文件
#
# 三国.txt
# +
# 已筛.txt
#
# 自动去重
# ============================================================

def merge_targets(
    sanguo_file,
    filtered_file
):

    print("")
    print("============================================================")
    print("合并检测列表")
    print("============================================================")


    sanguo_targets =
        load_targets(sanguo_file)


    filtered_targets =
        load_targets(filtered_file)


    print(
        f"三国.txt：{len(sanguo_targets)} 个"
    )

    print(
        f"已筛.txt：{len(filtered_targets)} 个"
    )


    # --------------------------------------------------------
    # 三国.txt 放前面
    # 已筛.txt 补充进去
    #
    # 如果相同 IP:端口：
    # 只保留一个
    # --------------------------------------------------------

    targets = []

    seen = set()


    for target in (
        sanguo_targets +
        filtered_targets
    ):

        if target in seen:
            continue

        seen.add(target)

        targets.append(target)


    print(
        f"合并后：{len(targets)} 个"
    )

    print(
        f"重复去除："
        f"{len(sanguo_targets) + len(filtered_targets) - len(targets)} 个"
    )


    return targets


# ============================================================
# 检测单个 IP:端口
# ============================================================

def check_proxy(target):

    url =
        f"{WORKER_URL}/check"


    try:

        response = requests.get(

            url,

            params={
                "proxyip": target
            },

            timeout=TIMEOUT,
        )


        response.raise_for_status()


        data =
            response.json()


    except Exception as e:

        print(
            f"[失败] {target} "
            f"请求错误：{e}"
        )

        return None


    # ========================================================
    # success
    # ========================================================

    if data.get("success") is not True:

        print(
            f"[失败] {target} "
            f"success=false"
        )

        return None


    # ========================================================
    # responseTime
    # ========================================================

    response_time =
        data.get("responseTime")


    if response_time is None:

        print(
            f"[失败] {target} "
            f"没有 responseTime"
        )

        return None


    try:

        response_time =
            int(response_time)

    except Exception:

        print(
            f"[失败] {target} "
            f"responseTime 无效"
        )

        return None


    # ========================================================
    # probe_results
    #
    # Worker 当前结构：
    #
    # probe_results
    #   ├── ipv4
    #   │     └── exit
    #   │           ├── ip
    #   │           ├── country
    #   │           ├── asn
    #   │           └── asOrganization
    #   │
    #   └── ipv6
    #         └── exit
    # ========================================================

    probe_results =
        data.get("probe_results") or {}


    ipv4 =
        probe_results.get("ipv4") or {}


    ipv6 =
        probe_results.get("ipv6") or {}


    # --------------------------------------------------------
    # 优先 IPv4
    # 没有 IPv4 再使用 IPv6
    # --------------------------------------------------------

    exit_info =
        ipv4.get("exit") or \
        ipv6.get("exit") or {}


    if not isinstance(
        exit_info,
        dict
    ):

        print(
            f"[失败] {target} "
            f"没有有效 exit 信息"
        )

        return None


    # ========================================================
    # 出口 IP
    # ========================================================

    exit_ip =
        str(
            exit_info.get("ip") or ""
        ).strip()


    if not exit_ip:

        print(
            f"[失败] {target} "
            f"没有出口 IP"
        )

        return None


    # ========================================================
    # 国家
    # ========================================================

    country_raw =
        str(
            exit_info.get("country") or ""
        ).strip()


    if not country_raw:

        print(
            f"[失败] {target} "
            f"没有国家"
        )

        return None


    country_code =
        COUNTRY_MAP.get(
            country_raw.upper(),
            country_raw.upper()
        )


    # ========================================================
    # 只允许六个地区
    #
    # HK
    # TW
    # SG
    # JP
    # US
    # IN
    # ========================================================

    if (
        country_code
        not in COUNTRY_ORDER
    ):

        print(
            f"[跳过] {target} "
            f"国家不在指定范围："
            f"{country_raw}"
        )

        return None


    country_name =
        COUNTRY_NAME[
            country_code
        ]


    # ========================================================
    # ASN
    # ========================================================

    asn_value =
        exit_info.get("asn") or ""


    if isinstance(
        asn_value,
        int
    ):

        asn =
            f"AS{asn_value}"


    else:

        asn =
            str(asn_value).strip()


        if not asn:

            print(
                f"[失败] {target} "
                f"没有 ASN"
            )

            return None


        if not asn.upper().startswith(
            "AS"
        ):

            asn =
                f"AS{asn}"


    # ========================================================
    # 提取 ASN 数字
    # ========================================================

    asn_match =
        re.search(
            r"AS(\d+)",
            asn,
            re.IGNORECASE
        )


    if not asn_match:

        print(
            f"[失败] {target} "
            f"ASN 无效：{asn}"
        )

        return None


    asn =
        f"AS{asn_match.group(1)}"


    # ========================================================
    # 运营商
    # ========================================================

    organization =
        str(
            exit_info.get(
                "asOrganization"
            ) or ""
        ).strip()


    if not organization:

        organization =
            "Unknown"


    # ========================================================
    # 最终格式
    #
    # 18.141.208.166:443#SG Singapore AS16509 Amazon.com, Inc.
    # ========================================================

    result = (

        f"{target}"

        f"#{country_code} "

        f"{country_name} "

        f"{asn} "

        f"{organization}"
    )


    print(

        f"[成功] {target} → "

        f"{country_code} "

        f"{country_name} "

        f"{asn} "

        f"{organization} "

        f"{response_time}ms"
    )


    return result


# ============================================================
# IP 数字排序
# ============================================================

def ip_sort_key(ip):

    try:

        return tuple(
            int(x)
            for x in ip.split(".")
        )

    except Exception:

        return (
            999,
            999,
            999,
            999
        )


# ============================================================
# 最终排序
#
# 国家：
# HK → TW → SG → JP → US → IN
#
# 同国家：
# ASN 从小到大
#
# 同 ASN：
# IP 从小到大
# ============================================================

def sort_key(item):

    try:

        ip_port, info =
            item.split(
                "#",
                1
            )


        ip =
            ip_port.rsplit(
                ":",
                1
            )[0]


        country =
            info.split()[0].upper()


        asn_match =
            re.search(
                r"\bAS(\d+)\b",
                info,
                re.IGNORECASE
            )


        if asn_match:

            asn_number =
                int(
                    asn_match.group(1)
                )

        else:

            asn_number =
                999999999


        return (

            COUNTRY_ORDER.get(
                country,
                999
            ),

            asn_number,

            ip_sort_key(ip)
        )


    except Exception:

        return (

            999,

            999999999,

            (
                999,
                999,
                999,
                999
            )
        )


# ============================================================
# 主程序
# ============================================================

def main():

    # --------------------------------------------------------
    # 用法
    #
    # python scripts/check_proxy.py
    # 三国.txt
    # 已筛.txt
    # --------------------------------------------------------

    if len(sys.argv) != 3:

        print(

            "用法：\n"

            "python scripts/check_proxy.py "
            "三国.txt 已筛.txt"
        )

        sys.exit(1)


    input_file =
        sys.argv[1]


    output_file =
        sys.argv[2]


    # ========================================================
    # 合并 三国.txt + 已筛.txt
    # ========================================================

    targets =
        merge_targets(
            input_file,
            output_file
        )


    print("")
    print("============================================================")
    print("开始重新检测")
    print("============================================================")

    print(
        f"最终待检测：{len(targets)}"
    )

    print(
        f"Worker：{WORKER_URL}"
    )

    print(
        f"并发：{MAX_WORKERS}"
    )


    # ========================================================
    # 两边都没有数据
    # ========================================================

    if not targets:

        print(
            "错误：三国.txt 和 已筛.txt 都没有有效 IP:端口"
        )

        sys.exit(1)


    results = []


    # ========================================================
    # 并发检测
    # ========================================================

    with ThreadPoolExecutor(
        max_workers=MAX_WORKERS
    ) as executor:


        future_map = {

            executor.submit(
                check_proxy,
                target
            ): target

            for target in targets
        }


        for future in as_completed(
            future_map
        ):

            target =
                future_map[future]


            try:

                result =
                    future.result()


                if result:

                    results.append(
                        result
                    )


            except Exception as e:

                print(

                    f"[失败] {target} "

                    f"检测异常：{e}"
                )


    # ========================================================
    # 检测完成
    # ========================================================

    print("")
    print("============================================================")
    print("检测完成")
    print("============================================================")


    print(
        f"合并待检测：{len(targets)}"
    )


    print(
        f"检测成功：{len(results)}"
    )


    print(
        f"检测失败："
        f"{len(targets) - len(results)}"
    )


    # ========================================================
    # 极重要保护
    #
    # 如果本次一个成功的都没有：
    #
    # 不覆盖原来的 已筛.txt
    #
    # 防止 Worker 临时故障导致：
    #
    # 已筛.txt 被清空
    # ========================================================

    if len(results) == 0:

        print("")
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        print("本次检测成功数量为 0")
        print("为了防止数据丢失，不覆盖原来的已筛.txt")
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        sys.exit(1)


    # ========================================================
    # 排序
    # ========================================================

    results.sort(
        key=sort_key
    )


    # ========================================================
    # 再次去重
    #
    # 理论上前面已经去重
    # 这里按照最终 target 再保险一次
    # ========================================================

    final_results = []

    result_seen = set()


    for item in results:

        target =
            item.split(
                "#",
                1
            )[0].strip()


        if target in result_seen:

            continue


        result_seen.add(
            target
        )


        final_results.append(
            item
        )


    results =
        final_results


    # ========================================================
    # 写入临时文件
    #
    # 先写 .tmp
    # 成功后再替换
    #
    # 防止写文件过程中异常造成已筛损坏
    # ========================================================

    temp_file =
        f"{output_file}.tmp"


    with open(
        temp_file,
        "w",
        encoding="utf-8"
    ) as f:


        for item in results:

            f.write(
                item + "\n"
            )


    # ========================================================
    # 替换正式文件
    # ========================================================

    import os


    os.replace(
        temp_file,
        output_file
    )


    # ========================================================
    # 最终统计
    # ========================================================

    print("")
    print("============================================================")
    print("已筛.txt 更新完成")
    print("============================================================")


    print(
        f"最终数量：{len(results)}"
    )


    print(
        f"输出文件：{output_file}"
    )


if __name__ == "__main__":

    main()