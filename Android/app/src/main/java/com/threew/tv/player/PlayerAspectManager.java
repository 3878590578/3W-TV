package com.threew.tv.player;

import androidx.media3.ui.AspectRatioFrameLayout;

/**
 * 播放器画面比例管理器。
 *
 * 支持：
 * 自动、适应、填充、裁剪、原始比例、16:9、4:3。
 */
public class PlayerAspectManager {

    public static final int MODE_FIT = 0;
    public static final int MODE_FILL = 1;
    public static final int MODE_CROP = 2;
    public static final int MODE_ORIGINAL = 3;
    public static final int MODE_16_9 = 4;
    public static final int MODE_4_3 = 5;

    private int mode = MODE_FIT;

    public PlayerAspectManager() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (!isValidMode(mode)) {
            return;
        }

        this.mode = mode;
    }

    public void setFit() {
        mode = MODE_FIT;
    }

    public void setFill() {
        mode = MODE_FILL;
    }

    public void setCrop() {
        mode = MODE_CROP;
    }

    public void setOriginal() {
        mode = MODE_ORIGINAL;
    }

    public void set16x9() {
        mode = MODE_16_9;
    }

    public void set4x3() {
        mode = MODE_4_3;
    }

    /**
     * 将当前模式应用到 PlayerView。
     */
    public void apply(AspectRatioFrameLayout frameLayout) {
        if (frameLayout == null) {
            return;
        }

        switch (mode) {
            case MODE_FILL:
                frameLayout.setResizeMode(
                        AspectRatioFrameLayout.RESIZE_MODE_FILL
                );
                break;

            case MODE_CROP:
                frameLayout.setResizeMode(
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                );
                break;

            case MODE_ORIGINAL:
            case MODE_FIT:
            case MODE_16_9:
            case MODE_4_3:
            default:
                frameLayout.setResizeMode(
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                );
                break;
        }
    }

    /**
     * 根据模式名称返回显示名称。
     */
    public String getDisplayName() {
        switch (mode) {
            case MODE_FILL:
                return "填充";

            case MODE_CROP:
                return "裁剪";

            case MODE_ORIGINAL:
                return "原始比例";

            case MODE_16_9:
                return "16:9";

            case MODE_4_3:
                return "4:3";

            case MODE_FIT:
            default:
                return "显示全部画面";
        }
    }

    /**
     * 返回播放器可直接使用的 ResizeMode。
     */
    public int getResizeMode() {
        switch (mode) {
            case MODE_FILL:
                return AspectRatioFrameLayout.RESIZE_MODE_FILL;

            case MODE_CROP:
                return AspectRatioFrameLayout.RESIZE_MODE_ZOOM;

            case MODE_FIT:
            case MODE_ORIGINAL:
            case MODE_16_9:
            case MODE_4_3:
            default:
                return AspectRatioFrameLayout.RESIZE_MODE_FIT;
        }
    }

    /**
     * 判断模式是否有效。
     */
    public boolean isValidMode(int value) {
        return value == MODE_FIT
                || value == MODE_FILL
                || value == MODE_CROP
                || value == MODE_ORIGINAL
                || value == MODE_16_9
                || value == MODE_4_3;
    }

    /**
     * 获取所有模式。
     */
    public static int[] getAllModes() {
        return new int[]{
                MODE_FIT,
                MODE_FILL,
                MODE_CROP,
                MODE_ORIGINAL,
                MODE_16_9,
                MODE_4_3
        };
    }

    /**
     * 根据名称转换模式。
     */
    public static int fromName(String name) {
        if (name == null) {
            return MODE_FIT;
        }

        String value = name.trim();

        if ("填充".equals(value)) {
            return MODE_FILL;
        }

        if ("裁剪".equals(value)) {
            return MODE_CROP;
        }

        if ("原始比例".equals(value)) {
            return MODE_ORIGINAL;
        }

        if ("16:9".equals(value)) {
            return MODE_16_9;
        }

        if ("4:3".equals(value)) {
            return MODE_4_3;
        }

        return MODE_FIT;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
