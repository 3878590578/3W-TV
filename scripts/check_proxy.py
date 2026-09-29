import sys
import re
import ipaddress
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed


# ============================================================
# 配置
# ============================================================

WORKER_URL = "https://ip-jc.mofa.kdns.fr"

MAX_WORKERS = 16

TIMEOUT = 30


# ============================================================
# 国家排序
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
# 国家名称转换
# ============================================================

COUNTRY_MAP = {
    "HONG KONG": "HK",
    "HONGKONG": "HK",
    "HK": "HK",

    "TAIWAN": "TW",
    "TW": "TW",

    "SINGAPORE": "SG",
    "SG": "SG",

    "JAPAN": "JP",
    "JP": "JP",

    "UNITED STATES": "US",
    "UNITED STATES OF AMERICA": "US",
    "USA": "US",
    "US": "US",

    "INDIA": "IN",
    "IN": "IN",
}


# ============================================================
# 从一行中提取 IPv4
#
# 支持：
#
# 47.131.189.221
#
# 47.131.189.221:443
#
# 47.131.189.221:443#SG Singapore AS16509 Amazon.com, Inc.
# ============================================================

def extract_ip(line):

    if not line:
        return None

    line = line.strip()

    if not line:
        return None

    # 去掉 # 后面的备注
    if "#" in line:
        line = line.split("#", 1)[0].strip()

    # 如果有端口，只取 IP
    if ":" in line:
        line = line.split(":", 1)[0].strip()

    # 严格 IPv4
    match = re.fullmatch(
        r"\d{1,3}(?:\.\d{1,3}){3}",
        line
    )

    if not match:
        return None

    try:

        ip_obj = ipaddress.ip_address(line)

        if ip_obj.version != 4:
            return None

    except Exception:

        return None

    return line


# ============================================================
# 从文件读取 IP
#
# 三国.txt：
# 纯 IP
#
# 已筛.txt：
# IP:端口#备注
#
# 最终全部只提取 IP
# ============================================================

def read_ips(path):

    result = []

    try:

        with open(
            path,
            "r",
            encoding="utf-8"
        ) as f:

            lines = f.readlines()

    except FileNotFoundError:

        print(
            f"[提示] 文件不存在：{path}"
        )

        return result

    except Exception as e:

        print(
            f"[错误] 读取 {path} 失败：{e}"
        )

        return result

    for line in lines:

        ip = extract_ip(line)

        if ip:

            result.append(ip)

    # 去重，同时保持原始顺序
    result = list(
        dict.fromkeys(result)
    )

    return result


# ============================================================
# 合并两个 IP 来源
#
# 三国.txt
# +
# 已筛.txt
#
# 去重
# ============================================================

def merge_ips(
    source_file,
    old_filtered_file
):

    source_ips = read_ips(
        source_file
    )

    old_ips = read_ips(
        old_filtered_file
    )

    # 三国优先，然后加入旧已筛
    merged = list(
        dict.fromkeys(
            source_ips + old_ips
        )
    )

    print(
        f"三国.txt IP：{len(source_ips)}"
    )

    print(
        f"旧已筛.txt IP：{len(old_ips)}"
    )

    print(
        f"合并去重后：{len(merged)}"
    )

    return merged


# ============================================================
# 国家标准化
# ============================================================

def normalize_country(value):

    if not value:
        return ""

    value = str(
        value
    ).strip().upper()

    return COUNTRY_MAP.get(
        value,
        ""
    )


# ============================================================
# ASN 数字
# ============================================================

def asn_number(value):

    if not value:
        return 999999999999

    value = str(
        value
    ).strip().upper()

    match = re.search(
        r"AS(\d+)",
        value
    )

    if match:

        try:
            return int(
                match.group(1)
            )
        except Exception:
            pass

    return 999999999999


# ============================================================
# 检测单个 IP
#
# 注意：
#
# 输入只有 IP
#
# 端口完全使用 Worker 返回的 portRemote
# 不自行默认 443
# ============================================================

