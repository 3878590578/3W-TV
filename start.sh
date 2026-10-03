#!/bin/bash

set -e

CONFIG="/app/config.json"
RUNTIME_CONFIG="/app/config.runtime.json"
KEY_FILE="/app/data/reality.key"

UUID="648f778f-f591-48e0-9d3f-4fc1dc991d11"
SHORT_ID="f923a66867de26b2"
SNI="www.microsoft.com"
TARGET="www.microsoft.com:443"
FLOW="xtls-rprx-vision"

echo ""
echo "============================================================"
echo "        VLESS + REALITY | Railway Singapore"
echo "============================================================"
echo ""

# ============================================================
# 生成 REALITY X25519 密钥
# ============================================================

if [ ! -f "$KEY_FILE" ]; then
    echo "[INFO] 首次启动，正在生成 REALITY X25519 密钥..."

    xray x25519 > /tmp/x25519.txt

    PRIVATE_KEY=$(grep -E '^Private key:' /tmp/x25519.txt | sed 's/^Private key: *//')
    PUBLIC_KEY=$(grep -E '^Public key:' /tmp/x25519.txt | sed 's/^Public key: *//')

    if [ -z "$PRIVATE_KEY" ] || [ -z "$PUBLIC_KEY" ]; then
        echo "[ERROR] REALITY 密钥生成失败"
        cat /tmp/x25519.txt
        exit 1
    fi

    printf '%s\n%s\n' "$PRIVATE_KEY" "$PUBLIC_KEY" > "$KEY_FILE"

    rm -f /tmp/x25519.txt

    echo "[OK] REALITY 密钥生成完成"
else
    echo "[INFO] 读取已有 REALITY 密钥"

    PRIVATE_KEY=$(sed -n '1p' "$KEY_FILE")
    PUBLIC_KEY=$(sed -n '2p' "$KEY_FILE")
fi

# ============================================================
# 检查密钥
# ============================================================

if [ -z "$PRIVATE_KEY" ] || [ -z "$PUBLIC_KEY" ]; then
    echo "[ERROR] REALITY 密钥为空"
    exit 1
fi

# ============================================================
# 生成运行配置
# ============================================================

python3 - "$CONFIG" "$RUNTIME_CONFIG" "$PRIVATE_KEY" <<'PY'
import json
import sys

source = sys.argv[1]
target = sys.argv[2]
private_key = sys.argv[3]

with open(source, "r", encoding="utf-8") as f:
    config = json.load(f)

reality = (
    config["inbounds"][0]
    ["streamSettings"]
    ["realitySettings"]
)

reality["privateKey"] = private_key

with open(target, "w", encoding="utf-8") as f:
    json.dump(config, f, ensure_ascii=False, indent=2)
PY

# ============================================================
# 检查 Railway TCP Proxy
# ============================================================

TCP_DOMAIN="${RAILWAY_TCP_PROXY_DOMAIN:-}"
TCP_PORT="${RAILWAY_TCP_PROXY_PORT:-}"

if [ -z "$TCP_DOMAIN" ] || [ -z "$TCP_PORT" ]; then
    echo ""
    echo "============================================================"
    echo "[ERROR] Railway TCP Proxy 尚未配置"
    echo "============================================================"
    echo ""
    echo "请进入："
    echo ""
    echo "Service"
    echo "  → Settings"
    echo "  → Networking"
    echo "  → TCP Proxy"
    echo ""
    echo "内部端口填写：443"
    echo ""
    echo "创建完成后重新部署。"
    echo ""
    exit 1
fi

# ============================================================
# 生成完整 VLESS Reality 链接
# ============================================================

VLESS_URL="vless://${UUID}@${TCP_DOMAIN}:${TCP_PORT}?security=reality&sni=${SNI}&fp=chrome&pbk=${PUBLIC_KEY}&sid=${SHORT_ID}&flow=${FLOW}&type=tcp&encryption=none#VLESS-Reality-SG"

# ============================================================
# 输出节点
# ============================================================

echo ""
echo "============================================================"
echo "                 NODE INFORMATION"
echo "============================================================"
echo ""
echo "[VLESS + REALITY]"
echo ""
echo "$VLESS_URL"
echo ""
echo "------------------------------------------------------------"
echo "Server      : $TCP_DOMAIN"
echo "Port        : $TCP_PORT"
echo "SNI         : $SNI"
echo "Public Key  : $PUBLIC_KEY"
echo "Short ID    : $SHORT_ID"
echo "UUID        : $UUID"
echo "Flow        : $FLOW"
echo ""
echo "============================================================"
echo "              COPY THE VLESS URL ABOVE"
echo "============================================================"
echo ""

# ============================================================
# 启动 Xray
# ============================================================

echo "[INFO] 正在启动 Xray..."

exec xray run -c "$RUNTIME_CONFIG"