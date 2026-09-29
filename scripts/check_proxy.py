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
# 国家名称 → 国家代码
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
# 读取输入
# ============================================================

def read_input_file(path):
    """
    支持：

    18.138.57.221:443

    18.138.57.221:443#备注

    18.138.57.221
    """

    result = []

    try:
        with open(path, "r", encoding="utf-8") as f:
            lines = f.readlines()
    except Exception as e:
        print(f"读取文件失败：{e}")
        return result

    for line in lines:

        line = line.strip()

        if not line:
            continue

        # 去掉旧备注
        if "#" in line:
            line = line.split("#", 1)[0].strip()

        # IP:PORT
        match = re.match(
            r"^(\d{1,3}(?:\.\d{1,3}){3}):(\d+)$",
            line
        )

        if match:
            ip = match.group(1)
            port = match.group(2)

        else:
            # 只有 IP，默认 443
            match = re.match(
                r"^(\d{1,3}(?:\.\d{1,3}){3})$",
                line
            )

            if not match:
                continue

            ip = match.group(1)
            port = "443"

        # 检查 IPv4
        try:
            ipaddress.ip_address(ip)
        except Exception:
            continue

        result.append(f"{ip}:{port}")

    # 去重
    result = list(dict.fromkeys(result))

    return result


# ============================================================
# 国家代码
# ============================================================

def normalize_country(value):
    if not value:
        return ""

    value = str(value).strip().upper()

    return COUNTRY_MAP.get(value, "")


# ============================================================
# ASN 数字
# ============================================================

def asn_number(value):
    if not value:
        return 999999999999

    value = str(value).strip().upper()

    match = re.search(r"AS(\d+)", value)

    if match:
        try:
            return int(match.group(1))
        except Exception:
            pass

    return 999999999999


# ============================================================
# 检测单个 IP
# ============================================================

def check_proxy(address):

    url = WORKER_URL.rstrip("/") + "/check"

    params = {
        "ip": address
    }

    try:

        response = requests.get(
            url,
            params=params,
            timeout=TIMEOUT
        )

        if response.status_code != 200:
            print(
                f"[失败] {address} HTTP {response.status_code}"
            )
            return None

        try:
            data = response.json()
        except Exception:
            print(f"[失败] {address} 返回不是 JSON")
            return None

        # ----------------------------------------------------
        # success 必须为 true
        # ----------------------------------------------------

        if data.get("success") is not True:
            print(f"[失败] {address} success=false")
            return None

        # ----------------------------------------------------
        # 必须有 responseTime
        # ----------------------------------------------------

        response_time = data.get("responseTime")

        if response_time is None:
            print(f"[失败] {address} 没有 responseTime")
            return None

        # ----------------------------------------------------
        # 读取 probe_results
        # ----------------------------------------------------

        probe_results = data.get("probe_results") or {}

        ipv4 = probe_results.get("ipv4") or {}
        ipv6 = probe_results.get("ipv6") or {}

        # ----------------------------------------------------
        # 优先 IPv4
        # ----------------------------------------------------

        exit_info = None

        if isinstance(ipv4, dict):
            exit_info = ipv4.get("exit")

        if not isinstance(exit_info, dict):

            if isinstance(ipv6, dict):
                exit_info = ipv6.get("exit")

        if not isinstance(exit_info, dict):
            print(f"[失败] {address} 没有出口信息")
            return None

        # ----------------------------------------------------
        # 出口 IP
        # ----------------------------------------------------

        exit_ip = str(
            exit_info.get("ip") or ""
        ).strip()

        if not exit_ip:
            print(f"[失败] {address} 没有出口 IP")
            return None

        # ----------------------------------------------------
        # 国家
        # ----------------------------------------------------

        country_raw = str(
            exit_info.get("country") or ""
        ).strip()

        country = normalize_country(country_raw)

        if not country:
            print(
                f"[失败] {address} 没有国家"
            )
            return None

        # ----------------------------------------------------
        # 只保留指定国家
        #
        # HK → TW → SG → JP → US → IN
        # ----------------------------------------------------

        if country not in COUNTRY_ORDER:
            print(
                f"[跳过] {address} 国家={country_raw}"
            )
            return None

        # ----------------------------------------------------
        # 城市
        # ----------------------------------------------------

        city = str(
            exit_info.get("city") or ""
        ).strip()

        # ----------------------------------------------------
        # ASN
        # ----------------------------------------------------

        asn = str(
            exit_info.get("asn") or ""
        ).strip()

        if asn and not asn.upper().startswith("AS"):
            asn = "AS" + asn

        if not asn:
            print(
                f"[失败] {address} 没有 ASN"
            )
            return None

        # ----------------------------------------------------
        # 运营商
        # ----------------------------------------------------

        organization = str(
            exit_info.get("asOrganization") or ""
        ).strip()

        if not organization:
            organization = "Unknown"

        # ----------------------------------------------------
        # 端口
        # ----------------------------------------------------

        if ":" in address:
            port = address.rsplit(":", 1)[1]
        else:
            port = "443"

        # ----------------------------------------------------
        # 最终格式
        #
        # IP:PORT#国家 城市 AS ASN 运营商
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

        remark = " ".join(name_parts)

        output = (
            f"{exit_ip}:{port}#{remark}"
        )

        print(
            f"[成功] {address} → {output}"
        )

        return {
            "address": address,
            "ip": exit_ip,
            "port": int(port),
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
            f"[失败] {address} 请求超时"
        )

        return None

    except Exception as e:

        print(
            f"[失败] {address} {e}"
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
        return (999, 999, 999, 999)


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
# 写入输出
# ============================================================

def write_output(path, results):

    results = sort_results(results)

    with open(
        path,
        "w",
        encoding="utf-8"
    ) as f:

        for item in results:

            f.write(
                item["output"] + "\n"
            )


# ============================================================
# 主程序
# ============================================================

def main():

    if len(sys.argv) < 3:

        print(
            "用法：python check_proxy.py 输入文件 输出文件"
        )

        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    print("=" * 60)
    print("Cloudflare Worker IP 检测")
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

    # --------------------------------------------------------
    # 读取
    # --------------------------------------------------------

    addresses = read_input_file(
        input_file
    )

    print(
        f"待检测：{len(addresses)}"
    )

    if not addresses:

        print(
            "没有可检测的 IP"
        )

        # 不要清空已有输出
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
                address
            ): address
            for address in addresses
        }

        for future in as_completed(
            future_map
        ):

            try:

                result = future.result()

                if result is not None:
                    results.append(result)

            except Exception as e:

                address = future_map[future]

                print(
                    f"[失败] {address} {e}"
                )

    # --------------------------------------------------------
    # 统计
    # --------------------------------------------------------

    success_count = len(results)

    failed_count = (
        len(addresses)
        - success_count
    )

    print("=" * 60)

    print(
        f"原始数量：{len(addresses)}"
    )

    print(
        f"成功数量：{success_count}"
    )

    print(
        f"失败数量：{failed_count}"
    )

    # --------------------------------------------------------
    # 重要：
    #
    # 如果全部失败，不覆盖原来的 已筛.txt
    #
    # 防止 Worker 临时异常导致已筛.txt 被清空。
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

    # --------------------------------------------------------
    # 显示前几条
    # --------------------------------------------------------

    print("结果示例：")

    for item in results[:10]:

        print(
            item["output"]
        )

    print("=" * 60)


if __name__ == "__main__":
    main()