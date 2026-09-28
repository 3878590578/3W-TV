package com.threew.tv.player;

/**
 * 进度拖动时的预览信息。
 */
public class PlayerSeekPreview {

    private long positionMs;
    private long durationMs;
    private boolean visible;

    public PlayerSeekPreview() {
        visible = false;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getProgress() {
        if (durationMs <= 0L) {
            return 0f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        (float) positionMs / durationMs
                )
        );
    }

    public void show(long positionMs, long durationMs) {
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
        this.visible = true;
    }

    public void hide() {
        visible = false;
    }
}