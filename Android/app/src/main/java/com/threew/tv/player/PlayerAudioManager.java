package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 播放器音频轨道管理器。
 *
 * 负责：
 * 1. 获取当前视频的音频轨道；
 * 2. 获取语言、声道、采样率、码率等信息；
 * 3. 记录当前音频轨道；
 * 4. 提供音频轨道选择所需的数据。
 *
 * 实际轨道切换由 TrackSelectorManager 负责。
 */
public class PlayerAudioManager {

    private ExoPlayer player;

    private String selectedLanguage;

    private int selectedGroupIndex = -1;

    private int selectedTrackIndex = -1;

    public PlayerAudioManager() {
    }

    public PlayerAudioManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
        refreshSelectedTrack();
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * 获取全部音频轨道。
     */
    public List<PlayerTrackInfo> getAudioTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        return new PlayerTrackManager(player).getAudioTracks();
    }

    /**
     * 获取当前选中的音频轨道。
     */
    public PlayerTrackInfo getSelectedTrack() {
        List<PlayerTrackInfo> tracks = getAudioTracks();

        for (PlayerTrackInfo track : tracks) {
            if (track.isSelected()) {
                return track;
            }
        }

        return null;
    }

    /**
     * 刷新当前选中音频轨道。
     */
    public void refreshSelectedTrack() {
        PlayerTrackInfo track = getSelectedTrack();

        if (track == null) {
            selectedGroupIndex = -1;
            selectedTrackIndex = -1;
            return;
        }

        selectedGroupIndex = track.getGroupIndex();
        selectedTrackIndex = track.getTrackIndex();

        String language = track.getLanguage();

        if (language != null && !language.trim().isEmpty()) {
            selectedLanguage = language;
        }
    }

    /**
     * 当前音频语言。
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * 手动记录音频语言。
     */
    public void setSelectedLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            selectedLanguage = null;
            return;
        }

        selectedLanguage = language.trim();
    }

    public int getSelectedGroupIndex() {
        return selectedGroupIndex;
    }

    public int getSelectedTrackIndex() {
        return selectedTrackIndex;
    }

    /**
     * 是否存在音频轨道。
     */
    public boolean hasAudioTracks() {
        return !getAudioTracks().isEmpty();
    }

    /**
     * 是否存在多个音频轨道。
     */
    public boolean hasMultipleAudioTracks() {
        return getAudioTracks().size() > 1;
    }

    /**
     * 获取所有音频语言。
     */
    public List<String> getAudioLanguages() {
        List<PlayerTrackInfo> tracks = getAudioTracks();

        if (tracks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (PlayerTrackInfo track : tracks) {
            String language = track.getLanguage();

            if (language == null || language.trim().isEmpty()) {
                continue;
            }

            if (!result.contains(language)) {
                result.add(language);
            }
        }

        return result;
    }

    /**
     * 根据语言寻找音频轨道。
     */
    public PlayerTrackInfo findByLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return null;
        }

        String target = language.trim()
                .toLowerCase(Locale.US);

        List<PlayerTrackInfo> tracks = getAudioTracks();

        for (PlayerTrackInfo track : tracks) {
            String value = track.getLanguage();

            if (value == null) {
                continue;
            }

            if (value.trim().toLowerCase(Locale.US).equals(target)) {
                return track;
            }
        }

        return null;
    }

    /**
     * 根据轨道索引寻找音频轨道。
     */
    public PlayerTrackInfo findTrack(
            int groupIndex,
            int trackIndex
    ) {
        List<PlayerTrackInfo> tracks = getAudioTracks();

        for (PlayerTrackInfo track : tracks) {
            if (track.getGroupIndex() == groupIndex
                    && track.getTrackIndex() == trackIndex) {
                return track;
            }
        }

        return null;
    }

    /**
     * 获取音频轨道显示名称。
     */
    public String getDisplayName(PlayerTrackInfo track) {
        if (track == null) {
            return "未知音频";
        }

        String label = track.getLabel();

        if (label != null && !label.trim().isEmpty()) {
            return label.trim();
        }

        String language = track.getLanguage();

        if (language != null && !language.trim().isEmpty()) {
            return languageName(language);
        }

        if (track.getChannelCount() > 0) {
            return track.getChannelCount() + " 声道";
        }

        return "音频";
    }

    /**
     * 获取音频轨道详细信息。
     */
    public String getDetail(PlayerTrackInfo track) {
        if (track == null) {
            return "";
        }

        List<String> parts = new ArrayList<>();

        int channels = track.getChannelCount();

        if (channels > 0) {
            if (channels == 1) {
                parts.add("单声道");
            } else if (channels == 2) {
                parts.add("立体声");
            } else {
                parts.add(channels + " 声道");
            }
        }

        int sampleRate = track.getSampleRate();

        if (sampleRate > 0) {
            parts.add(String.format(
                    Locale.US,
                    "%.1f kHz",
                    sampleRate / 1000f
            ));
        }

        int bitrate = track.getBitrate();

        if (bitrate > 0) {
            if (bitrate >= 1_000_000) {
                parts.add(String.format(
                        Locale.US,
                        "%.1f Mbps",
                        bitrate / 1_000_000f
                ));
            } else {
                parts.add((bitrate / 1000) + " kbps");
            }
        }

        return join(parts);
    }

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(" · ");
            }

            builder.append(value);
        }

        return builder.toString();
    }

    private String languageName(String language) {
        String value = language
                .trim()
                .toLowerCase(Locale.US);

        switch (value) {
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
                return language;
        }
    }

    /**
     * 清除保存的音频轨道选择。
     */
    public void clearSelection() {
        selectedLanguage = null;
        selectedGroupIndex = -1;
        selectedTrackIndex = -1;
    }
}
