package com.threew.tv.player;

import android.content.Context;
import android.content.SharedPreferences;

import com.threew.tv.model.Video;

import java.util.HashMap;
import java.util.Map;

/**
 * 3W影视片头 / 片尾跳过管理器
 *
 * 规则：
 * 1. 全局开关
 * 2. 每部剧可以单独覆盖全局设置
 * 3. 不保存到单集
 * 4. 片头记录的是“片头结束时间”
 * 5. 片尾记录的是“距离结尾还剩多少毫秒”
 *
 * 播放时：
 * - position < introEndMs -> 显示/执行片头跳过
 * - duration - position <= outroDurationMs -> 进入片尾区域
 */
public class SkipManager {

    private static final String PREFS =
            "threew_skip_settings";

    private static final String KEY_GLOBAL_INTRO =
            "global_intro_enabled";

    private static final String KEY_GLOBAL_OUTRO =
            "global_outro_enabled";

    private static final String KEY_GLOBAL_INTRO_END =
            "global_intro_end_ms";

    private static final String KEY_GLOBAL_OUTRO_DURATION =
            "global_outro_duration_ms";

    private static final String KEY_SERIES_PREFIX =
            "series_";

    private static final String SUFFIX_INTRO_ENABLED =
            "_intro_enabled";

    private static final String SUFFIX_OUTRO_ENABLED =
            "_outro_enabled";

    private static final String SUFFIX_INTRO_END =
            "_intro_end_ms";

    private static final String SUFFIX_OUTRO_DURATION =
            "_outro_duration_ms";

    private final SharedPreferences preferences;

    public SkipManager(Context context) {

        Context appContext =
                context.getApplicationContext();

        preferences =
                appContext.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    // ============================================================
    // 全局开关
    // ============================================================

    public boolean isGlobalIntroEnabled() {
        return preferences.getBoolean(
                KEY_GLOBAL_INTRO,
                true
        );
    }

    public void setGlobalIntroEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(
                        KEY_GLOBAL_INTRO,
                        enabled
                )
                .apply();
    }

    public boolean isGlobalOutroEnabled() {
        return preferences.getBoolean(
                KEY_GLOBAL_OUTRO,
                true
        );
    }

