#!/bin/bash

set -e

CONFIG="/app/config.json"
RUNTIME_CONFIG="/app/config.runtime.json"
KEY_FILE="/app/data/reality.key"

UUID="648f778f-f591-48e0-9d3f-4fc1dc991d11"
SHORT_ID="f923a66867de26b2"
SNI="www.microsoft.com"
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

    X25519_OUTPUT="$(xray x25519)"

    PRIVATE_KEY="$(printf '%s\n' "$X25519_OUTPUT" \
        | grep '^PrivateKey:' \
        | sed 's/^PrivateKey:[[:space:]]*//')"

    PUBLIC_KEY="$(printf '%s\n' "$X25519_OUTPUT" \
        | grep '^Password (PublicKey):' \
        | sed 's/^Password (PublicKey):[[:space:]]*//')"

    if [ -z "$PRIVATE_KEY" ] || [ -z "$PUBLIC_KEY" ]; then
        echo ""
        echo "[ERROR] REALITY 密钥解析失败"
        echo ""
        echo "$X25519_OUTPUT"
        echo ""
        exit 1
    fi

    printf '%s\n%s\n' "$PRIVATE_KEY" "$PUBLIC_KEY" > "$KEY_FILE"

    echo "[OK] REALITY 密钥生成成功"

else

    echo "[INFO] 读取已有 REALITY 密钥"

    PRIVATE_KEY="$(sed -n '1p' "$KEY_FILE")"
    PUBLIC_KEY="$(sed -n '2p' "$KEY_FILE")"

fi

# ============================================================
# 检查密钥
# ============================================================

if [ -z "$PRIVATE_KEY" ] || [ -z "$PUBLIC_KEY" ]; then

    echo ""
    echo "[ERROR] REALITY 密钥为空"
    echo ""

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
    json.dump(
        config,
        f,
        ensure_ascii=False,
        indent=2
    )

PY

# ============================================================
# Railway TCP Proxy
# ============================================================

TCP_DOMAIN="${RAILWAY_TCP_PROXY_DOMAIN:-}"
TCP_PORT="${RAILWAY_TCP_PROXY_PORT:-}"

if [ -z "$TCP_DOMAIN" ] || [ -z "$TCP_PORT" ]; then

    echo ""
    echo "============================================================"
    echo "[ERROR] Railway TCP Proxy 尚未配置"
    echo "============================================================"
    echo ""
    echo "请在 Railway："
    echo ""
    echo "Service"
    echo " → Settings"
    echo " → Networking"
    echo " → TCP Proxy"
    echo ""
    echo "内部端口：443"
    echo ""
    exit 1

fi

# ============================================================
# 生成 VLESS Reality URI
# ============================================================

VLESS_URL="vless://${UUID}@${TCP_DOMAIN}:${TCP_PORT}?encryption=none&flow=${FLOW}&security=reality&sni=${SNI}&fp=chrome&pbk=${PUBLIC_KEY}&sid=${SHORT_ID}&type=tcp#VLESS-Reality-SG"

# ============================================================
# 输出节点信息
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
# 检查 Xray 配置
# ============================================================

echo "[INFO] 检查 Xray 配置..."

if ! xray run -test -c "$RUNTIME_CONFIG"; then

    echo ""
    echo "[ERROR] Xray 配置检查失败"
    echo ""

    exit 1

fi

echo "[OK] Xray 配置检查通过"
echo ""

# ============================================================
# 启动 Xray
# ============================================================

echo "[INFO] 正在启动 Xray..."
echo ""

exec xray run -c "$RUNTIME_CONFIG"