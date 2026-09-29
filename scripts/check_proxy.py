import requests
import ipaddress
import re
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed


# ============================================================
# 配置
# ============================================================

WORKER_URL = "https://ip-jc.mofa.kdns.fr"

MAX_WORKERS = 16
TIMEOUT = 30


# ============================================================
# 国家名称
# ============================================================

COUNTRY_MAP = {
    "SG": "SG Singapore",
    "US": "US United States",
    "JP": "JP Japan",
    "KR": "KR South Korea",
    "TW": "TW Taiwan",
    "HK": "HK Hong Kong",
    "IN": "IN India",
    "CA": "CA Canada",
    "GB": "GB United Kingdom",
    "DE": "DE Germany",
    "FR": "FR France",
    "AU": "AU Australia",
    "NL": "NL Netherlands",
    "RU": "RU Russia",
    "BR": "BR Brazil",
    "ID": "ID Indonesia",
    "MY": "MY Malaysia",
    "TH": "TH Thailand",
    "VN": "VN Vietnam",
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

            # 去掉已有备注
            target = line.split("#", 1)[0].strip()

            if not target:
                continue

            targets.append(target)

    # 去重，同时保持原顺序
    return list(dict.fromkeys(targets))


# ============================================================
# 提取 IP
# ============================================================

def extract_host(target):

    target = target.strip()

    # [IPv6]:443
    if target.startswith("["):

        pos = target.find("]")

        if pos != -1:
            return target[1:pos]

    # IPv4:端口
    if ":" in target:

        host, port = target.rsplit(":", 1)

        if port.isdigit():
            return host

    return target


# ============================================================
# IP 排序
# ============================================================

def ip_sort_key(line):

    target = line.split("#", 1)[0]

    host = extract_host(target)

    try:

        ip = ipaddress.ip_address(host)

        return (
            ip.version,
            int(ip)
        )

    except Exception:

        return (
            9,
            host
        )


# ============================================================
# ASN 排序
# ============================================================

def asn_sort_key(line):

    match = re.search(
        r"\bAS(\d+)\b",
        line,
        re.IGNORECASE
    )

    if match:

        return int(match.group(1))

    return 999999999


# ============================================================
# 国家排序
# ============================================================

def country_sort_key(line):

    match = re.search(
        r"#([A-Z]{2})\s",
        line
    )

    if match:

        return match.group(1)

    return "ZZ"


# ============================================================
# 最终排序
#
# 国家
# ↓
# ASN
# ↓
# IP
# ============================================================

def sort_results(results):

    return sorted(
        results,
        key=lambda line: (
            country_sort_key(line),
            asn_sort_key(line),
            ip_sort_key(line)
        )
    )


# ============================================================
# 获取出口信息
# ============================================================

def get_exit_info(data):

    probe_results = data.get(
        "probe_results",
        {}
    )

    if not isinstance(
        probe_results,
        dict
    ):
        return {}

    # 优先 IPv4
    ipv4 = probe_results.get(
        "ipv4",
        {}
    )

    if isinstance(ipv4, dict):

        exit_data = ipv4.get("exit")

        if isinstance(exit_data, dict):

            return exit_data

    # IPv4 没有时尝试 IPv6
    ipv6 = probe_results.get(
        "ipv6",
        {}
    )

    if isinstance(ipv6, dict):

        exit_data = ipv6.get("exit")

        if isinstance(exit_data, dict):

            return exit_data

    return {}


# ============================================================
# 检测单个 IP
# ============================================================

def check_target(target):

    try:

        url = (
            WORKER_URL.rstrip("/")
            + "/check"
        )

        response = requests.get(
            url,
            params={
                "proxyip": target
            },
            timeout=TIMEOUT
        )

        if response.status_code != 200:

            print(
                f"❌ {target} "
                f"HTTP {response.status_code}"
            )

            return None

        data = response.json()

        # ====================================================
        # 必须检测成功
        # ====================================================

        if data.get("success") is not True:

            print(
                f"❌ {target} 检测失败"
            )

            return None

        # ====================================================
        # 必须存在 ms
        # ====================================================

        response_time = data.get(
            "responseTime"
        )

        if response_time is None:

            print(
                f"❌ {target} 没有 ms"
            )

            return None

        try:

            response_time = int(
                response_time
            )

        except Exception:

            print(
                f"❌ {target} ms 无效"
            )

            return None

        # ====================================================
        # 获取 IP
        # ====================================================

        proxy_ip = data.get(
            "proxyIP"
        )

        if not proxy_ip:

            proxy_ip = extract_host(
                target
            )

        # ====================================================
        # 获取端口
        # ====================================================

        port = data.get(
            "portRemote"
        )

        if not port:

            if ":" in target:

                try:

                    port = int(
                        target.rsplit(
                            ":",
                            1
                        )[1]
                    )

                except Exception:

                    port = 443

            else:

                port = 443

        # ====================================================
        # 获取出口信息
        # ====================================================

        exit_data = get_exit_info(
            data
        )

        country_code = str(
            exit_data.get(
                "country",
                ""
            )
        ).upper().strip()

        asn = exit_data.get(
            "asn"
        )

        organization = str(
            exit_data.get(
                "asOrganization",
                ""
            )
        ).strip()

        # ====================================================
        # ASN
        # ====================================================

        if isinstance(asn, int):

            asn_text = f"AS{asn}"

        elif isinstance(asn, str):

            asn_text = asn.strip()

            if asn_text:

                if not asn_text.upper().startswith("AS"):

                    asn_text = (
                        "AS"
                        + asn_text
                    )

            else:

                asn_text = "AS0"

        else:

            asn_text = "AS0"

        # ====================================================
        # 国家
        # ====================================================

        country_text = COUNTRY_MAP.get(
            country_code
        )

        if not country_text:

            if country_code:

                country_text = (
                    country_code
                    + " Unknown"
                )

            else:

                country_text = (
                    "ZZ Unknown"
                )

        # ====================================================
        # 服务商
        # ====================================================

        if not organization:

            organization = "Unknown"

        # ====================================================
        # 最终格式
        #
        # IP:端口#国家地区 AS 服务商
        # ====================================================

        result = (
            f"{proxy_ip}:{port}"
            f"#{country_text} "
            f"{asn_text} "
            f"{organization}"
        )

        print(
            f"✅ {target} "
            f"→ {response_time}ms "
            f"→ {result}"
        )

        return result

    except Exception as e:

        print(
            f"❌ {target} "
            f"→ {type(e).__name__}: {e}"
        )

        return None


# ============================================================
# 主程序
# ============================================================

def main():

    if len(sys.argv) != 3:

        print(
            "用法："
            "python check_proxy.py 输入文件 输出文件"
        )

        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    targets = read_targets(
        input_file
    )

    print()
    print("=" * 60)
    print("开始检测")
    print(f"检测接口：{WORKER_URL}")
    print(f"输入文件：{input_file}")
    print(f"目标数量：{len(targets)}")
    print(f"并发数量：{MAX_WORKERS}")
    print("=" * 60)
    print()

    results = []

    # ========================================================
    # 并发检测
    # ========================================================

    with ThreadPoolExecutor(
        max_workers=MAX_WORKERS
    ) as executor:

        futures = {
            executor.submit(
                check_target,
                target
            ): target
            for target in targets
        }

        for future in as_completed(
            futures
        ):

            try:

                result = future.result()

                if result:

                    results.append(
                        result
                    )

            except Exception as e:

                target = futures[future]

                print(
                    f"❌ {target} "
                    f"→ {e}"
                )

    # ========================================================
    # 排序
    # ========================================================

    results = sort_results(
        results
    )

    # ========================================================
    # 写入结果
    # ========================================================

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as f:

        for line in results:

            f.write(
                line + "\n"
            )

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