    public void setGlobalOutroEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(
                        KEY_GLOBAL_OUTRO,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 全局片头 / 片尾时间
    // ============================================================

    public long getGlobalIntroEndMs() {
        return Math.max(
                0L,
                preferences.getLong(
                        KEY_GLOBAL_INTRO_END,
                        0L
                )
        );
    }

    public void setGlobalIntroEndMs(long milliseconds) {

        preferences.edit()
                .putLong(
                        KEY_GLOBAL_INTRO_END,
                        Math.max(0L, milliseconds)
                )
                .apply();
    }

    public long getGlobalOutroDurationMs() {
        return Math.max(
                0L,
                preferences.getLong(
                        KEY_GLOBAL_OUTRO_DURATION,
                        0L
                )
        );
    }

    public void setGlobalOutroDurationMs(
            long milliseconds
    ) {

        preferences.edit()
                .putLong(
                        KEY_GLOBAL_OUTRO_DURATION,
                        Math.max(0L, milliseconds)
                )
                .apply();
    }

    // ============================================================
    // 单部剧设置
    // ============================================================

    public void setSeriesIntroEnabled(
            long videoId,
            boolean enabled
    ) {

        if (videoId <= 0) {
            return;
        }

        preferences.edit()
                .putBoolean(
                        seriesKey(
                                videoId,
                                SUFFIX_INTRO_ENABLED
                        ),
                        enabled
                )
                .apply();
    }

    public void setSeriesOutroEnabled(
            long videoId,
            boolean enabled
    ) {

        if (videoId <= 0) {
            return;
        }

        preferences.edit()
                .putBoolean(
                        seriesKey(
                                videoId,
                                SUFFIX_OUTRO_ENABLED
                        ),
                        enabled
                )
                .apply();
    }

    public boolean hasSeriesIntroSetting(
            long videoId
    ) {

        return videoId > 0 &&
                preferences.contains(
                        seriesKey(
                                videoId,
                                SUFFIX_INTRO_ENABLED
                        )
                );
    }

    public boolean hasSeriesOutroSetting(
            long videoId
    ) {

        return videoId > 0 &&
                preferences.contains(
                        seriesKey(
                                videoId,
                                SUFFIX_OUTRO_ENABLED
                        )
                );
    }

    public boolean isSeriesIntroEnabled(
            long videoId
    ) {

        if (videoId <= 0) {
            return isGlobalIntroEnabled();
        }

        String key =
                seriesKey(
                        videoId,
                        SUFFIX_INTRO_ENABLED
                );

        if (!preferences.contains(key)) {
            return isGlobalIntroEnabled();
        }

        return preferences.getBoolean(
                key,
                isGlobalIntroEnabled()
        );
    }

    public boolean isSeriesOutroEnabled(
            long videoId
    ) {

        if (videoId <= 0) {
            return isGlobalOutroEnabled();
        }

        String key =
                seriesKey(
                        videoId,
                        SUFFIX_OUTRO_ENABLED
                );

        if (!preferences.contains(key)) {
            return isGlobalOutroEnabled();
        }

        return preferences.getBoolean(
                key,
                isGlobalOutroEnabled()
        );
    }

    // ============================================================
    // 单部剧片头时间
    // ============================================================

    public long getSeriesIntroEndMs(
            long videoId
    ) {

        if (videoId <= 0) {
            return getGlobalIntroEndMs();
        }

        String key =
                seriesKey(
                        videoId,
                        SUFFIX_INTRO_END
                );

        if (!preferences.contains(key)) {
            return getGlobalIntroEndMs();
        }

        return Math.max(
                0L,
                preferences.getLong(
                        key,
                        getGlobalIntroEndMs()
                )
        );
    }

    public void setSeriesIntroEndMs(
            long videoId,
            long milliseconds
    ) {

        if (videoId <= 0) {
            setGlobalIntroEndMs(milliseconds);
            return;
        }

        preferences.edit()
                .putLong(
                        seriesKey(
                                videoId,
                                SUFFIX_INTRO_END
                        ),
                        Math.max(0L, milliseconds)
                )
                .apply();
    }

    // ============================================================
    // 单部剧片尾时间
    // ============================================================

    public long getSeriesOutroDurationMs(
            long videoId
    ) {

        if (videoId <= 0) {
            return getGlobalOutroDurationMs();
        }

        String key =
                seriesKey(
                        videoId,
                        SUFFIX_OUTRO_DURATION
                );

        if (!preferences.contains(key)) {
            return getGlobalOutroDurationMs();
        }

        return Math.max(
                0L,
                preferences.getLong(
                        key,
                        getGlobalOutroDurationMs()
                )
        );
    }

    public void setSeriesOutroDurationMs(
            long videoId,
            long milliseconds
    ) {

        if (videoId <= 0) {
            setGlobalOutroDurationMs(milliseconds);
            return;
        }

        preferences.edit()
                .putLong(
                        seriesKey(
                                videoId,
                                SUFFIX_OUTRO_DURATION
                        ),
                        Math.max(0L, milliseconds)
                )
                .apply();
    }

    // ============================================================
    // 从当前播放位置记录片头
    // ============================================================

    /**
     * 点击“片头”按钮时调用。
     *
     * 例如当前播放到 01:32，
     * 则保存 92 秒作为片头结束位置。
     */
    public void markIntro(
            long videoId,
            long currentPositionMs
    ) {

        long position =
                Math.max(0L, currentPositionMs);

        if (videoId <= 0) {
            setGlobalIntroEndMs(position);
        } else {
            setSeriesIntroEndMs(
                    videoId,
                    position
            );
        }
    }

    /**
     * 记录片头结束位置并同时打开片头跳过。
     */
    public void markIntroAndEnable(
            long videoId,
            long currentPositionMs
    ) {

        markIntro(
                videoId,
                currentPositionMs
        );

        if (videoId <= 0) {
            setGlobalIntroEnabled(true);
        } else {
            setSeriesIntroEnabled(
                    videoId,
                    true
            );
        }
    }

    // ============================================================
    // 从当前播放位置记录片尾
    // ============================================================

    /**
     * 点击“片尾”按钮时调用。
     *
     * 例如：
     * 视频总长度 45:00
     * 当前播放 43:20
     *
     * 片尾长度 = 1:40。
     */
    public void markOutro(
            long videoId,
            long currentPositionMs,
            long durationMs
    ) {

        if (durationMs <= 0) {
            return;
        }

        long position =
                Math.max(
                        0L,
                        Math.min(
                                currentPositionMs,
                                durationMs
                        )
                );

        long outroDuration =
                Math.max(
                        0L,
                        durationMs - position
                );

        if (videoId <= 0) {
            setGlobalOutroDurationMs(
                    outroDuration
            );
        } else {
            setSeriesOutroDurationMs(
                    videoId,
                    outroDuration
            );
        }
    }

    /**
     * 记录片尾并同时打开片尾跳过。
     */
    public void markOutroAndEnable(
            long videoId,
            long currentPositionMs,
            long durationMs
    ) {

        markOutro(
                videoId,
                currentPositionMs,
                durationMs
        );

        if (videoId <= 0) {
            setGlobalOutroEnabled(true);
        } else {
            setSeriesOutroEnabled(
                    videoId,
                    true
            );
        }
    }

    // ============================================================
    // 播放时判断
    // ============================================================

    /**
     * 当前时间是否位于片头区域。
     */
    public boolean isInIntro(
            long videoId,
            long positionMs,
            long durationMs
    ) {

        if (!isSeriesIntroEnabled(videoId)) {
            return false;
        }

        long introEnd =
                getSeriesIntroEndMs(videoId);

        if (introEnd <= 0) {
            return false;
        }

        if (positionMs < 0) {
            return false;
        }

        if (durationMs > 0 &&
                introEnd >= durationMs) {

            return false;
        }

        return positionMs < introEnd;
    }

    /**
     * 当前时间是否位于片尾区域。
     */
    public boolean isInOutro(
            long videoId,
            long positionMs,
            long durationMs
    ) {

        if (!isSeriesOutroEnabled(videoId)) {
            return false;
        }

        if (durationMs <= 0 ||
                positionMs < 0) {

            return false;
        }

        long outroDuration =
                getSeriesOutroDurationMs(videoId);

        if (outroDuration <= 0) {
            return false;
        }

        long outroStart =
                durationMs - outroDuration;

        if (outroStart < 0) {
            outroStart = 0;
        }

        return positionMs >= outroStart &&
                positionMs < durationMs;
    }

    /**
     * 获取片头跳转位置。
     */
    public long getIntroSkipPosition(
            long videoId,
            long durationMs
    ) {

        long introEnd =
                getSeriesIntroEndMs(videoId);

        if (durationMs > 0) {
            introEnd =
                    Math.min(
                            introEnd,
                            durationMs
                    );
        }

        return Math.max(
                0L,
                introEnd
        );
    }

    /**
     * 获取片尾开始位置。
     */
    public long getOutroStartPosition(
            long videoId,
            long durationMs
    ) {

        if (durationMs <= 0) {
            return 0L;
        }

        long outroDuration =
                getSeriesOutroDurationMs(videoId);

        if (outroDuration <= 0) {
            return durationMs;
        }

        return Math.max(
                0L,
                durationMs - outroDuration
        );
    }

    // ============================================================
    // 根据 Video 读取设置
    // ============================================================

    public boolean isIntroEnabled(Video video) {

        if (video == null) {
            return isGlobalIntroEnabled();
        }

        return isSeriesIntroEnabled(
                video.getId()
        );
    }

    public boolean isOutroEnabled(Video video) {

        if (video == null) {
            return isGlobalOutroEnabled();
        }

        return isSeriesOutroEnabled(
                video.getId()
        );
    }

    public long getIntroEndMs(Video video) {

        if (video == null) {
            return getGlobalIntroEndMs();
        }

        return getSeriesIntroEndMs(
                video.getId()
        );
    }

    public long getOutroDurationMs(Video video) {

        if (video == null) {
            return getGlobalOutroDurationMs();
        }

        return getSeriesOutroDurationMs(
                video.getId()
        );
    }

    // ============================================================
    // 删除单部剧设置
    // ============================================================

    public void clearSeriesSettings(
            long videoId
    ) {

        if (videoId <= 0) {
            return;
        }

        preferences.edit()
                .remove(
                        seriesKey(
                                videoId,
                                SUFFIX_INTRO_ENABLED
                        )
                )
                .remove(
                        seriesKey(
                                videoId,
                                SUFFIX_OUTRO_ENABLED
                        )
                )
                .remove(
                        seriesKey(
                                videoId,
                                SUFFIX_INTRO_END
                        )
                )
                .remove(
                        seriesKey(
                                videoId,
                                SUFFIX_OUTRO_DURATION
                        )
                )
                .apply();
    }

    /**
     * 清空全部片头片尾设置。
     */
    public void reset() {

        preferences.edit()
                .clear()
                .apply();
    }

    /**
     * 导出当前设置。
     */
    public Map<String, ?> getAllSettings() {

        return new HashMap<>(
                preferences.getAll()
        );
    }

    // ============================================================
    // 内部
    // ============================================================

    private String seriesKey(
            long videoId,
            String suffix
    ) {

        return KEY_SERIES_PREFIX +
                videoId +
                suffix;
    }
}
