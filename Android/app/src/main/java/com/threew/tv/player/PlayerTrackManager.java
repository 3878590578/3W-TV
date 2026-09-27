package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackGroupArray;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放器轨道管理器
 *
 * 负责读取当前播放器的视频、音频、字幕轨道信息，
 * 并转换为统一的 PlayerTrackInfo。
 */
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

    /**
     * 获取全部轨道。
     */
    public List<PlayerTrackInfo> getAllTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        return getTracks(player.getCurrentTracks());
    }

    /**
     * 根据 Tracks 获取全部轨道。
     */
    public List<PlayerTrackInfo> getTracks(Tracks tracks) {
        if (tracks == null) {
            return Collections.emptyList();
        }

        List<PlayerTrackInfo> result = new ArrayList<>();

        List<Tracks.Group> groups = tracks.getGroups();

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Tracks.Group group = groups.get(groupIndex);

            TrackGroup trackGroup = group.getMediaTrackGroup();
            int trackType = group.getType();

            for (int trackIndex = 0;
                 trackIndex < trackGroup.length;
                 trackIndex++) {

                Format format = trackGroup.getFormat(trackIndex);

                boolean selected = group.isTrackSelected(trackIndex);
                boolean supported = group.isTrackSupported(trackIndex);

                result.add(new PlayerTrackInfo(
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
                        selected,
                        supported
                ));
            }
        }

        return result;
    }

    /**
     * 获取视频轨道。
     */
    public List<PlayerTrackInfo> getVideoTracks() {
        return getTracksByType(C.TRACK_TYPE_VIDEO);
    }

    /**
     * 获取音频轨道。
     */
    public List<PlayerTrackInfo> getAudioTracks() {
        return getTracksByType(C.TRACK_TYPE_AUDIO);
    }

    /**
     * 获取字幕轨道。
     */
    public List<PlayerTrackInfo> getSubtitleTracks() {
        return getTracksByType(C.TRACK_TYPE_TEXT);
    }

    private List<PlayerTrackInfo> getTracksByType(int type) {
        List<PlayerTrackInfo> all = getAllTracks();

        if (all.isEmpty()) {
            return all;
        }

        List<PlayerTrackInfo> result = new ArrayList<>();

        for (PlayerTrackInfo info : all) {
            if (info.getTrackType() == type) {
                result.add(info);
            }
        }

        return result;
    }

    /**
     * 获取当前选中的音频轨道。
     */
    public PlayerTrackInfo getSelectedAudioTrack() {
        return getSelectedTrack(C.TRACK_TYPE_AUDIO);
    }

    /**
     * 获取当前选中的字幕轨道。
     */
    public PlayerTrackInfo getSelectedSubtitleTrack() {
        return getSelectedTrack(C.TRACK_TYPE_TEXT);
    }

    /**
     * 获取当前选中的视频轨道。
     */
    public PlayerTrackInfo getSelectedVideoTrack() {
        return getSelectedTrack(C.TRACK_TYPE_VIDEO);
    }

    private PlayerTrackInfo getSelectedTrack(int type) {
        List<PlayerTrackInfo> tracks = getTracksByType(type);

        for (PlayerTrackInfo info : tracks) {
            if (info.isSelected()) {
                return info;
            }
        }

        return null;
    }

    /**
     * 获取轨道数量。
     */
    public int getTrackCount(int type) {
        return getTracksByType(type).size();
    }

    /**
     * 是否存在字幕。
     */
    public boolean hasSubtitleTracks() {
        return !getSubtitleTracks().isEmpty();
    }

    /**
     * 是否存在多个音频轨道。
     */
    public boolean hasMultipleAudioTracks() {
        return getAudioTracks().size() > 1;
    }

    /**
     * 是否存在多个字幕轨道。
     */
    public boolean hasMultipleSubtitleTracks() {
        return getSubtitleTracks().size() > 1;
    }

    /**
     * 根据轨道索引获取轨道。
     */
    public PlayerTrackInfo findTrack(int type, int groupIndex, int trackIndex) {
        List<PlayerTrackInfo> tracks = getAllTracks();

        for (PlayerTrackInfo info : tracks) {
            if (info.getTrackType() == type
                    && info.getGroupIndex() == groupIndex
                    && info.getTrackIndex() == trackIndex) {
                return info;
            }
        }

        return null;
    }

    /**
     * 返回不可变的轨道列表。
     */
    public List<PlayerTrackInfo> getImmutableTracks() {
        return Collections.unmodifiableList(
                new ArrayList<>(getAllTracks())
        );
    }

    /**
     * 轨道类型转文字。
     */
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
