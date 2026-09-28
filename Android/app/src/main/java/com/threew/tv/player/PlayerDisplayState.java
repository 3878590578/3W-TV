package com.threew.tv.player;

/**
 * 播放画面显示状态。
 */
public class PlayerDisplayState {

    public static final String FIT = "fit";
    public static final String FILL = "fill";
    public static final String CROP = "crop";
    public static final String ORIGINAL = "original";
    public static final String RATIO_16_9 = "16:9";
    public static final String RATIO_4_3 = "4:3";

    private String mode;

    public PlayerDisplayState() {
        this(FIT);
    }

    public PlayerDisplayState(String mode) {
        setMode(mode);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        if (FIT.equals(mode)
                || FILL.equals(mode)
                || CROP.equals(mode)
                || ORIGINAL.equals(mode)
                || RATIO_16_9.equals(mode)
                || RATIO_4_3.equals(mode)) {
            this.mode = mode;
        } else {
            this.mode = FIT;
        }
    }

    public boolean isFit() {
        return FIT.equals(mode);
    }

    public boolean isFill() {
        return FILL.equals(mode);
    }

    public boolean isCrop() {
        return CROP.equals(mode);
    }

    public boolean isOriginal() {
        return ORIGINAL.equals(mode);
    }

    public PlayerDisplayState copy() {
        return new PlayerDisplayState(mode);
    }
}