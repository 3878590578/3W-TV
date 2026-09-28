package com.threew.tv.player;

import android.content.Context;
import android.text.TextUtils;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackSelectorManager {

    private final DefaultTrackSelector trackSelector;

    public TrackSelectorManager(Context context) {
        trackSelector =
                new DefaultTrackSelector(
                        context.getApplicationContext()
                );

        trackSelector.setParameters(
                trackSelector
                        .buildUponParameters()
                        .clearOverrides()
                        .build()
        );
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void attachToPlayer(
            ExoPlayer player
    ) {
        if (player == null) {
            return;
        }

        player.setTrackSelectionParameters(
                trackSelector
                        .buildUponParameters()
                        .build()
        );
    }

    public void selectAudioLanguage(
            ExoPlayer player,
            String language
    ) {
        if (player == null) {
            return;
        }

        TrackSelectionParameters.Builder builder =
                player.getTrackSelectionParameters()
                        .buildUpon();

        if (TextUtils.isEmpty(language)) {
            builder.setPreferredAudioLanguage(null);
        } else {
            builder.setPreferredAudioLanguage(
                    language
            );
        }

        player.setTrackSelectionParameters(
                builder.build()
        );
    }

    public void selectSubtitleLanguage(
            ExoPlayer player,
            String language
    ) {
        if (player == null) {
            return;
        }

        TrackSelectionParameters.Builder builder =
                player.getTrackSelectionParameters()
                        .buildUpon();

        if (TextUtils.isEmpty(language)) {
            builder.setPreferredTextLanguage(null);
        } else {
            builder.setPreferredTextLanguage(
                    language
            );
        }

        builder.setTrackTypeDisabled(
                C.TRACK_TYPE_TEXT,
                false
        );

        player.setTrackSelectionParameters(
                builder.build()
        );
    }

    public void disableSubtitles(
            ExoPlayer player
    ) {
        if (player == null) {
            return;
        }

        TrackSelectionParameters parameters =
                player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(
                                C.TRACK_TYPE_TEXT,
                                true
                        )
                        .build();

        player.setTrackSelectionParameters(
                parameters
        );
    }

    public void enableSubtitles(
            ExoPlayer player
    ) {
        if (player == null) {
            return;
        }

        TrackSelectionParameters parameters =
                player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(
                                C.TRACK_TYPE_TEXT,
                                false
                        )
                        .build();

        player.setTrackSelectionParameters(
                parameters
        );
    }

    public void selectTrack(
            ExoPlayer player,
            Tracks.Group group,
            int trackIndex
    ) {
        if (player == null ||
                group == null ||
                trackIndex < 0 ||
                trackIndex >= group.length) {
            return;
        }

        TrackSelectionOverride override =
                new TrackSelectionOverride(
                        group.getMediaTrackGroup(),
                        Collections.singletonList(
                                trackIndex
                        )
                );

        TrackSelectionParameters parameters =
                player.getTrackSelectionParameters()
                        .buildUpon()
                        .addOverride(override)
                        .build();

        player.setTrackSelectionParameters(
                parameters
        );
    }

    public void clearOverrides(
            ExoPlayer player
    ) {
        if (player == null) {
            return;
        }

        TrackSelectionParameters parameters =
                player.getTrackSelectionParameters()
                        .buildUpon()
                        .clearOverrides()
                        .build();

        player.setTrackSelectionParameters(
                parameters
        );
    }

    public List<TrackInfo> getAudioTracks(
            ExoPlayer player
    ) {
        return getTracks(
                player,
                C.TRACK_TYPE_AUDIO
        );
    }

    public List<TrackInfo> getSubtitleTracks(
            ExoPlayer player
    ) {
        return getTracks(
                player,
                C.TRACK_TYPE_TEXT
        );
    }

    public List<TrackInfo> getVideoTracks(
            ExoPlayer player
    ) {
        return getTracks(
                player,
                C.TRACK_TYPE_VIDEO
        );
    }

    private List<TrackInfo> getTracks(
            ExoPlayer player,
            int trackType
    ) {
        List<TrackInfo> result =
                new ArrayList<>();

        if (player == null) {
            return result;
        }

        Tracks tracks =
                player.getCurrentTracks();

        for (Tracks.Group group :
                tracks.getGroups()) {

            if (group.getType() != trackType) {
                continue;
            }

            if (group.length <= 0) {
                continue;
            }

            Format format =
                    group.getTrackFormat(0);

            String language =
                    format.language;

            if (language == null) {
                language = "";
            }

            String label =
                    format.label;

            if (TextUtils.isEmpty(label)) {
                label = language;
            }

            if (TextUtils.isEmpty(label)) {
                label = "轨道";
            }

            result.add(
                    new TrackInfo(
                            group,
                            label,
                            language,
                            format.id,
                            format.sampleMimeType,
                            group.isSelected()
                    )
            );
        }

        return result;
    }

    public TrackInfo findSelectedAudio(
            ExoPlayer player
    ) {
        return findSelected(
                getAudioTracks(player)
        );
    }

    public TrackInfo findSelectedSubtitle(
            ExoPlayer player
    ) {
        return findSelected(
                getSubtitleTracks(player)
        );
    }

    public TrackInfo findSelectedVideo(
            ExoPlayer player
    ) {
        return findSelected(
                getVideoTracks(player)
        );
    }

    private TrackInfo findSelected(
            List<TrackInfo> tracks
    ) {
        if (tracks == null) {
            return null;
        }

        for (TrackInfo track : tracks) {
            if (track.isSelected()) {
                return track;
            }
        }

        return null;
    }

    public static class TrackInfo {

        private final Tracks.Group group;
        private final String label;
        private final String language;
        private final String id;
        private final String mimeType;
        private final boolean selected;

        public TrackInfo(
                Tracks.Group group,
                String label,
                String language,
                String id,
                String mimeType,
                boolean selected
        ) {
            this.group = group;
            this.label = label;
            this.language = language;
            this.id = id;
            this.mimeType = mimeType;
            this.selected = selected;
        }

        public Tracks.Group getGroup() {
            return group;
        }

        public String getLabel() {
            return label;
        }

        public String getLanguage() {
            return language;
        }

        public String getId() {
            return id;
        }

        public String getMimeType() {
            return mimeType;
        }

        public boolean isSelected() {
            return selected;
        }

        public int getTrackCount() {
            return group == null
                    ? 0
                    : group.length;
        }
    }
}