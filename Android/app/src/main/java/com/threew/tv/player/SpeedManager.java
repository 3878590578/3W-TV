package com.threew.tv.player;

import android.content.Context;

import com.threew.tv.settings.SpeedSettings;

import java.util.Arrays;

/**
 * 3W影视播放速度管理器
 *
 * 支持：
 * 1. 全局正常播放速度
 * 2. 每部剧独立播放速度
 * 3. 长按临时倍速
 * 4. 长按结束后恢复原来的正常速度
 *
 * 支持倍速：
 * 1× / 1.5× / 2× / 2.5× / 3× / 5× / 8×
 *
 * 设计：
 * - 正常倍速可以保存
 * - 每部剧可以覆盖全局倍速
 * - 不保存单集倍速
 * - 长按倍速绝不覆盖正常倍速设置
 */
public class SpeedManager {

    private static final float DEFAULT_SPEED = 1.0f;
    private static final float DEFAULT_LONG_PRESS_SPEED = 2.0f;

    private static final float[] SPEEDS = {
            1.0f,
            1.5f,
            2.0f,
            2.5f,
            3.0f,
            5.0f,
            8.0f
    };

    private static final float[] LONG_PRESS_SPEEDS = {
            2.0f,
            3.0f,
            5.0f,
            8.0f
    };

    private final SpeedSettings settings;

    public SpeedManager(Context context) {
        settings =
                new SpeedSettings(
                        context.getApplicationContext()
                );
    }

    // ============================================================
    // 倍速列表
    // ============================================================

    public float[] getSupportedSpeeds() {
        return Arrays.copyOf(
                SPEEDS,
                SPEEDS.length
        );
    }

    public float[] getLongPressSpeeds() {
        return Arrays.copyOf(
                LONG_PRESS_SPEEDS,
                LONG_PRESS_SPEEDS.length
        );
    }

    public boolean isSupportedSpeed(float speed) {

        for (float value : SPEEDS) {
            if (Math.abs(value - speed) < 0.01f) {
                return true;
            }
        }

        return false;
    }

    public boolean isSupportedLongPressSpeed(
            float speed
    ) {

        for (float value : LONG_PRESS_SPEEDS) {
            if (Math.abs(value - speed) < 0.01f) {
                return true;
            }
        }

        return false;
    }

    // ============================================================
    // 全局正常倍速
    // ============================================================

    public float getGlobalSpeed() {

        float speed =
                settings.getGlobalSpeed();

        return normalizeSpeed(speed);
    }

    public void setGlobalSpeed(float speed) {

        settings.setGlobalSpeed(
                normalizeSpeed(speed)
        );
    }

    // ============================================================
    // 每部剧正常倍速
    // ============================================================

    public float getSeriesSpeed(long videoId) {

        if (videoId <= 0) {
            return getGlobalSpeed();
        }

        float speed =
                settings.getSeriesSpeed(videoId);

        return normalizeSpeed(speed);
    }

    public void setSeriesSpeed(
            long videoId,
            float speed
    ) {

        if (videoId <= 0) {
            setGlobalSpeed(speed);
            return;
        }

        settings.setSeriesSpeed(
                videoId,
                normalizeSpeed(speed)
        );
    }

    public boolean hasSeriesSpeed(long videoId) {

        if (videoId <= 0) {
            return false;
        }

        return settings.hasSeriesSpeed(videoId);
    }

    public void clearSeriesSpeed(long videoId) {

        if (videoId <= 0) {
            return;
        }

        settings.clearSeriesSpeed(videoId);
    }

    // ============================================================
    // 播放器当前正常倍速
    // ============================================================

    /**
     * 获取当前视频应该使用的正常倍速。
     *
     * 如果设置过单剧倍速：
     * 使用单剧倍速。
     *
     * 否则：
     * 使用全局倍速。
     */
    public float getNormalSpeed(long videoId) {

        if (videoId > 0 &&
                hasSeriesSpeed(videoId)) {

            return getSeriesSpeed(videoId);
        }

        return getGlobalSpeed();
    }

    /**
     * 设置当前视频正常倍速。
     *
     * 这里保存到“单部剧”。
     */
    public void setNormalSpeed(
            long videoId,
            float speed
    ) {

        speed = normalizeSpeed(speed);

        if (videoId <= 0) {
            setGlobalSpeed(speed);
        } else {
            setSeriesSpeed(
                    videoId,
                    speed
            );
        }
    }

    // ============================================================
    // 长按临时倍速
    // ============================================================

    public float getLongPressSpeed() {

        float speed =
                settings.getLongPressSpeed();

        return normalizeLongPressSpeed(speed);
    }

    public void setLongPressSpeed(
            float speed
    ) {

        settings.setLongPressSpeed(
                normalizeLongPressSpeed(speed)
        );
    }

    /**
     * 开始长按时返回临时倍速。
     *
     * 这个值只用于播放器运行时，
     * 不会覆盖正常倍速。
     */
    public float beginLongPressSpeed(
            long videoId
    ) {

        return getLongPressSpeed();
    }

    /**
     * 长按结束后恢复正常倍速。
     */
    public float endLongPressSpeed(
            long videoId
    ) {

        return getNormalSpeed(videoId);
    }

