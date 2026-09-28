package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerTrackManager {

    private ExoPlayer player;

    public PlayerTrackManager() {
    }

    public PlayerTrackManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public List<PlayerTrackInfo> getAllTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        return getTracks(player.getCurrentTracks());
    }

    public List<PlayerTrackInfo> getTracks(Tracks tracks) {
        if (tracks == null) {
            return Collections.emptyList();
        }

        List<PlayerTrackInfo> result = new ArrayList<>();
        List<Tracks.Group> groups = tracks.getGroups();

        for (int groupIndex = 0;
             groupIndex < groups.size();
             groupIndex++) {

            Tracks.Group group = groups.get(groupIndex);
            TrackGroup trackGroup =
                    group.getMediaTrackGroup();

            int trackType = group.getType();

            for (int trackIndex = 0;
                 trackIndex < trackGroup.length;
                 trackIndex++) {

                Format format =
                        trackGroup.getFormat(trackIndex);

                result.add(
                        new PlayerTrackInfo(
                                trackType,
                                groupIndex,
                                trackIndex,
                                format.id,
                                format.language,
                                format.label,
                                format.sampleMimeType,
                                format.width,
                                format.height,
                                format.bitrate,
                                format.channelCount,
                                format.sampleRate,
                                group.isTrackSelected(trackIndex),
                                group.isTrackSupported(trackIndex)
                        )
                );
            }
        }

        return result;
    }

    public List<PlayerTrackInfo> getVideoTracks() {
        return getTracksByType(C.TRACK_TYPE_VIDEO);
    }

    public List<PlayerTrackInfo> getAudioTracks() {
        return getTracksByType(C.TRACK_TYPE_AUDIO);
    }

    public List<PlayerTrackInfo> getSubtitleTracks() {
        return getTracksByType(C.TRACK_TYPE_TEXT);
    }

    private List<PlayerTrackInfo> getTracksByType(int type) {
        List<PlayerTrackInfo> all = getAllTracks();

        if (all.isEmpty()) return all;

        List<PlayerTrackInfo> result = new ArrayList<>();

        for (PlayerTrackInfo info : all) {
            if (info.getTrackType() == type) {
                result.add(info);
            }
        }

        return result;
    }

    public PlayerTrackInfo getSelectedAudioTrack() {
        return getSelectedTrack(C.TRACK_TYPE_AUDIO);
    }

    public PlayerTrackInfo getSelectedSubtitleTrack() {
        return getSelectedTrack(C.TRACK_TYPE_TEXT);
    }

    public PlayerTrackInfo getSelectedVideoTrack() {
        return getSelectedTrack(C.TRACK_TYPE_VIDEO);
    }

    private PlayerTrackInfo getSelectedTrack(int type) {
        for (PlayerTrackInfo info : getTracksByType(type)) {
            if (info.isSelected()) return info;
        }

        return null;
    }

    public int getTrackCount(int type) {
        return getTracksByType(type).size();
    }

    public boolean hasSubtitleTracks() {
        return !getSubtitleTracks().isEmpty();
    }

    public boolean hasMultipleAudioTracks() {
        return getAudioTracks().size() > 1;
    }

    public boolean hasMultipleSubtitleTracks() {
        return getSubtitleTracks().size() > 1;
    }

    public PlayerTrackInfo findTrack(
            int type,
            int groupIndex,
            int trackIndex
    ) {
        for (PlayerTrackInfo info : getAllTracks()) {
            if (info.getTrackType() == type &&
                    info.getGroupIndex() == groupIndex &&
                    info.getTrackIndex() == trackIndex) {
                return info;
            }
        }

        return null;
    }

    public List<PlayerTrackInfo> getImmutableTracks() {
        return Collections.unmodifiableList(
                new ArrayList<>(getAllTracks())
        );
    }

    public static String getTrackTypeName(int type) {
        switch (type) {
            case C.TRACK_TYPE_VIDEO:
                return "视频";
            case C.TRACK_TYPE_AUDIO:
                return "音频";
            case C.TRACK_TYPE_TEXT:
                return "字幕";
            case C.TRACK_TYPE_IMAGE:
                return "图片";
            case C.TRACK_TYPE_METADATA:
                return "元数据";
            default:
                return "其他";
        }
    }
}