package com.threew.tv.player;

/**
 * 播放器操作请求。
 */
public class PlayerActionRequest {

    private final PlayerAction action;

    private long positionMs;
    private float value;

    private String valueText;

    public PlayerActionRequest(PlayerAction action) {
        this.action = action;
    }

    public PlayerAction getAction() {
        return action;
    }

    public long getPositionMs() {
        return Math.max(0L, positionMs);
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getValueText() {
        return valueText == null ? "" : valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public boolean is(PlayerAction target) {
        return action == target;
    }
}