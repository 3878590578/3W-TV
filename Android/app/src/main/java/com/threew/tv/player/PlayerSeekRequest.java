package com.threew.tv.player;

public class PlayerSeekRequest {

    public static final int RELATIVE = 0;
    public static final int ABSOLUTE = 1;

    private final long positionMs;
    private final int type;

    public PlayerSeekRequest(long positionMs, int type) {
        this.positionMs = Math.max(0L, positionMs);
        this.type = type == ABSOLUTE ? ABSOLUTE : RELATIVE;
    }

    public static PlayerSeekRequest absolute(long positionMs) {
        return new PlayerSeekRequest(positionMs, ABSOLUTE);
    }

    public static PlayerSeekRequest relative(long deltaMs) {
        return new PlayerSeekRequest(deltaMs, RELATIVE);
    }

    public long getPositionMs() {
        return positionMs;
    }

    public int getType() {
        return type;
    }

    public boolean isAbsolute() {
        return type == ABSOLUTE;
    }

    public boolean isRelative() {
        return type == RELATIVE;
    }
}