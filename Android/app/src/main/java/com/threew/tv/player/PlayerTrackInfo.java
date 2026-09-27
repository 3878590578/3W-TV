package com.threew.tv.player;

import androidx.media3.common.C;

import java.util.Locale;

/**
 * 播放器轨道信息
 *
 * 统一封装音频、字幕、视频轨道的信息，
 * 供播放器菜单、轨道选择器等模块使用。
 */
public class PlayerTrackInfo {

    public static final int TYPE_VIDEO = C.TRACK_TYPE_VIDEO;
    public static final int TYPE_AUDIO = C.TRACK_TYPE_AUDIO;
    public static final int TYPE_TEXT = C.TRACK_TYPE_TEXT;

    private final int trackType;
    private final int groupIndex;
    private final int trackIndex;

    private final String id;
    private final String language;
    private final String label;
    private final String mimeType;

    private final int width;
    private final int height;
    private final int bitrate;
    private final int channelCount;
    private final int sampleRate;

    private final boolean selected;
    private final boolean supported;

    public PlayerTrackInfo(
            int trackType,
            int groupIndex,
            int trackIndex,
            String id,
            String language,
            String label,
            String mimeType,
            int width,
            int height,
            int bitrate,
            int channelCount,
            int sampleRate,
            boolean selected,
            boolean supported
    ) {
        this.trackType = trackType;
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
        this.id = id;
        this.language = language;
        this.label = label;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.channelCount = channelCount;
        this.sampleRate = sampleRate;
        this.selected = selected;
        this.supported = supported;
    }

    public int getTrackType() {
        return trackType;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getLabel() {
        return label;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isVideo() {
        return trackType == TYPE_VIDEO;
    }

    public boolean isAudio() {
        return trackType == TYPE_AUDIO;
    }

    public boolean isText() {
        return trackType == TYPE_TEXT;
    }

    public String getDisplayName() {
        if (label != null && !label.trim().isEmpty()) {
            return label.trim();
        }

        if (isVideo()) {
            if (width > 0 && height > 0) {
                return width + "×" + height;
            }

            if (bitrate > 0) {
                return formatBitrate(bitrate);
            }

            return "视频";
        }

        if (isAudio()) {
            if (language != null && !language.trim().isEmpty()) {
                return languageName(language);
            }

            if (channelCount > 0) {
                return "音频 " + channelCount + " 声道";
            }

            return "音频";
        }

        if (isText()) {
            if (language != null && !language.trim().isEmpty()) {
                return languageName(language);
            }

            return "字幕";
        }

        return "未知轨道";
    }

    public String getDetailText() {
        if (isVideo()) {
            StringBuilder builder = new StringBuilder();

            if (width > 0 && height > 0) {
                builder.append(width)
                        .append("×")
                        .append(height);
            }

            if (bitrate > 0) {
                if (builder.length() > 0) {
                    builder.append(" · ");
                }
                builder.append(formatBitrate(bitrate));
            }

            return builder.toString();
        }

        if (isAudio()) {
            StringBuilder builder = new StringBuilder();

            if (channelCount > 0) {
                builder.append(channelCount).append(" 声道");
            }

            if (sampleRate > 0) {
                if (builder.length() > 0) {
                    builder.append(" · ");
                }

                builder.append(sampleRate / 1000)
                        .append(" kHz");
            }

            if (bitrate > 0) {
                if (builder.length() > 0) {
                    builder.append(" · ");
                }

                builder.append(formatBitrate(bitrate));
            }

            return builder.toString();
        }

        return mimeType == null ? "" : mimeType;
    }

    private String formatBitrate(int value) {
        if (value <= 0) {
            return "";
        }

        if (value >= 1_000_000) {
            return String.format(
                    Locale.US,
                    "%.1f Mbps",
                    value / 1_000_000f
            );
        }

        if (value >= 1_000) {
            return String.format(
                    Locale.US,
                    "%d kbps",
                    value / 1_000
            );
        }

        return value + " bps";
    }

    private String languageName(String value) {
        String languageValue = value.trim().toLowerCase(Locale.US);

        switch (languageValue) {
            case "zh":
            case "zh-cn":
            case "zh-hans":
                return "中文";

            case "zh-tw":
            case "zh-hk":
            case "zh-hant":
                return "繁體中文";

            case "en":
            case "en-us":
            case "en-gb":
                return "English";

            case "ja":
            case "ja-jp":
                return "日本語";

            case "ko":
            case "ko-kr":
                return "한국어";

            case "fr":
                return "Français";

            case "de":
                return "Deutsch";

            case "es":
                return "Español";

            case "ru":
                return "Русский";

            default:
                return value;
        }
    }

    @Override
    public String toString() {
        return "PlayerTrackInfo{" +
                "trackType=" + trackType +
                ", groupIndex=" + groupIndex +
                ", trackIndex=" + trackIndex +
                ", id='" + id + '\'' +
                ", language='" + language + '\'' +
                ", label='" + label + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", bitrate=" + bitrate +
                ", channelCount=" + channelCount +
                ", sampleRate=" + sampleRate +
                ", selected=" + selected +
                ", supported=" + supported +
                '}';
    }
}
