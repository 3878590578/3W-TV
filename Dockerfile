FROM debian:bookworm-slim

ARG XRAY_VERSION=26.9.8

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl unzip \
    && rm -rf /var/lib/apt/lists/* \
    && curl -L --fail \
       "https://github.com/XTLS/Xray-core/releases/download/v${XRAY_VERSION}/Xray-linux-64.zip" \
       -o /tmp/xray.zip \
    && unzip -q /tmp/xray.zip -d /tmp/xray \
    && install -m 755 /tmp/xray/xray /usr/local/bin/xray \
    && rm -rf /tmp/xray /tmp/xray.zip

WORKDIR /app

COPY config.json /app/config.json
COPY start.sh /app/start.sh

RUN chmod +x /app/start.sh

EXPOSE 443
EXPOSE 8388

CMD ["/app/start.sh"]