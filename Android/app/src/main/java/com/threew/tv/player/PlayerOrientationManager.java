package com.threew.tv.player;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Surface;

import java.util.Locale;

/**
 * 播放器屏幕方向管理器。
 *
 * 支持：
 * 1. 自动跟随设备；
 * 2. 竖屏；
 * 3. 横屏；
 * 4. 根据设备当前方向自动判断。
 */
public class PlayerOrientationManager {

    public static final int MODE_AUTO = 0;
    public static final int MODE_PORTRAIT = 1;
    public static final int MODE_LANDSCAPE = 2;

    private int mode = MODE_AUTO;

    private Activity activity;

    public PlayerOrientationManager() {
    }

    public PlayerOrientationManager(Activity activity) {
        this.activity = activity;
    }

    public void attachActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getMode() {
        return mode;
    }

    /**
     * 设置方向模式。
     */
    public void setMode(int mode) {
        if (!isValidMode(mode)) {
            return;
        }

        this.mode = mode;
        apply();
    }

    public void setAuto() {
        setMode(MODE_AUTO);
    }

    public void setPortrait() {
        setMode(MODE_PORTRAIT);
    }

    public void setLandscape() {
        setMode(MODE_LANDSCAPE);
    }

    /**
     * 应用当前方向。
     */
    public void apply() {
        if (activity == null) {
            return;
        }

        switch (mode) {
            case MODE_PORTRAIT:
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                );
                break;

            case MODE_LANDSCAPE:
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                );
                break;

            case MODE_AUTO:
            default:
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                );
                break;
        }
    }

    /**
     * 临时切换到横屏。
     */
    public void enterLandscape() {
        if (activity == null) {
            return;
        }

        activity.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );
    }

    /**
     * 临时切换到竖屏。
     */
    public void enterPortrait() {
        if (activity == null) {
            return;
        }

        activity.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );
    }

    /**
     * 判断当前屏幕是否横屏。
     */
    public boolean isLandscape() {
        if (activity == null) {
            return false;
        }

        Configuration configuration =
                activity.getResources().getConfiguration();

        return configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 判断当前屏幕是否竖屏。
     */
    public boolean isPortrait() {
        if (activity == null) {
            return false;
        }

        Configuration configuration =
                activity.getResources().getConfiguration();

        return configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 获取设备当前旋转角度。
     */
    public int getCurrentRotation() {
        if (activity == null) {
            return Surface.ROTATION_0;
        }

        return activity
                .getWindowManager()
                .getDefaultDisplay()
                .getRotation();
    }

    /**
     * 自动判断当前设备方向。
     */
    public int getCurrentOrientationMode() {
        return isLandscape()
                ? MODE_LANDSCAPE
                : MODE_PORTRAIT;
    }

    /**
     * 获取方向显示名称。
     */
    public String getDisplayName() {
        switch (mode) {
            case MODE_PORTRAIT:
                return "竖屏";

            case MODE_LANDSCAPE:
                return "横屏";

            case MODE_AUTO:
            default:
                return "自动";
        }
    }

    /**
     * 根据名称转换方向模式。
     */
    public static int fromName(String name) {
        if (name == null) {
            return MODE_AUTO;
        }

        String value = name.trim()
                .toLowerCase(Locale.CHINA);

        if ("竖屏".equals(value)
                || "portrait".equals(value)) {
            return MODE_PORTRAIT;
        }

        if ("横屏".equals(value)
                || "landscape".equals(value)) {
            return MODE_LANDSCAPE;
        }

        return MODE_AUTO;
    }

    public boolean isValidMode(int value) {
        return value == MODE_AUTO
                || value == MODE_PORTRAIT
                || value == MODE_LANDSCAPE;
    }

    /**
     * 获取所有方向模式。
     */
    public static int[] getAllModes() {
        return new int[]{
                MODE_AUTO,
                MODE_PORTRAIT,
                MODE_LANDSCAPE
        };
    }

    /**
     * 播放器关闭时恢复自动方向。
     */
    public void release() {
        if (activity != null) {
            activity.setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            );
        }

        activity = null;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
