import sys
import os
import re
import requests
from concurrent.futures import ThreadPoolExecutor, as_completed


# ============================================================
# 配置
# ============================================================

WORKER_URL = "https://ip-jc.mofa.kdns.fr/check"

# 最大并发
MAX_WORKERS = 100

# 单次请求超时
TIMEOUT = 5

# 失败后的重试次数
# 1 = 第一次失败后再检测一次
MAX_RETRIES = 1

# 国家固定排序
COUNTRY_ORDER = {
    "HK": 0,
    "TW": 1,
    "SG": 2,
    "JP": 3,
    "US": 4,
    "IN": 5,
}


# ============================================================
# IPv4
# ============================================================

def is_valid_ipv4(ip):
    pattern = r"^(?:\d{1,3}\.){3}\d{1,3}$"

    if not re.match(pattern, ip):
        return False

    try:
        parts = ip.split(".")
        return all(0 <= int(x) <= 255 for x in parts)
    except Exception:
        return False


# ============================================================
# 提取 IP
# ============================================================

def extract_ip(line):
    """
    支持：

    IP
    IP:443
    IP#备注
    IP:443#备注
    """

    line = line.strip()

    if not line:
        return None

    # 去掉备注
    line = line.split("#", 1)[0].strip()

    # 提取 IP
    match = re.match(
        r"^(\d{1,3}(?:\.\d{1,3}){3})(?::\d+)?$",
        line
    )

    if not match:
        return None

    ip = match.group(1)

    if not is_valid_ipv4(ip):
        return None

    return ip


# ============================================================
# 读取 IP
# ============================================================

def read_ips(filename):
    ips = []

    if not os.path.exists(filename):
        return ips

    with open(filename, "r", encoding="utf-8") as f:
        for line in f:
            ip = extract_ip(line)

            if ip:
                ips.append(ip)

    return ips


# ============================================================
# 去重
# ============================================================

def unique_ips(ips):
    result = []
    seen = set()

    for ip in ips:
        if ip not in seen:
            seen.add(ip)
            result.append(ip)

    return result


# ============================================================
# IP 数字排序
# ============================================================

def ip_sort_key(ip):
    return tuple(int(x) for x in ip.split("."))


# ============================================================
# 国家代码
# ============================================================

def get_country_code(country_name):
    if not country_name:
        return None

    country_upper = str(country_name).strip().upper()

    country_map = {
        "HONG KONG": "HK",
        "HONGKONG": "HK",

        "TAIWAN": "TW",
        "TAIWAN, CHINA": "TW",

        "SINGAPORE": "SG",

        "JAPAN": "JP",

        "UNITED STATES": "US",
        "UNITED STATES OF AMERICA": "US",
        "USA": "US",

        "INDIA": "IN",
    }

    if country_upper in COUNTRY_ORDER:
        return country_upper

    return country_map.get(country_upper)


# ============================================================
# 单个 IP 检测
# ============================================================

