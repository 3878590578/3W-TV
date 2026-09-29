import sys
import os
import re
import time
import requests


# ============================================================
# 配置
# ============================================================

WORKER_URL = "https://ip-jc.mofa.kdns.fr/check"

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
# 基础函数
# ============================================================

def is_valid_ipv4(ip):
    """
    检查是否为合法 IPv4
    """
    pattern = r"^(?:\d{1,3}\.){3}\d{1,3}$"

    if not re.match(pattern, ip):
        return False

    try:
        parts = ip.split(".")
        return all(0 <= int(x) <= 255 for x in parts)
    except Exception:
        return False


def extract_ip(line):
    """
    支持以下格式：

    47.131.189.221

    47.131.189.221:443

    47.131.189.221#SG Singapore AS16509 Amazon.com, Inc.

    47.131.189.221:443#SG Singapore AS16509 Amazon.com, Inc.

    最终只提取 IP
    """

    line = line.strip()

    if not line:
        return None

    # 去掉 #
    line = line.split("#", 1)[0].strip()

    # 去掉 :端口
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


def read_ips(filename):
    """
    从文件中读取 IP。

    支持：

    IP
    IP:PORT
    IP#备注
    IP:PORT#备注
    """

    ips = []

    if not os.path.exists(filename):
        return ips

    with open(filename, "r", encoding="utf-8") as f:
        for line in f:
            ip = extract_ip(line)

            if ip:
                ips.append(ip)

    return ips


def unique_ips(ips):
    """
    去重，同时保持第一次出现的顺序
    """

    result = []
    seen = set()

    for ip in ips:
        if ip not in seen:
            seen.add(ip)
            result.append(ip)

    return result


def ip_sort_key(ip):
    """
    IPv4 数字排序
    """

    return tuple(int(x) for x in ip.split("."))


# ============================================================
# Worker 检测
# ============================================================

def check_proxy(ip):
    """
    调用 Cloudflare Worker：

    /check?ip=IP

    返回：

    {
        success,
        portRemote,
        responseTime,
        probe_results
    }
    """

    url = WORKER_URL

    try:
        response = requests.get(
            url,
            params={"ip": ip},
            timeout=30
        )

        if response.status_code != 200:
            print(
                f"[失败] {ip} HTTP {response.status_code}"
            )
            return None

        data = response.json()

    except Exception as e:
        print(f"[失败] {ip} 请求错误: {e}")
        return None

    # --------------------------------------------------------
    # success
    # --------------------------------------------------------

    if data.get("success") is not True:
        print(f"[失败] {ip} success != true")
        return None

    # --------------------------------------------------------
    # responseTime
    # --------------------------------------------------------

    response_time = data.get("responseTime")

    if response_time is None:
        print(f"[失败] {ip} 没有 responseTime")
        return None

    # --------------------------------------------------------
    # Worker 实际返回端口
    # --------------------------------------------------------

    port_remote = data.get("portRemote")

    if port_remote is None:
        print(f"[失败] {ip} 没有 portRemote")
        return None

    try:
        port_remote = int(port_remote)
    except Exception:
        print(f"[失败] {ip} portRemote 无效: {port_remote}")
        return None

    if port_remote < 1 or port_remote > 65535:
        print(f"[失败] {ip} portRemote 超出范围: {port_remote}")
        return None

    # --------------------------------------------------------
    # 读取 probe_results
    #
    # 正确结构：
    #
    # probe_results
    #   ├── ipv4
    #   │    └── exit
    #   │         ├── country
    #   │         ├── city
    #   │         ├── asn
    #   │         └── asOrganization
    #   │
    #   └── ipv6
    #        └── exit
    # --------------------------------------------------------

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
        print(f"[失败] {ip} 没有 exit 信息")
        return None

    # --------------------------------------------------------
    # 国家
    # --------------------------------------------------------

    country_name = exit_info.get("country")

    if not country_name:
        print(f"[失败] {ip} 没有国家信息")
        return None

    # --------------------------------------------------------
    # 城市
    # --------------------------------------------------------

    city = exit_info.get("city") or ""

    # --------------------------------------------------------
    # ASN
    # --------------------------------------------------------

    asn = exit_info.get("asn")

    if not asn:
        print(f"[失败] {ip} 没有 ASN")
        return None

    # --------------------------------------------------------
    # ASN 组织
    # --------------------------------------------------------

    organization = (
        exit_info.get("asOrganization")
        or exit_info.get("organization")
        or ""
    )

    # --------------------------------------------------------
    # 国家代码
    #
    # Worker 的 country 可能是：
    #
    # Singapore
    # Hong Kong
    # Taiwan
    # Japan
    # United States
    # India
    #
    # 也可能直接返回 SG/HK/TW 等
    # --------------------------------------------------------

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
        country_code = country_upper
    else:
        country_code = country_map.get(country_upper)

    if not country_code:
        print(
            f"[跳过] {ip} 国家不属于 HK/TW/SG/JP/US/IN: "
            f"{country_name}"
        )
        return None

    # --------------------------------------------------------
    # 清理城市
    # --------------------------------------------------------

    city = str(city).strip()

    # --------------------------------------------------------
    # ASN 格式
    # --------------------------------------------------------

    asn_text = str(asn).strip()

    if asn_text.upper().startswith("AS"):
        asn_number = asn_text[2:]
    else:
        asn_number = asn_text

    # ASN 必须尽量是数字
    try:
        int(asn_number)
    except Exception:
        print(f"[失败] {ip} ASN 无效: {asn}")
        return None

    # --------------------------------------------------------
    # 组织名称
    # --------------------------------------------------------

    organization = str(organization).strip()

    if organization:
        remark = (
            f"{country_code} "
            f"{city} "
            f"AS{asn_number} "
            f"{organization}"
        )
    else:
        remark = (
            f"{country_code} "
            f"{city} "
            f"AS{asn_number}"
        )

    # 清理连续空格
    remark = re.sub(r"\s+", " ", remark).strip()

    # --------------------------------------------------------
    # 最终结果
    # --------------------------------------------------------

    result = {
        "ip": ip,
        "port": port_remote,
        "country": country_code,
        "city": city,
        "asn": int(asn_number),
        "organization": organization,
        "response_time": response_time,
        "remark": remark,
    }

    return result


