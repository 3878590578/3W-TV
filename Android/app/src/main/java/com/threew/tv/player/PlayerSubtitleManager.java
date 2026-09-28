package com.threew.tv.player;

import android.content.Context;
import android.net.Uri;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.text.Cue;
import androidx.media3.common.text.CueGroup;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerSubtitleManager {

    private final Context context;

    private ExoPlayer player;

    private boolean subtitleEnabled = true;

    private String selectedLanguage;
    private String selectedSubtitleUri;
    private String currentText = "";

    public PlayerSubtitleManager(
            Context context
    ) {
        this.context =
                context.getApplicationContext();
    }

    public PlayerSubtitleManager(
            Context context,
            ExoPlayer player
    ) {
        this.context =
                context.getApplicationContext();
        this.player = player;
    }

    public void attachPlayer(
            ExoPlayer player
    ) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

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

    public void toggle() {
        if (subtitleEnabled) {
            disable();
        } else {
            enable();
        }
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(
            String language
    ) {
        if (language == null) {
            selectedLanguage = null;
            return;
        }

        String value =
                language.trim();

        selectedLanguage =
                value.isEmpty()
                        ? null
                        : value;
    }

    public String getSelectedSubtitleUri() {
        return selectedSubtitleUri;
    }

    public void setSelectedSubtitleUri(
            String uri
    ) {
        selectedSubtitleUri = uri;
    }

    public String getCurrentText() {
        return currentText;
    }

    public void updateCueGroup(
            CueGroup cueGroup
    ) {
        if (cueGroup == null ||
                !subtitleEnabled) {
            currentText = "";
            return;
        }

        List<Cue> cues =
                cueGroup.cues;

        if (cues == null ||
                cues.isEmpty()) {
            currentText = "";
            return;
        }

        StringBuilder builder =
                new StringBuilder();

        for (Cue cue : cues) {
            if (cue == null ||
                    cue.text == null) {
                continue;
            }

            String text =
                    cue.text.toString().trim();

            if (text.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append("\n");
            }

            builder.append(text);
        }

        currentText =
                builder.toString();
    }

    public MediaItem.SubtitleConfiguration
    createSubtitleConfiguration(
            Uri subtitleUri,
            String language,
            String label,
            String mimeType
    ) {
        if (subtitleUri == null) {
            return null;
        }

        String finalLanguage =
                normalizeLanguage(language);

        String finalMimeType =
                mimeType;

        if (finalMimeType == null ||
                finalMimeType.trim().isEmpty()) {
            finalMimeType =
                    guessMimeType(
                            subtitleUri
                    );
        }

        String finalLabel =
                label;

        if (finalLabel == null ||
                finalLabel.trim().isEmpty()) {
            finalLabel =
                    finalLanguage;
        }

        return new MediaItem
                .SubtitleConfiguration
                .Builder(subtitleUri)
                .setMimeType(finalMimeType)
                .setLanguage(finalLanguage)
                .setLabel(finalLabel)
                .setSelectionFlags(
                        C.SELECTION_FLAG_DEFAULT
                )
                .build();
    }

    public String guessMimeType(
            Uri uri
    ) {
        if (uri == null) {
            return MimeTypes.TEXT_VTT;
        }

        String value =
                uri.toString()
                        .toLowerCase(Locale.US);

        if (value.endsWith(".srt")) {
            return MimeTypes.APPLICATION_SUBRIP;
        }

        if (value.endsWith(".ass") ||
                value.endsWith(".ssa")) {
            return MimeTypes.TEXT_SSA;
        }

        if (value.endsWith(".vtt")) {
            return MimeTypes.TEXT_VTT;
        }

        return MimeTypes.TEXT_VTT;
    }

    public List<PlayerTrackInfo>
    getSubtitleTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        PlayerTrackManager manager =
                new PlayerTrackManager(player);

        return manager.getSubtitleTracks();
    }

    public List<String>
    getSubtitleLanguages() {
        List<PlayerTrackInfo> tracks =
                getSubtitleTracks();

        if (tracks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result =
                new ArrayList<>();

        for (PlayerTrackInfo track : tracks) {
            String language =
                    track.getLanguage();

            if (language == null ||
                    language.trim().isEmpty()) {
                continue;
            }

            if (!result.contains(language)) {
                result.add(language);
            }
        }

        return result;
    }

    public void selectLanguage(
            String language
    ) {
        setSelectedLanguage(language);
        enable();
    }

    public void clearSelection() {
        selectedLanguage = null;
        selectedSubtitleUri = null;
        currentText = "";
    }

    private String normalizeLanguage(
            String language
    ) {
        if (language == null ||
                language.trim().isEmpty()) {
            return "und";
        }

        String value =
                language.trim()
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

    public boolean hasCurrentText() {
        return currentText != null &&
                !currentText.trim().isEmpty();
    }

    public void clearCurrentText() {
        currentText = "";
    }
}