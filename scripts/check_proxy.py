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


COUNTRY_NAME = {
    "HK": "Hong Kong",
    "TW": "Taiwan",
    "SG": "Singapore",
    "JP": "Japan",
    "US": "United States",
    "IN": "India",
}


# ============================================================
# 国家固定排序
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
# 读取 IP:端口
# 自动去除原有 # 后面的内容
# ============================================================

def load_targets(filename):

    targets = []
    seen = set()

    with open(
        filename,
        "r",
        encoding="utf-8"
    ) as f:

        for line in f:

            line = line.strip()

            if not line:
                continue

            # 去掉已有备注
            target = line.split("#", 1)[0].strip()

            if not target:
                continue

            if target in seen:
                continue

            seen.add(target)
            targets.append(target)

    return targets


# ============================================================
# 检测单个 IP:端口
# ============================================================

def check_proxy(target):

    url = f"{WORKER_URL}/check"

    try:

        response = requests.get(
            url,
            params={
                "proxyip": target
            },
            timeout=TIMEOUT
        )

        response.raise_for_status()

        data = response.json()

    except Exception as e:

        print(
            f"[失败] {target} 请求错误: {e}"
        )

        return None


    # ========================================================
    # 必须 success=true
    # ========================================================

    if data.get("success") is not True:

        print(
            f"[失败] {target} success=false"
        )

        return None


    # ========================================================
    # 必须存在 responseTime
    # ========================================================

    response_time = data.get(
        "responseTime"
    )

    if response_time is None:

        print(
            f"[失败] {target} 没有 responseTime"
        )

        return None


    try:

        response_time = int(
            response_time
        )

    except Exception:

        print(
            f"[失败] {target} responseTime 无效"
        )

        return None


    # ========================================================
    # Worker 正确返回结构：
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
    #
    # 优先 IPv4，没有则 IPv6
    # ========================================================

    probe_results = (
        data.get("probe_results")
        or {}
    )

    ipv4 = (
        probe_results.get("ipv4")
        or {}
    )

    ipv6 = (
        probe_results.get("ipv6")
        or {}
    )

    exit_info = (
        ipv4.get("exit")
        or ipv6.get("exit")
        or {}
    )


    if not isinstance(
        exit_info,
        dict
    ):

        print(
            f"[失败] {target} 没有有效 exit 信息"
        )

        return None


    # ========================================================
    # 出口 IP
    # ========================================================

    exit_ip = str(
        exit_info.get("ip") or ""
    ).strip()


    if not exit_ip:

        print(
            f"[失败] {target} 没有出口 IP"
        )

        return None


    # ========================================================
    # 国家
    # ========================================================

    country_raw = str(
        exit_info.get("country") or ""
    ).strip()


    if not country_raw:

        print(
            f"[失败] {target} 没有国家"
        )

        return None


    country_code = COUNTRY_MAP.get(
        country_raw.upper(),
        country_raw.upper()
    )


    # ========================================================
    # 只保留指定国家
    # ========================================================

    if country_code not in COUNTRY_ORDER:

        print(
            f"[跳过] {target} "
            f"国家不在指定范围：{country_raw}"
        )

        return None


    country_name = COUNTRY_NAME[
        country_code
    ]


    # ========================================================
    # ASN
    # ========================================================

    asn_value = (
        exit_info.get("asn")
        or ""
    )


    if isinstance(
        asn_value,
        int
    ):

        asn = f"AS{asn_value}"

    else:

        asn = str(
            asn_value
        ).strip()


        if not asn:

            print(
                f"[失败] {target} 没有 ASN"
            )

            return None


        if not asn.upper().startswith("AS"):

            asn = f"AS{asn}"


    # ========================================================
    # 提取 ASN 数字
    # ========================================================

    asn_match = re.search(
        r"AS(\d+)",
        asn,
        re.IGNORECASE
    )


    if not asn_match:

        print(
            f"[失败] {target} ASN 无效：{asn}"
        )

        return None


    asn = (
        f"AS{asn_match.group(1)}"
    )


    # ========================================================
    # 运营商
    # ========================================================

    organization = str(
        exit_info.get(
            "asOrganization"
        ) or ""
    ).strip()


    if not organization:

        organization = "Unknown"


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
# IP 排序
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

        ip_port, info = item.split(
            "#",
            1
        )

        ip = ip_port.rsplit(
            ":",
            1
        )[0]

        country = (
            info.split()[0]
            .upper()
        )


        asn_match = re.search(
            r"\bAS(\d+)\b",
            info,
            re.IGNORECASE
        )


        if asn_match:

            asn_number = int(
                asn_match.group(1)
            )

        else:

            asn_number = 999999999


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

    if len(sys.argv) != 3:

        print(
            "用法："
            "python scripts/check_proxy.py "
            "输入文件 输出文件"
        )

        sys.exit(1)


    input_file = sys.argv[1]
    output_file = sys.argv[2]


    # ========================================================
    # 读取目标
    # ========================================================

    targets = load_targets(
        input_file
    )


    print(
        f"待检测：{len(targets)}"
    )

    print(
        f"Worker：{WORKER_URL}"
    )

    print(
        f"并发：{MAX_WORKERS}"
    )


    if not targets:

        print(
            "没有需要检测的 IP"
        )

        sys.exit(0)


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

            target = future_map[
                future
            ]


            try:

                result = (
                    future.result()
                )


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
    # 排序
    # ========================================================

    results.sort(
        key=sort_key
    )


    # ========================================================
    # 写入输出文件
    # ========================================================

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as f:

        for item in results:

            f.write(
                item + "\n"
            )


    # ========================================================
    # 统计
    # ========================================================

    print("")

    print(
        f"原始数量：{len(targets)}"
    )

    print(
        f"成功数量：{len(results)}"
    )

    print(
        f"失败数量："
        f"{len(targets) - len(results)}"
    )

    print(
        f"输出文件：{output_file}"
    )


# ============================================================
# 执行
# ============================================================

if __name__ == "__main__":

    main()