# ============================================================
# 排序
# ============================================================

def sort_results(results):
    """
    排序：

    1. HK
    2. TW
    3. SG
    4. JP
    5. US
    6. IN

    同国家：
    ASN 从小到大

    同 ASN：
    IP 数字从小到大
    """

    return sorted(
        results,
        key=lambda x: (
            COUNTRY_ORDER.get(x["country"], 999),
            x["asn"],
            ip_sort_key(x["ip"]),
        )
    )


# ============================================================
# 写入结果
# ============================================================

def write_results(results, output_file):
    """
    写入：

    IP:端口#国家 城市 AS ASN 组织
    """

    # 确保目录存在
    parent = os.path.dirname(os.path.abspath(output_file))

    if parent:
        os.makedirs(parent, exist_ok=True)

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
# 主程序
# ============================================================

def main():
    """
    支持两种调用方式：

    第一种：

    python scripts/check_proxy.py 三国.txt 已筛.txt 已筛_new.txt

    含义：

    三国.txt
       +
    已筛.txt
       ↓
    合并 IP
       ↓
    全部重新检测
       ↓
    已筛_new.txt


    第二种：

    python scripts/check_proxy.py 已筛.txt 已筛_new.txt

    含义：

    已筛.txt
       ↓
    提取 IP
       ↓
    全部重新检测
       ↓
    已筛_new.txt
    """

    args = sys.argv[1:]

    if len(args) not in (2, 3):
        print("")
        print("用法：")
        print("")
        print("第一种：")
        print(
            "python scripts/check_proxy.py "
            "三国.txt 已筛.txt 已筛_new.txt"
        )
        print("")
        print("第二种：")
        print(
            "python scripts/check_proxy.py "
            "已筛.txt 已筛_new.txt"
        )
        print("")
        sys.exit(1)

    # --------------------------------------------------------
    # 第一种：三国 + 已筛
    # --------------------------------------------------------

    if len(args) == 3:

        source_file = args[0]
        old_file = args[1]
        output_file = args[2]

        print("=" * 60)
        print("模式：合并 三国.txt + 已筛.txt 后重新检测")
        print("=" * 60)

        source_ips = read_ips(source_file)
        old_ips = read_ips(old_file)

        print(f"三国.txt IP 数量：{len(source_ips)}")
        print(f"旧已筛.txt IP 数量：{len(old_ips)}")

        all_ips = unique_ips(
            source_ips + old_ips
        )

    # --------------------------------------------------------
    # 第二种：只检测已筛
    # --------------------------------------------------------

    else:

        input_file = args[0]
        output_file = args[1]

        print("=" * 60)
        print("模式：重新检测已筛.txt")
        print("=" * 60)

        all_ips = unique_ips(
            read_ips(input_file)
        )

        print(f"输入 IP 数量：{len(all_ips)}")

    # --------------------------------------------------------
    # 防止误把输入和输出设置成同一个文件
    # --------------------------------------------------------

    input_paths = []

    if len(args) == 3:
        input_paths = [
            os.path.abspath(args[0]),
            os.path.abspath(args[1]),
        ]
    else:
        input_paths = [
            os.path.abspath(args[0]),
        ]

    output_abs = os.path.abspath(output_file)

    if output_abs in input_paths:
        print("")
        print("错误：输出文件不能和输入文件相同。")
        print(f"输出文件：{output_file}")
        print("")
        print("必须使用临时文件，例如：")
        print("已筛_new.txt")
        print("")
        sys.exit(1)

    # --------------------------------------------------------
    # 没有 IP
    # --------------------------------------------------------

    if not all_ips:
        print("")
        print("没有可检测的 IP。")
        print("不会生成新的结果，不会覆盖旧的已筛.txt。")
        print("")
        sys.exit(0)

    print("")
    print(f"合并后待检测 IP：{len(all_ips)}")
    print("")

    # --------------------------------------------------------
    # 开始检测
    # --------------------------------------------------------

    results = []

    success_count = 0

    for index, ip in enumerate(all_ips, start=1):

        print(
            f"[{index}/{len(all_ips)}] "
            f"检测 {ip}"
        )

        result = check_proxy(ip)

        if result:

            results.append(result)

            success_count += 1

            print(
                f"  ✓ 成功 "
                f"{result['ip']}:{result['port']} "
                f"{result['country']} "
                f"{result['city']} "
                f"AS{result['asn']} "
                f"{result['organization']}"
            )

        else:

            print(
                f"  ✗ 失败"
            )

        # 稍微停顿，避免连续请求过快
        time.sleep(0.1)

    print("")
    print("=" * 60)
    print(f"检测完成")
    print(f"总 IP：{len(all_ips)}")
    print(f"成功：{success_count}")
    print(f"失败：{len(all_ips) - success_count}")
    print("=" * 60)
    print("")

    # --------------------------------------------------------
    # 安全保护
    #
    # 一个都成功：
    # 不生成新文件
    # 不允许 Workflow 替换旧已筛.txt
    # --------------------------------------------------------

    if success_count == 0:

        print(
            "所有 IP 检测失败，"
            "为了防止已筛.txt 被清空，"
            "不会生成新的输出文件。"
        )

        # 如果之前残留临时文件，删除
        if os.path.exists(output_file):
            try:
                os.remove(output_file)
            except Exception:
                pass

        sys.exit(0)

    # --------------------------------------------------------
    # 排序
    # --------------------------------------------------------

    results = sort_results(results)

    # --------------------------------------------------------
    # 写入临时文件
    # --------------------------------------------------------

    write_results(
        results,
        output_file
    )

    # --------------------------------------------------------
    # 显示结果
    # --------------------------------------------------------

    print(
        f"新的检测结果已经写入："
        f"{output_file}"
    )

    print(
        f"最终保留：{len(results)} 条"
    )

    print("")

    for item in results:
        print(
            f'{item["ip"]}:'
            f'{item["port"]}#'
            f'{item["remark"]}'
        )

    print("")
    print(
        "注意：这里只负责生成临时结果文件，"
        "Workflow 随后会使用 mv 替换正式的已筛.txt。"
    )


if __name__ == "__main__":
    main()