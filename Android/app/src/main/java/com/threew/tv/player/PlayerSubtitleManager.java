package com.threew.tv.player;

import android.content.Context;
import android.net.Uri;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.text.Cue;
import androidx.media3.common.text.CueGroup;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 播放器字幕管理器。
 *
 * 支持：
 * 1. 管理播放器内置字幕轨道；
 * 2. 选择/关闭字幕；
 * 3. 从本地文件构造外挂字幕 MediaItem；
 * 4. 保存当前字幕状态；
 * 5. 提供当前字幕显示文本。
 */
public class PlayerSubtitleManager {

    private final Context context;

    private ExoPlayer player;

    private boolean subtitleEnabled = true;

    private String selectedLanguage;

    private String selectedSubtitleUri;

    private String currentText = "";

    public PlayerSubtitleManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public PlayerSubtitleManager(Context context, ExoPlayer player) {
        this.context = context.getApplicationContext();
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * 启用字幕。
     */
    public void enable() {
        subtitleEnabled = true;

        if (player != null) {
            player.setTrackSelectionParameters(
                    player.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(
                                    C.TRACK_TYPE_TEXT,
                                    false
                            )
                            .build()
            );
        }
    }

    /**
     * 关闭字幕。
     */
    public void disable() {
        subtitleEnabled = false;

        if (player != null) {
            player.setTrackSelectionParameters(
                    player.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(
                                    C.TRACK_TYPE_TEXT,
                                    true
                            )
                            .build()
            );
        }

        currentText = "";
    }

    public boolean isEnabled() {
        return subtitleEnabled;
    }

    /**
     * 切换字幕开关。
     */
    public void toggle() {
        if (subtitleEnabled) {
            disable();
        } else {
            enable();
        }
    }

    /**
     * 获取当前字幕语言。
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * 设置字幕语言记录。
     *
     * 实际轨道选择由 TrackSelectorManager 负责。
     */
    public void setSelectedLanguage(String language) {
        if (language == null) {
            selectedLanguage = null;
            return;
        }

        String value = language.trim();

        selectedLanguage = value.isEmpty() ? null : value;
    }

    /**
     * 获取外挂字幕 URI。
     */
    public String getSelectedSubtitleUri() {
        return selectedSubtitleUri;
    }

    public void setSelectedSubtitleUri(String uri) {
        selectedSubtitleUri = uri;
    }

    /**
     * 获取当前字幕文本。
     */
    public String getCurrentText() {
        return currentText;
    }

    /**
     * 根据 CueGroup 更新当前字幕文本。
     */
    public void updateCueGroup(CueGroup cueGroup) {
        if (cueGroup == null || !subtitleEnabled) {
            currentText = "";
            return;
        }

        List<Cue> cues = cueGroup.cues;

        if (cues == null || cues.isEmpty()) {
            currentText = "";
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (Cue cue : cues) {
            if (cue == null || cue.text == null) {
                continue;
            }

            String text = cue.text.toString().trim();

            if (text.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append("\n");
            }

            builder.append(text);
        }

        currentText = builder.toString();
    }

    /**
     * 创建外挂字幕配置。
     *
     * 注意：
     * 这里只构造 MediaItem，不会自动替换当前视频。
     */
    public MediaItem.SubtitleConfiguration createSubtitleConfiguration(
            Uri subtitleUri,
            String language,
            String label,
            String mimeType
    ) {
        if (subtitleUri == null) {
            return null;
        }

        String finalLanguage = normalizeLanguage(language);

        String finalMimeType = mimeType;

        if (finalMimeType == null || finalMimeType.trim().isEmpty()) {
            finalMimeType = guessMimeType(subtitleUri);
        }

        String finalLabel = label;

        if (finalLabel == null || finalLabel.trim().isEmpty()) {
            finalLabel = finalLanguage;
        }

        return new MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                .setMimeType(finalMimeType)
                .setLanguage(finalLanguage)
                .setLabel(finalLabel)
                .setSelectionFlags(
                        MediaItem.SubtitleConfiguration.SELECTION_FLAG_DEFAULT
                )
                .build();
    }

    /**
     * 从本地字幕 URI 判断常见字幕类型。
     */
    public String guessMimeType(Uri uri) {
        if (uri == null) {
            return MimeTypes.TEXT_VTT;
        }

        String value = uri.toString().toLowerCase(Locale.US);

        if (value.endsWith(".srt")) {
            return MimeTypes.APPLICATION_SUBRIP;
        }

        if (value.endsWith(".ass") || value.endsWith(".ssa")) {
            return MimeTypes.TEXT_SSA;
        }

        if (value.endsWith(".vtt")) {
            return MimeTypes.TEXT_VTT;
        }

        return MimeTypes.TEXT_VTT;
    }

    /**
     * 获取播放器当前字幕轨道。
     */
    public List<PlayerTrackInfo> getSubtitleTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        PlayerTrackManager manager =
                new PlayerTrackManager(player);

        return manager.getSubtitleTracks();
    }

    /**
     * 获取字幕语言列表。
     */
    public List<String> getSubtitleLanguages() {
        List<PlayerTrackInfo> tracks = getSubtitleTracks();

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
     * 根据语言记录选择字幕。
     *
     * 轨道实际选择交给 TrackSelectorManager，
     * 这里只维护字幕状态。
     */
    public void selectLanguage(String language) {
        setSelectedLanguage(language);
        enable();
    }

    /**
     * 清除当前字幕选择记录。
     */
    public void clearSelection() {
        selectedLanguage = null;
        selectedSubtitleUri = null;
        currentText = "";
    }

    /**
     * 语言代码标准化。
     */
    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "und";
        }

        String value = language.trim()
                .replace('_', '-')
                .toLowerCase(Locale.US);

        if ("cn".equals(value)) {
            return "zh";
        }

        if ("jp".equals(value)) {
            return "ja";
        }

        if ("kr".equals(value)) {
            return "ko";
        }

        return value;
    }

    /**
     * 获取当前字幕是否有内容。
     */
    public boolean hasCurrentText() {
        return currentText != null
                && !currentText.trim().isEmpty();
    }

    /**
     * 清除当前显示字幕文本。
     */
    public void clearCurrentText() {
        currentText = "";
    }
}
