import sys
import re
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
# 香港 HK
# 台湾 TW
# 新加坡 SG
# 日本 JP
# 美国 US
# 印度 IN
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
# 读取 IP
# ============================================================

def read_targets(filename):
    targets = []

    with open(filename, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()

            if not line:
                continue

            # 去掉旧的 # 信息
            line = line.split("#", 1)[0].strip()

            if line not in targets:
                targets.append(line)

    return targets


# ============================================================
# 单个 IP 检测
# ============================================================

def check_proxy(target):
    try:
        url = f"{WORKER_URL}/check"

        response = requests.get(
            url,
            params={
                "proxyip": target
            },
            timeout=TIMEOUT
        )

        if response.status_code != 200:
            print(f"[失败] {target} HTTP {response.status_code}")
            return None

        data = response.json()

        # 必须 success=true
        if data.get("success") is not True:
            print(f"[失败] {target} success=false")
            return None

        # 必须有 responseTime
        response_time = data.get("responseTime")

        if response_time is None:
            print(f"[失败] {target} 没有 responseTime")
            return None

        try:
            response_time = int(response_time)
        except Exception:
            print(f"[失败] {target} responseTime 无效")
            return None

        # ====================================================
        # 获取出口信息
        # ====================================================

        probe_results = data.get("probe_results", {})

        ipv4 = probe_results.get("ipv4", {})
        ipv6 = probe_results.get("ipv6", {})

        exit_ip = (
            ipv4.get("exit")
            or ipv6.get("exit")
        )

        if not exit_ip:
            print(f"[失败] {target} 没有出口 IP")
            return None

        # ====================================================
        # 国家
        # ====================================================

        country = data.get("country", "")

        if not country:
            country = (
                ipv4.get("country")
                or ipv6.get("country")
                or ""
            )

        country = str(country).upper().strip()

        if not country:
            print(f"[失败] {target} 没有国家")
            return None

        country_name = COUNTRY_NAME.get(
            country,
            country
        )

        # ====================================================
        # ASN
        # ====================================================

        asn = data.get("asn", "")

        if not asn:
            asn = (
                ipv4.get("asn")
                or ipv6.get("asn")
                or ""
            )

        asn = str(asn).strip()

        if asn:
            if not asn.upper().startswith("AS"):
                asn = "AS" + asn.lstrip("asAS")

        # ====================================================
        # ASN 组织
        # ====================================================

        organization = data.get("asOrganization", "")

        if not organization:
            organization = (
                ipv4.get("asOrganization")
                or ipv6.get("asOrganization")
                or ""
            )

        organization = str(organization).strip()

        # ====================================================
        # 输出
        # ====================================================

        result = (
            f"{target}#"
            f"{country} "
            f"{country_name} "
            f"{asn} "
            f"{organization}"
        )

        print(f"[成功] {result}")

        return result

    except Exception as e:
        print(f"[异常] {target} -> {e}")
        return None


# ============================================================
# 排序
#
# 国家：
# HK → TW → SG → JP → US → IN
#
# 同国家：
# ASN 数字升序
#
# 同 ASN：
# IP 数字升序
# ============================================================

def sort_key(item):
    ip_port, info = item.split("#", 1)

    # IP
    ip = ip_port.rsplit(":", 1)[0]

    ip_parts = tuple(
        int(x)
        for x in ip.split(".")
    )

    # 国家代码
    country = info.split()[0].upper()

    # ASN
    asn_match = re.search(
        r"\bAS(\d+)\b",
        info,
        re.IGNORECASE
    )

    if asn_match:
        asn_number = int(asn_match.group(1))
    else:
        asn_number = 999999999

    # 固定国家顺序
    country_number = COUNTRY_ORDER.get(
        country,
        999
    )

    return (
        country_number,
        asn_number,
        ip_parts
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

    print(f"待检测：{len(targets)}")
    print(f"Worker：{WORKER_URL}")
    print(f"并发：{MAX_WORKERS}")
    print()

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

        completed = 0

        for future in as_completed(future_map):

            target = future_map[future]

            completed += 1

            try:
                result = future.result()

                if result:
                    results.append(result)

                print(
                    f"进度：{completed}/{len(targets)}"
                )

            except Exception as e:
                print(
                    f"[异常] {target} -> {e}"
                )

    # ========================================================
    # 去重
    # ========================================================

    results = list(
        dict.fromkeys(results)
    )

    # ========================================================
    # 固定国家顺序
    # ========================================================

    results.sort(
        key=sort_key
    )

    # ========================================================
    # 写入文件
    # ========================================================

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as f:

        for result in results:
            f.write(result + "\n")

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


if __name__ == "__main__":
    main()
