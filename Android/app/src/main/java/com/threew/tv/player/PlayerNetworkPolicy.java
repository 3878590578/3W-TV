package com.threew.tv.player;

/**
 * 播放器网络策略。
 *
 * 当前只描述策略，不直接操作网络。
 */
public class PlayerNetworkPolicy {

    public enum NetworkMode {
        ANY,
        WIFI_ONLY,
        MOBILE_ONLY
    }

    private NetworkMode networkMode;
    private boolean allowMetered;
    private boolean allowCellularFallback;

    public PlayerNetworkPolicy() {
        networkMode = NetworkMode.ANY;
        allowMetered = true;
        allowCellularFallback = true;
    }

    public NetworkMode getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(NetworkMode networkMode) {
        this.networkMode = networkMode == null
                ? NetworkMode.ANY
                : networkMode;
    }

    public boolean isAllowMetered() {
        return allowMetered;
    }

    public void setAllowMetered(boolean allowMetered) {
        this.allowMetered = allowMetered;
    }

    public boolean isAllowCellularFallback() {
        return allowCellularFallback;
    }

    public void setAllowCellularFallback(boolean allowCellularFallback) {
        this.allowCellularFallback = allowCellularFallback;
    }

    public boolean isWifiOnly() {
        return networkMode == NetworkMode.WIFI_ONLY;
    }

    public boolean isMobileOnly() {
        return networkMode == NetworkMode.MOBILE_ONLY;
    }

    public PlayerNetworkPolicy copy() {
        PlayerNetworkPolicy copy = new PlayerNetworkPolicy();
        copy.networkMode = networkMode;
        copy.allowMetered = allowMetered;
        copy.allowCellularFallback = allowCellularFallback;
        return copy;
    }
}