def check_proxy(ip):
    """
    使用：

    /check?proxyip=IP:443

    注意：
    三国.txt 没有端口，因此第一次检测统一使用 443
    Worker 会返回真正检测到的 portRemote。
    """

    for attempt in range(MAX_RETRIES + 1):

        try:
            response = requests.get(
                WORKER_URL,
                params={
                    "proxyip": f"{ip}:443"
                },
                timeout=TIMEOUT
            )

            if response.status_code != 200:
                raise Exception(
                    f"HTTP {response.status_code}"
                )

            data = response.json()

            # ------------------------------------------------
            # success
            # ------------------------------------------------

            if data.get("success") is not True:
                raise Exception("success != true")

            # ------------------------------------------------
            # responseTime
            # ------------------------------------------------

            response_time = data.get("responseTime")

            if response_time is None:
                raise Exception("没有 responseTime")

            # ------------------------------------------------
            # Worker 实际端口
            # ------------------------------------------------

            port_remote = data.get("portRemote")

            if port_remote is None:
                raise Exception("没有 portRemote")

            try:
                port_remote = int(port_remote)
            except Exception:
                raise Exception(
                    f"portRemote 无效: {port_remote}"
                )

            if not 1 <= port_remote <= 65535:
                raise Exception(
                    f"portRemote 超出范围: {port_remote}"
                )

            # ------------------------------------------------
            # probe_results
            # ------------------------------------------------

            probe_results = data.get("probe_results") or {}

            ipv4 = probe_results.get("ipv4") or {}
            ipv6 = probe_results.get("ipv6") or {}

            ipv4_exit = ipv4.get("exit") or {}
            ipv6_exit = ipv6.get("exit") or {}

            # 优先 IPv4
            if ipv4_exit:
                exit_info = ipv4_exit
            elif ipv6_exit:
                exit_info = ipv6_exit
            else:
                raise Exception("没有 exit 信息")

            # ------------------------------------------------
            # 国家
            # ------------------------------------------------

            country_name = exit_info.get("country")

            country_code = get_country_code(
                country_name
            )

            if not country_code:
                raise Exception(
                    f"国家不属于 HK/TW/SG/JP/US/IN: "
                    f"{country_name}"
                )

            # ------------------------------------------------
            # 城市
            # ------------------------------------------------

            city = str(
                exit_info.get("city") or ""
            ).strip()

            # ------------------------------------------------
            # ASN
            # ------------------------------------------------

            asn = exit_info.get("asn")

            if not asn:
                raise Exception("没有 ASN")

            asn_text = str(asn).strip()

            if asn_text.upper().startswith("AS"):
                asn_number = asn_text[2:]
            else:
                asn_number = asn_text

            try:
                asn_number_int = int(asn_number)
            except Exception:
                raise Exception(
                    f"ASN 无效: {asn}"
                )

            # ------------------------------------------------
            # 组织
            # ------------------------------------------------

            organization = (
                exit_info.get("asOrganization")
                or exit_info.get("organization")
                or ""
            )

            organization = str(
                organization
            ).strip()

            # ------------------------------------------------
            # 备注
            # ------------------------------------------------

            if organization:
                remark = (
                    f"{country_code} "
                    f"{city} "
                    f"AS{asn_number_int} "
                    f"{organization}"
                )
            else:
                remark = (
                    f"{country_code} "
                    f"{city} "
                    f"AS{asn_number_int}"
                )

            remark = re.sub(
                r"\s+",
                " ",
                remark
            ).strip()

            # ------------------------------------------------
            # 成功
            # ------------------------------------------------

            return {
                "ip": ip,
                "port": port_remote,
                "country": country_code,
                "city": city,
                "asn": asn_number_int,
                "organization": organization,
                "response_time": response_time,
                "remark": remark,
                "attempt": attempt + 1,
            }

        except Exception as e:

            if attempt < MAX_RETRIES:
                print(
                    f"[重试] {ip} "
                    f"第 {attempt + 1} 次失败：{e}"
                )
                continue

            return None


# ============================================================
# 排序
# ============================================================

def sort_results(results):
    """
    国家：
    HK → TW → SG → JP → US → IN

    同国家：
    ASN 从小到大

    同 ASN：
    IP 从小到大
    """

    return sorted(
        results,
        key=lambda x: (
            COUNTRY_ORDER.get(
                x["country"],
                999
            ),
            x["asn"],
            ip_sort_key(x["ip"]),
        )
    )


# ============================================================
# 写入结果
# ============================================================

def write_results(results, output_file):

    parent = os.path.dirname(
        os.path.abspath(output_file)
    )

    os.makedirs(
        parent,
        exist_ok=True
    )

    with open(
        output_file,
        "w",
        encoding="utf-8",
        newline="\n"
    ) as f:

        for item in results:

            line = (
                f'{item["ip"]}:'
                f'{item["port"]}#'
                f'{item["remark"]}'
            )

            f.write(line + "\n")


# ============================================================
# 并发检测
# ============================================================

def run_checks(all_ips):

    results = []

    success_count = 0
    fail_count = 0

    total = len(all_ips)

    print("")
    print("=" * 60)
    print("开始并发检测")
    print(f"IP 数量：{total}")
    print(f"并发数量：{MAX_WORKERS}")
    print(f"单次超时：{TIMEOUT} 秒")
    print(f"失败重试：{MAX_RETRIES} 次")
    print("=" * 60)
    print("")

    with ThreadPoolExecutor(
        max_workers=MAX_WORKERS
    ) as executor:

        future_map = {
            executor.submit(
                check_proxy,
                ip
            ): ip
            for ip in all_ips
        }

        for index, future in enumerate(
            as_completed(future_map),
            start=1
        ):

            ip = future_map[future]

            try:
                result = future.result()

            except Exception as e:

                result = None

                print(
                    f"[{index}/{total}] "
                    f"✗ {ip} "
                    f"异常：{e}"
                )

            if result:

                results.append(result)

                success_count += 1

                print(
                    f"[{index}/{total}] "
                    f"✓ {ip}:"
                    f"{result['port']} "
                    f"{result['country']} "
                    f"{result['city']} "
                    f"AS{result['asn']} "
                    f"{result['organization']}"
                )

            else:

                fail_count += 1

                print(
                    f"[{index}/{total}] "
                    f"✗ {ip}"
                )

    print("")
    print("=" * 60)
    print("检测完成")
    print(f"总数：{total}")
    print(f"成功：{success_count}")
    print(f"失败：{fail_count}")
    print("=" * 60)
    print("")

    return results