    // ============================================================
    // 倍速调整
    // ============================================================

    /**
     * 获取当前倍速在列表中的位置。
     */
    public int getSpeedIndex(float speed) {

        speed = normalizeSpeed(speed);

        for (int i = 0; i < SPEEDS.length; i++) {

            if (Math.abs(
                    SPEEDS[i] - speed
            ) < 0.01f) {

                return i;
            }
        }

        return 0;
    }

    /**
     * 获取下一个倍速。
     */
    public float nextSpeed(float speed) {

        int index =
                getSpeedIndex(speed);

        if (index >= SPEEDS.length - 1) {
            return SPEEDS[SPEEDS.length - 1];
        }

        return SPEEDS[index + 1];
    }

    /**
     * 获取上一个倍速。
     */
    public float previousSpeed(float speed) {

        int index =
                getSpeedIndex(speed);

        if (index <= 0) {
            return SPEEDS[0];
        }

        return SPEEDS[index - 1];
    }

    /**
     * 长按倍速列表中获取下一个。
     */
    public float nextLongPressSpeed(
            float speed
    ) {

        int index =
                getLongPressSpeedIndex(speed);

        if (index >= LONG_PRESS_SPEEDS.length - 1) {
            return LONG_PRESS_SPEEDS[
                    LONG_PRESS_SPEEDS.length - 1
                    ];
        }

        return LONG_PRESS_SPEEDS[index + 1];
    }

    /**
     * 长按倍速列表中获取上一个。
     */
    public float previousLongPressSpeed(
            float speed
    ) {

        int index =
                getLongPressSpeedIndex(speed);

        if (index <= 0) {
            return LONG_PRESS_SPEEDS[0];
        }

        return LONG_PRESS_SPEEDS[index - 1];
    }

    private int getLongPressSpeedIndex(
            float speed
    ) {

        speed =
                normalizeLongPressSpeed(speed);

        for (int i = 0;
             i < LONG_PRESS_SPEEDS.length;
             i++) {

            if (Math.abs(
                    LONG_PRESS_SPEEDS[i] - speed
            ) < 0.01f) {

                return i;
            }
        }

        return 0;
    }

    // ============================================================
    // 快速设置
    // ============================================================

    public void setNormalSpeed1x(
            long videoId
    ) {
        setNormalSpeed(videoId, 1.0f);
    }

    public void setNormalSpeed15x(
            long videoId
    ) {
        setNormalSpeed(videoId, 1.5f);
    }

    public void setNormalSpeed2x(
            long videoId
    ) {
        setNormalSpeed(videoId, 2.0f);
    }

    public void setNormalSpeed25x(
            long videoId
    ) {
        setNormalSpeed(videoId, 2.5f);
    }

    public void setNormalSpeed3x(
            long videoId
    ) {
        setNormalSpeed(videoId, 3.0f);
    }

    public void setNormalSpeed5x(
            long videoId
    ) {
        setNormalSpeed(videoId, 5.0f);
    }

    public void setNormalSpeed8x(
            long videoId
    ) {
        setNormalSpeed(videoId, 8.0f);
    }

    // ============================================================
    // 显示文本
    // ============================================================

    public String formatSpeed(float speed) {

        speed = normalizeSpeed(speed);

        if (Math.abs(speed - 1.0f) < 0.01f) {
            return "1×";
        }

        if (Math.abs(speed - 1.5f) < 0.01f) {
            return "1.5×";
        }

        if (Math.abs(speed - 2.0f) < 0.01f) {
            return "2×";
        }

        if (Math.abs(speed - 2.5f) < 0.01f) {
            return "2.5×";
        }

        if (Math.abs(speed - 3.0f) < 0.01f) {
            return "3×";
        }

        if (Math.abs(speed - 5.0f) < 0.01f) {
            return "5×";
        }

        if (Math.abs(speed - 8.0f) < 0.01f) {
            return "8×";
        }

        return String.format(
                java.util.Locale.US,
                "%.1f×",
                speed
        );
    }

    // ============================================================
    // 重置
    // ============================================================

    public void reset() {
        settings.reset();
    }

    // ============================================================
    // 内部校正
    // ============================================================

    private float normalizeSpeed(float speed) {

        if (Float.isNaN(speed) ||
                Float.isInfinite(speed)) {

            return DEFAULT_SPEED;
        }

        float nearest =
                SPEEDS[0];

        float distance =
                Math.abs(
                        speed - nearest
                );

        for (float value : SPEEDS) {

            float currentDistance =
                    Math.abs(
                            speed - value
                    );

            if (currentDistance < distance) {
                distance = currentDistance;
                nearest = value;
            }
        }

        return nearest;
    }

    private float normalizeLongPressSpeed(
            float speed
    ) {

        if (Float.isNaN(speed) ||
                Float.isInfinite(speed)) {

            return DEFAULT_LONG_PRESS_SPEED;
        }

        float nearest =
                LONG_PRESS_SPEEDS[0];

        float distance =
                Math.abs(
                        speed - nearest
                );

        for (float value :
                LONG_PRESS_SPEEDS) {

            float currentDistance =
                    Math.abs(
                            speed - value
                    );

            if (currentDistance < distance) {
                distance = currentDistance;
                nearest = value;
            }
        }

        return nearest;
    }
}