def check_proxy(ip):

    url = (
        WORKER_URL.rstrip("/")
        + "/check"
    )

    params = {
        "ip": ip
    }

    try:

        response = requests.get(
            url,
            params=params,
            timeout=TIMEOUT
        )

        # ----------------------------------------------------
        # HTTP 状态
        # ----------------------------------------------------

        if response.status_code != 200:

            print(
                f"[失败] {ip} HTTP {response.status_code}"
            )

            return None

        # ----------------------------------------------------
        # JSON
        # ----------------------------------------------------

        try:

            data = response.json()

        except Exception:

            print(
                f"[失败] {ip} 返回不是 JSON"
            )

            return None

        # ----------------------------------------------------
        # success
        # ----------------------------------------------------

        if data.get("success") is not True:

            print(
                f"[失败] {ip} success=false"
            )

            return None

        # ----------------------------------------------------
        # responseTime
        # ----------------------------------------------------

        response_time = data.get(
            "responseTime"
        )

        if response_time is None:

            print(
                f"[失败] {ip} 没有 responseTime"
            )

            return None

        # ----------------------------------------------------
        # Worker 返回的远端端口
        # ----------------------------------------------------

        port_remote = data.get(
            "portRemote"
        )

        if port_remote is None:

            print(
                f"[失败] {ip} 没有 portRemote"
            )

            return None

        try:

            port_remote = int(
                port_remote
            )

        except Exception:

            print(
                f"[失败] {ip} portRemote 无效：{port_remote}"
            )

            return None

        if not (
            1 <= port_remote <= 65535
        ):

            print(
                f"[失败] {ip} portRemote 超出范围：{port_remote}"
            )

            return None

        # ----------------------------------------------------
        # probe_results
        # ----------------------------------------------------

        probe_results = (
            data.get(
                "probe_results"
            )
            or {}
        )

        ipv4 = (
            probe_results.get(
                "ipv4"
            )
            or {}
        )

        ipv6 = (
            probe_results.get(
                "ipv6"
            )
            or {}
        )

        # ----------------------------------------------------
        # 优先 IPv4 exit
        # ----------------------------------------------------

        exit_info = None

        if isinstance(
            ipv4,
            dict
        ):

            candidate = ipv4.get(
                "exit"
            )

            if isinstance(
                candidate,
                dict
            ):

                exit_info = candidate

        # ----------------------------------------------------
        # 没有 IPv4 时使用 IPv6
        # ----------------------------------------------------

        if exit_info is None:

            if isinstance(
                ipv6,
                dict
            ):

                candidate = ipv6.get(
                    "exit"
                )

                if isinstance(
                    candidate,
                    dict
                ):

                    exit_info = candidate

        if not isinstance(
            exit_info,
            dict
        ):

            print(
                f"[失败] {ip} 没有出口信息"
            )

            return None

        # ----------------------------------------------------
        # 出口 IP
        # ----------------------------------------------------

        exit_ip = str(
            exit_info.get(
                "ip"
            )
            or ""
        ).strip()

        if not exit_ip:

            print(
                f"[失败] {ip} 没有出口 IP"
            )

            return None

        # ----------------------------------------------------
        # 国家
        # ----------------------------------------------------

        country_raw = str(
            exit_info.get(
                "country"
            )
            or ""
        ).strip()

        country = normalize_country(
            country_raw
        )

        if not country:

            print(
                f"[失败] {ip} 没有国家"
            )

            return None

        # ----------------------------------------------------
        # 只保留指定国家
        # ----------------------------------------------------

        if country not in COUNTRY_ORDER:

            print(
                f"[跳过] {ip} 国家={country_raw}"
            )

            return None

        # ----------------------------------------------------
        # 城市
        # ----------------------------------------------------

        city = str(
            exit_info.get(
                "city"
            )
            or ""
        ).strip()

        # ----------------------------------------------------
        # ASN
        # ----------------------------------------------------

        asn = str(
            exit_info.get(
                "asn"
            )
            or ""
        ).strip()

        if asn:

            if not asn.upper().startswith("AS"):

                asn = "AS" + asn

        if not asn:

            print(
                f"[失败] {ip} 没有 ASN"
            )

            return None

        # ----------------------------------------------------
        # 运营商
        # ----------------------------------------------------

        organization = str(
            exit_info.get(
                "asOrganization"
            )
            or ""
        ).strip()

        if not organization:

            organization = "Unknown"

        # ----------------------------------------------------
        # 最终格式
        #
        # IP:Worker返回的port#国家 城市 ASN 运营商
        #
        # 例如：
        #
        # 47.131.189.221:443#SG Singapore AS16509 Amazon.com, Inc.
        # ----------------------------------------------------

        name_parts = [
            country,
            city,
            asn,
            organization
        ]

        name_parts = [
            str(x).strip()
            for x in name_parts
            if str(x).strip()
        ]

        remark = " ".join(
            name_parts
        )

        output = (
            f"{ip}:{port_remote}#{remark}"
        )

        print(
            f"[成功] {ip} → {output}"
        )

        return {
            "ip": ip,
            "port": port_remote,
            "country": country,
            "city": city,
            "asn": asn,
            "asn_num": asn_number(asn),
            "organization": organization,
            "response_time": response_time,
            "output": output,
        }

    except requests.exceptions.Timeout:

        print(
            f"[失败] {ip} 请求超时"
        )

        return None

    except Exception as e:

        print(
            f"[失败] {ip} {e}"
        )

        return None


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
# 排序
#
# 1. 国家
# 2. ASN
# 3. IP
# ============================================================

def sort_results(results):

    return sorted(

        results,

        key=lambda x: (

            COUNTRY_ORDER.get(
                x["country"],
                999
            ),

            x["asn_num"],

            ip_sort_key(
                x["ip"]
            )
        )
    )


