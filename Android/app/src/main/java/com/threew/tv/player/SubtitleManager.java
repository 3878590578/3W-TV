package com.threew.tv.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放器字幕管理器。
 *
 * 负责保存和读取播放器字幕相关设置：
 * - 字幕开关
 * - 首选字幕语言
 * - 当前字幕轨道
 * - 外挂字幕
 * - 系统语言跟随
 * - 字幕字体缩放
 *
 * 实际字幕轨道选择由播放器负责。
 */
public class SubtitleManager {

    private static final String PREF_NAME =
            "threew_subtitle_settings";

    private static final String KEY_ENABLED =
            "enabled";

    private static final String KEY_LANGUAGE =
            "language";

    private static final String KEY_TRACK_ID =
            "track_id";

    private static final String KEY_EXTERNAL_URI =
            "external_uri";

    private static final String KEY_EXTERNAL_NAME =
            "external_name";

    private static final String KEY_FOLLOW_SYSTEM =
            "follow_system";

    private static final String KEY_FONT_SCALE =
            "font_scale";

    private final SharedPreferences preferences;

    public SubtitleManager(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREF_NAME,
                                Context.MODE_PRIVATE
                        );
    }

    public boolean isEnabled() {
        return preferences.getBoolean(
                KEY_ENABLED,
                true
        );
    }

    public void setEnabled(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_ENABLED,
                        enabled
                )
                .apply();
    }

    public boolean toggleEnabled() {
        boolean enabled =
                !isEnabled();

        setEnabled(enabled);

        return enabled;
    }

    public String getPreferredLanguage() {
        return preferences.getString(
                KEY_LANGUAGE,
                ""
        );
    }

    public void setPreferredLanguage(
            String language
    ) {
        if (language == null) {
            language = "";
        }

        preferences.edit()
                .putString(
                        KEY_LANGUAGE,
                        language
                )
                .apply();
    }

    public boolean isFollowSystemLanguage() {
        return preferences.getBoolean(
                KEY_FOLLOW_SYSTEM,
                true
        );
    }

    public void setFollowSystemLanguage(
            boolean follow
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_FOLLOW_SYSTEM,
                        follow
                )
                .apply();
    }

    public String getSelectedTrackId() {
        return preferences.getString(
                KEY_TRACK_ID,
                ""
        );
    }

    public void setSelectedTrackId(
            String trackId
    ) {
        if (trackId == null) {
            trackId = "";
        }

        preferences.edit()
                .putString(
                        KEY_TRACK_ID,
                        trackId
                )
                .apply();
    }

    public void clearSelectedTrack() {
        preferences.edit()
                .remove(KEY_TRACK_ID)
                .apply();
    }

    public String getExternalSubtitleUri() {
        return preferences.getString(
                KEY_EXTERNAL_URI,
                ""
        );
    }

    public Uri getExternalSubtitleUriObject() {
        String value =
                getExternalSubtitleUri();

        if (TextUtils.isEmpty(value)) {
            return null;
        }

        try {
            return Uri.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public void setExternalSubtitleUri(
            Uri uri
    ) {
        preferences.edit()
                .putString(
                        KEY_EXTERNAL_URI,
                        uri == null
                                ? ""
                                : uri.toString()
                )
                .apply();
    }

    public String getExternalSubtitleName() {
        return preferences.getString(
                KEY_EXTERNAL_NAME,
                ""
        );
    }

    public void setExternalSubtitleName(
            String name
    ) {
        if (name == null) {
            name = "";
        }

        preferences.edit()
                .putString(
                        KEY_EXTERNAL_NAME,
                        name
                )
                .apply();
    }

    public void setExternalSubtitle(
            Uri uri,
            String name
    ) {
        preferences.edit()
                .putString(
                        KEY_EXTERNAL_URI,
                        uri == null
                                ? ""
                                : uri.toString()
                )
                .putString(
                        KEY_EXTERNAL_NAME,
                        name == null
                                ? ""
                                : name
                )
                .apply();
    }

    public boolean hasExternalSubtitle() {
        return !TextUtils.isEmpty(
                getExternalSubtitleUri()
        );
    }

    public void clearExternalSubtitle() {
        preferences.edit()
                .remove(KEY_EXTERNAL_URI)
                .remove(KEY_EXTERNAL_NAME)
                .apply();
    }

    public float getFontScale() {
        float value =
                preferences.getFloat(
                        KEY_FONT_SCALE,
                        1.0f
                );

        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            return 1.0f;
        }

        return Math.max(
                0.5f,
                Math.min(
                        2.0f,
                        value
                )
        );
    }

    public void setFontScale(
            float scale
    ) {
        if (Float.isNaN(scale)
                || Float.isInfinite(scale)) {
            scale = 1.0f;
        }

        scale = Math.max(
                0.5f,
                Math.min(
                        2.0f,
                        scale
                )
        );

        preferences.edit()
                .putFloat(
                        KEY_FONT_SCALE,
                        scale
                )
                .apply();
    }

    public float increaseFontScale() {
        float scale =
                getFontScale() + 0.1f;

        setFontScale(scale);

        return getFontScale();
    }

    public float decreaseFontScale() {
        float scale =
                getFontScale() - 0.1f;

        setFontScale(scale);

        return getFontScale();
    }

    public String findPreferredLanguage(
            List<String> languages
    ) {
        if (languages == null
                || languages.isEmpty()) {
            return "";
        }

        String preferred =
                getPreferredLanguage();

        if (!TextUtils.isEmpty(preferred)) {

            for (String language : languages) {

                if (TextUtils.isEmpty(language)) {
                    continue;
                }

                if (preferred.equalsIgnoreCase(
                        language
                )) {
                    return language;
                }
            }

            String preferredLower =
                    preferred.toLowerCase();

            for (String language : languages) {

                if (TextUtils.isEmpty(language)) {
                    continue;
                }

                if (language.toLowerCase()
                        .startsWith(preferredLower)) {
                    return language;
                }
            }
        }

        if (isFollowSystemLanguage()) {
            return languages.get(0);
        }

        return "";
    }

    public List<String> normalizeLanguages(
            List<String> languages
    ) {
        List<String> result =
                new ArrayList<>();

        if (languages == null) {
            return result;
        }

        for (String language : languages) {

            if (TextUtils.isEmpty(language)) {
                continue;
            }

            boolean exists = false;

            for (String current : result) {

                if (current.equalsIgnoreCase(
                        language
                )) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                result.add(language);
            }
        }

        return result;
    }

    public SubtitleState getState() {
        return new SubtitleState(
                isEnabled(),
                getPreferredLanguage(),
                getSelectedTrackId(),
                getExternalSubtitleUri(),
                getExternalSubtitleName(),
                isFollowSystemLanguage(),
                getFontScale()
        );
    }

    public void reset() {
        preferences.edit()
                .clear()
                .apply();
    }

    public static class SubtitleState {

        private final boolean enabled;
        private final String preferredLanguage;
        private final String selectedTrackId;
        private final String externalUri;
        private final String externalName;
        private final boolean followSystemLanguage;
        private final float fontScale;

        public SubtitleState(
                boolean enabled,
                String preferredLanguage,
                String selectedTrackId,
                String externalUri,
                String externalName,
                boolean followSystemLanguage,
                float fontScale
        ) {
            this.enabled = enabled;
            this.preferredLanguage =
                    preferredLanguage;
            this.selectedTrackId =
                    selectedTrackId;
            this.externalUri =
                    externalUri;
            this.externalName =
                    externalName;
            this.followSystemLanguage =
                    followSystemLanguage;
            this.fontScale =
                    fontScale;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public String getSelectedTrackId() {
            return selectedTrackId;
        }

        public String getExternalUri() {
            return externalUri;
        }

        public String getExternalName() {
            return externalName;
        }

        public boolean isFollowSystemLanguage() {
            return followSystemLanguage;
        }

        public float getFontScale() {
            return fontScale;
        }
    }
}