# ============================================================
# 主程序
# ============================================================

def main():

    args = sys.argv[1:]

    # --------------------------------------------------------
    # 参数
    # --------------------------------------------------------

    if len(args) == 3:

        source_file = args[0]
        old_file = args[1]
        output_file = args[2]

        print("=" * 60)
        print("模式：三国.txt + 已筛.txt 合并检测")
        print("=" * 60)

        source_ips = read_ips(
            source_file
        )

        old_ips = read_ips(
            old_file
        )

        print(
            f"三国.txt：{len(source_ips)} 个 IP"
        )

        print(
            f"旧已筛.txt：{len(old_ips)} 个 IP"
        )

        all_ips = unique_ips(
            source_ips + old_ips
        )

    elif len(args) == 2:

        input_file = args[0]
        output_file = args[1]

        print("=" * 60)
        print("模式：重新检测已筛.txt")
        print("=" * 60)

        all_ips = unique_ips(
            read_ips(input_file)
        )

        print(
            f"已筛.txt：{len(all_ips)} 个 IP"
        )

    else:

        print("")
        print("参数错误。")
        print("")
        print(
            "第一种："
        )
        print(
            "python scripts/check_proxy.py "
            "三国.txt 已筛.txt 已筛_new.txt"
        )
        print("")
        print(
            "第二种："
        )
        print(
            "python scripts/check_proxy.py "
            "已筛.txt 已筛_new.txt"
        )
        print("")

        sys.exit(1)

    # --------------------------------------------------------
    # 输入输出不能相同
    # --------------------------------------------------------

    if len(args) == 3:

        input_paths = [
            os.path.abspath(
                source_file
            ),
            os.path.abspath(
                old_file
            ),
        ]

    else:

        input_paths = [
            os.path.abspath(
                input_file
            )
        ]

    output_abs = os.path.abspath(
        output_file
    )

    if output_abs in input_paths:

        print("")
        print(
            "错误：输出文件不能和输入文件相同。"
        )
        print(
            f"输出：{output_file}"
        )
        print("")

        sys.exit(1)

    # --------------------------------------------------------
    # 没有 IP
    # --------------------------------------------------------

    if not all_ips:

        print("")
        print(
            "没有可检测的 IP。"
        )
        print(
            "不会覆盖原来的已筛.txt。"
        )
        print("")

        sys.exit(0)

    # --------------------------------------------------------
    # 去重后数量
    # --------------------------------------------------------

    print("")
    print(
        f"合并去重后：{len(all_ips)} 个 IP"
    )
    print("")

    # --------------------------------------------------------
    # 并发检测
    # --------------------------------------------------------

    results = run_checks(
        all_ips
    )

    # --------------------------------------------------------
    # 全部失败
    # --------------------------------------------------------

    if not results:

        print(
            "所有 IP 都检测失败。"
        )

        print(
            "为了保护原来的已筛.txt，"
            "不会生成新的结果文件。"
        )

        if os.path.exists(
            output_file
        ):

            try:
                os.remove(
                    output_file
                )
            except Exception:
                pass

        sys.exit(0)

    # --------------------------------------------------------
    # 排序
    # --------------------------------------------------------

    results = sort_results(
        results
    )

    # --------------------------------------------------------
    # 写临时文件
    # --------------------------------------------------------

    write_results(
        results,
        output_file
    )

    # --------------------------------------------------------
    # 输出统计
    # --------------------------------------------------------

    print("")
    print("=" * 60)
    print(
        f"最终保留：{len(results)} 个 IP"
    )
    print(
        f"输出文件：{output_file}"
    )
    print("=" * 60)
    print("")

    # --------------------------------------------------------
    # 显示最终结果
    # --------------------------------------------------------

    for item in results:

        print(
            f'{item["ip"]}:'
            f'{item["port"]}#'
            f'{item["remark"]}'
        )

    print("")
    print(
        "检测结果已生成。"
    )
    print(
        "Workflow 随后会将已筛_new.txt "
        "替换为正式的已筛.txt。"
    )


# ============================================================
# 入口
# ============================================================

if __name__ == "__main__":
    main()