# ============================================================
# 写入已筛.txt
# ============================================================

def write_output(
    path,
    results
):

    results = sort_results(
        results
    )

    with open(
        path,
        "w",
        encoding="utf-8"
    ) as f:

        for item in results:

            f.write(
                item["output"]
                + "\n"
            )


# ============================================================
# 主程序
#
# 支持两种用法
#
# 第一工作流：
#
# python check_proxy.py 三国.txt 已筛.txt 已筛.txt
#
# 含义：
#
# 三国.txt
# +
# 旧已筛.txt
# ↓
# 合并检测
# ↓
# 新已筛.txt
#
#
# 第二工作流：
#
# python check_proxy.py 已筛.txt 已筛.txt
#
# 含义：
#
# 已筛.txt
# ↓
# 全部重新检测
# ↓
# 新已筛.txt
# ============================================================

def main():

    if len(sys.argv) == 4:

        # ----------------------------------------------------
        # 三个参数
        #
        # source
        # old filtered
        # output
        # ----------------------------------------------------

        source_file = sys.argv[1]

        old_filtered_file = sys.argv[2]

        output_file = sys.argv[3]

        print("=" * 60)

        print(
            "模式：三国.txt + 已筛.txt 合并重新检测"
        )

        print("=" * 60)

        print(
            f"来源文件：{source_file}"
        )

        print(
            f"旧筛选文件：{old_filtered_file}"
        )

        print(
            f"输出文件：{output_file}"
        )

        print(
            f"Worker：{WORKER_URL}"
        )

        print(
            f"并发：{MAX_WORKERS}"
        )

        print("=" * 60)

        addresses = merge_ips(
            source_file,
            old_filtered_file
        )

    elif len(sys.argv) == 3:

        # ----------------------------------------------------
        # 两个参数
        #
        # input
        # output
        # ----------------------------------------------------

        input_file = sys.argv[1]

        output_file = sys.argv[2]

        print("=" * 60)

        print(
            "模式：已筛.txt 重新检测"
        )

        print("=" * 60)

        print(
            f"输入文件：{input_file}"
        )

        print(
            f"输出文件：{output_file}"
        )

        print(
            f"Worker：{WORKER_URL}"
        )

        print(
            f"并发：{MAX_WORKERS}"
        )

        print("=" * 60)

        addresses = read_ips(
            input_file
        )

        print(
            f"读取 IP：{len(addresses)}"
        )

    else:

        print(
            "用法："
        )

        print(
            "第一工作流："
        )

        print(
            "python scripts/check_proxy.py 三国.txt 已筛.txt 已筛.txt"
        )

        print()

        print(
            "第二工作流："
        )

        print(
            "python scripts/check_proxy.py 已筛.txt 已筛.txt"
        )

        sys.exit(1)

    # --------------------------------------------------------
    # 没有 IP
    # --------------------------------------------------------

    if not addresses:

        print(
            "没有可检测的 IP"
        )

        print(
            "不覆盖原输出文件。"
        )

        sys.exit(0)

    # --------------------------------------------------------
    # 并发检测
    # --------------------------------------------------------

    results = []

    with ThreadPoolExecutor(
        max_workers=MAX_WORKERS
    ) as executor:

        future_map = {

            executor.submit(
                check_proxy,
                ip
            ): ip

            for ip in addresses
        }

        for future in as_completed(
            future_map
        ):

            ip = future_map[
                future
            ]

            try:

                result = future.result()

                if result is not None:

                    results.append(
                        result
                    )

            except Exception as e:

                print(
                    f"[失败] {ip} {e}"
                )

    # --------------------------------------------------------
    # 统计
    # --------------------------------------------------------

    success_count = len(
        results
    )

    failed_count = (
        len(addresses)
        - success_count
    )

    print("=" * 60)

    print(
        f"原始检测数量：{len(addresses)}"
    )

    print(
        f"成功数量：{success_count}"
    )

    print(
        f"失败数量：{failed_count}"
    )

    # --------------------------------------------------------
    # 全部失败保护
    #
    # 非常重要：
    #
    # 如果 Worker 临时异常，
    # 绝对不能把原来的已筛.txt清空。
    # --------------------------------------------------------

    if success_count == 0:

        print(
            "本次成功数量为 0"
        )

        print(
            "为了防止误清空，不覆盖原输出文件。"
        )

        sys.exit(0)

    # --------------------------------------------------------
    # 排序
    # --------------------------------------------------------

    results = sort_results(
        results
    )

    # --------------------------------------------------------
    # 写入
    # --------------------------------------------------------

    write_output(
        output_file,
        results
    )

    print(
        f"输出文件：{output_file}"
    )

    print("=" * 60)

    print(
        "结果示例："
    )

    for item in results[:10]:

        print(
            item["output"]
        )

    print("=" * 60)


if __name__ == "__main__":

    main()