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
# 国家固定排序
#
# HK 香港
# TW 台湾
# SG 新加坡
# JP 日本
# US 美国
# IN 印度
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
# 国家名称
# ============================================================

COUNTRY_NAME = {
    "HK": "Hong Kong",
    "TW": "Taiwan",
    "SG": "Singapore",
    "JP": "Japan",
    "US": "United States",
    "IN": "India",
    "KR": "South Korea",
    "GB": "United Kingdom",
    "DE": "Germany",
    "FR": "France",
    "CA": "Canada",
    "AU": "Australia",
    "NL": "Netherlands",
    "RU": "Russia",
    "BR": "Brazil",
}


# ============================================================
# 读取 IP:端口
# ============================================================

def read_targets(filename):
    targets = []

    with open(filename, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()

            if not line:
                continue

            # 去掉 # 后面的旧信息
            line = line.split("#", 1)[0].strip()

            # 基本检查
            if not re.match(
                r"^\d{1,3}(?:\.\d{1,3}){3}:\d+$",
                line
            ):
                continue

            if line not in targets:
                targets.append(line)

    return targets


# ============================================================
# 获取 ASN 数字
# ============================================================

def parse_asn(value):
    if value is None:
        return 999999999

    match = re.search(r"AS?(\d+)", str(value), re.IGNORECASE)

    if match:
        return int(match.group(1))

    return 999999999


# ============================================================
# 获取国家
# ============================================================

def get_country_code(data):
    country = (
        data.get("country")
        or data.get("countryCode")
        or data.get("country_code")
        or ""
    )

    return str(country).upper().strip()


# ============================================================
# 单个 IP 检测
# ============================================================

def check_proxy(target):
    url = f"{WORKER_URL.rstrip('/')}/check"

    try:
        response = requests.get(
            url,
            params={"proxyip": target},
            timeout=TIMEOUT
        )

        if response.status_code != 200:
            return None

        data = response.json()

        # 必须检测成功
        if data.get("success") is not True:
            return None

        # 必须有 responseTime
        response_time = data.get("responseTime")

        if response_time is None:
            return None

        try:
            response_time = int(response_time)
        except Exception:
            return None

        if response_time < 0:
            return None

        # ====================================================
        # 获取出口 IP 信息
        # ====================================================

        probe_results = data.get("probe_results") or {}

        ipv4 = probe_results.get("ipv4") or {}
        ipv6 = probe_results.get("ipv6") or {}

        exit_ip = (
            ipv4.get("exit")
            or ipv6.get("exit")
            or ""
        )

        if not exit_ip:
            return None

        # ====================================================
        # 国家
        # ====================================================

        country = get_country_code(data)

        if not country:
            country = str(
                ipv4.get("country")
                or ipv6.get("country")
                or ""
            ).upper().strip()

        if not country:
            return None

        country_name = COUNTRY_NAME.get(
            country,
            country
        )

        # ====================================================
        # ASN
        # ====================================================

        asn_value = (
            data.get("asn")
            or ipv4.get("asn")
            or ipv6.get("asn")
            or ""
        )

        asn_number = parse_asn(asn_value)

        if asn_number == 999999999:
            return None

        asn = f"AS{asn_number}"

        # ====================================================
        # ASN 组织
        # ====================================================

        organization = (
            data.get("asOrganization")
            or data.get("organization")
            or data.get("org")
            or ipv4.get("asOrganization")
            or ipv6.get("asOrganization")
            or ""
        )

        organization = str(organization).strip()

        if not organization:
            organization = "Unknown"

        # ====================================================
        # 保持原始 IP:端口
        # ====================================================

        result = (
            f"{target}#"
            f"{country} {country_name} "
            f"{asn} {organization}"
        )

        return result

    except Exception as e:
        print(f"[失败] {target} -> {e}")
        return None


# ============================================================
# 排序
#
# 1. HK
# 2. TW
# 3. SG
# 4. JP
# 5. US
# 6. IN
#
# 国家内部：
# ASN 数字升序
# IP 数字升序
# ============================================================

def sort_key(item):
    try:
        ip_port, info = item.split("#", 1)

        ip = ip_port.rsplit(":", 1)[0]

        # # 后面的第一个字段就是国家代码
        country = info.split()[0].upper()

        # 提取 ASN
        asn_match = re.search(
            r"\bAS(\d+)\b",
            info,
            re.IGNORECASE
        )

        if asn_match:
            asn = int(asn_match.group(1))
        else:
            asn = 999999999

        # IP 数字排序
        try:
            ip_numbers = tuple(
                int(x)
                for x in ip.split(".")
            )
        except Exception:
            ip_numbers = (999, 999, 999, 999)

        # 固定国家顺序
        country_order = COUNTRY_ORDER.get(
            country,
            999
        )

        return (
            country_order,
            asn,
            ip_numbers,
        )

    except Exception:
        return (
            999,
            999999999,
            (999, 999, 999, 999),
        )


# ============================================================
# 主程序
# ============================================================

def main():
    if len(sys.argv) != 3:
        print(
            "用法：python scripts/check_proxy.py "
            "输入文件 输出文件"
        )
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    print("=" * 60)
    print("Cloudflare Worker IP 检测")
    print("=" * 60)

    targets = read_targets(input_file)

    print(f"待检测数量：{len(targets)}")
    print(f"Worker：{WORKER_URL}")
    print(f"并发：{MAX_WORKERS}")
    print()

    results = []

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

        completed = 0

        for future in as_completed(future_map):
            target = future_map[future]

            completed += 1

            try:
                result = future.result()

                if result:
                    results.append(result)
                    print(
                        f"[{completed}/{len(targets)}] "
                        f"成功：{result}"
                    )
                else:
                    print(
                        f"[{completed}/{len(targets)}] "
                        f"失败：{target}"
                    )

            except Exception as e:
                print(
                    f"[{completed}/{len(targets)}] "
                    f"异常：{target} -> {e}"
                )

    # ========================================================
    # 去重
    # ========================================================

    results = list(dict.fromkeys(results))

    # ========================================================
    # 固定国家顺序 + ASN + IP 排序
    # ========================================================

    results.sort(key=sort_key)

    # ========================================================
    # 写入结果
    # ========================================================

    with open(
        output_file,
        "w",
        encoding="utf-8",
        newline="\n"
    ) as f:

        if results:
            f.write("\n".join(results))
            f.write("\n")

    # ========================================================
    # 统计
    # ========================================================

    print()
    print("=" * 60)
    print("检测完成")
    print(f"原始数量：{len(targets)}")
    print(f"成功数量：{len(results)}")
    print(f"失败数量：{len(targets) - len(results)}")
    print(f"输出文件：{output_file}")
    print("=" * 60)

    # ========================================================
    # 国家统计
    # ========================================================

    country_count = {}

    for item in results:
        try:
            info = item.split("#", 1)[1]
            country = info.split()[0]

            country_count[country] = (
                country_count.get(country, 0) + 1
            )

        except Exception:
            pass

    if country_count:
        print()
        print("国家统计：")

        for country in COUNTRY_ORDER:
            if country in country_count:
                print(
                    f"{country}: "
                    f"{country_count[country]}"
                )


if __name__ == "__main__":
    main()
