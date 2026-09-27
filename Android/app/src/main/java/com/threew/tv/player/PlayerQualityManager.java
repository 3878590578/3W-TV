package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 播放器画质管理器。
 *
 * 负责读取当前视频可用的视频轨道，
 * 提供自动、原画以及常见分辨率选项。
 *
 * 实际轨道切换由 TrackSelectorManager 负责。
 */
public class PlayerQualityManager {

    public static final int QUALITY_AUTO = 0;
    public static final int QUALITY_2160P = 2160;
    public static final int QUALITY_1440P = 1440;
    public static final int QUALITY_1080P = 1080;
    public static final int QUALITY_720P = 720;
    public static final int QUALITY_480P = 480;
    public static final int QUALITY_360P = 360;

    private ExoPlayer player;

    private int selectedQuality = QUALITY_AUTO;

    public PlayerQualityManager() {
    }

    public PlayerQualityManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * 获取所有视频轨道。
     */
    public List<PlayerTrackInfo> getVideoTracks() {
        if (player == null) {
            return Collections.emptyList();
        }

        return new PlayerTrackManager(player).getVideoTracks();
    }

    /**
     * 获取当前视频轨道。
     */
    public PlayerTrackInfo getSelectedTrack() {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        for (PlayerTrackInfo track : tracks) {
            if (track.isSelected()) {
                return track;
            }
        }

        return null;
    }

    /**
     * 获取当前实际视频高度。
     */
    public int getCurrentHeight() {
        PlayerTrackInfo track = getSelectedTrack();

        if (track == null) {
            return 0;
        }

        return track.getHeight();
    }

    /**
     * 获取当前实际视频宽度。
     */
    public int getCurrentWidth() {
        PlayerTrackInfo track = getSelectedTrack();

        if (track == null) {
            return 0;
        }

        return track.getWidth();
    }

    /**
     * 获取当前画质文字。
     */
    public String getCurrentQualityName() {
        PlayerTrackInfo track = getSelectedTrack();

        if (track == null) {
            return "自动";
        }

        return formatQuality(track.getWidth(), track.getHeight());
    }

    /**
     * 获取可用画质列表。
     */
    public List<QualityOption> getQualityOptions() {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        if (tracks.isEmpty()) {
            return Collections.singletonList(
                    new QualityOption(
                            QUALITY_AUTO,
                            "自动",
                            0,
                            0
                    )
            );
        }

        List<QualityOption> result = new ArrayList<>();

        result.add(new QualityOption(
                QUALITY_AUTO,
                "自动",
                0,
                0
        ));

        List<PlayerTrackInfo> sorted = new ArrayList<>(tracks);

        Collections.sort(
                sorted,
                new Comparator<PlayerTrackInfo>() {
                    @Override
                    public int compare(
                            PlayerTrackInfo first,
                            PlayerTrackInfo second
                    ) {
                        return Integer.compare(
                                second.getHeight(),
                                first.getHeight()
                        );
                    }
                }
        );

        for (PlayerTrackInfo track : sorted) {
            int height = track.getHeight();
            int width = track.getWidth();

            if (height <= 0 && width <= 0) {
                continue;
            }

            boolean exists = false;

            for (QualityOption option : result) {
                if (option.getHeight() == height
                        && option.getWidth() == width) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                continue;
            }

            result.add(new QualityOption(
                    height,
                    formatQuality(width, height),
                    width,
                    height
            ));
        }

        return result;
    }

    /**
     * 获取常见画质是否可用。
     */
    public boolean hasQuality(int targetHeight) {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        for (PlayerTrackInfo track : tracks) {
            if (track.getHeight() == targetHeight) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取最接近目标高度的视频轨道。
     */
    public PlayerTrackInfo findClosestQuality(int targetHeight) {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        if (tracks.isEmpty()) {
            return null;
        }

        PlayerTrackInfo best = null;
        int bestDifference = Integer.MAX_VALUE;

        for (PlayerTrackInfo track : tracks) {
            int height = track.getHeight();

            if (height <= 0) {
                continue;
            }

            int difference = Math.abs(height - targetHeight);

            if (difference < bestDifference) {
                bestDifference = difference;
                best = track;
            }
        }

        return best;
    }

    /**
     * 记录用户选择的画质。
     *
     * 实际轨道切换交给 TrackSelectorManager。
     */
    public void setQuality(int quality) {
        selectedQuality = quality;
    }

    public int getSelectedQuality() {
        return selectedQuality;
    }

    public void setAutoQuality() {
        selectedQuality = QUALITY_AUTO;
    }

    public boolean isAutoQuality() {
        return selectedQuality == QUALITY_AUTO;
    }

    /**
     * 获取当前播放器是否支持自适应画质。
     */
    public boolean supportsAdaptiveQuality() {
        if (player == null) {
            return false;
        }

        Tracks tracks = player.getCurrentTracks();

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() != C.TRACK_TYPE_VIDEO) {
                continue;
            }

            if (group.length > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取最高画质。
     */
    public PlayerTrackInfo getHighestQuality() {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        PlayerTrackInfo result = null;

        for (PlayerTrackInfo track : tracks) {
            if (result == null
                    || track.getHeight() > result.getHeight()) {
                result = track;
            }
        }

        return result;
    }

    /**
     * 获取最低画质。
     */
    public PlayerTrackInfo getLowestQuality() {
        List<PlayerTrackInfo> tracks = getVideoTracks();

        PlayerTrackInfo result = null;

        for (PlayerTrackInfo track : tracks) {
            if (track.getHeight() <= 0) {
                continue;
            }

            if (result == null
                    || track.getHeight() < result.getHeight()) {
                result = track;
            }
        }

        return result;
    }

    /**
     * 格式化画质名称。
     */
    public String formatQuality(int width, int height) {
        if (height <= 0) {
            return "未知";
        }

        if (height >= 2160) {
            return "4K";
        }

        if (height >= 1440) {
            return "2K";
        }

        if (height >= 1080) {
            return "1080P";
        }

        if (height >= 720) {
            return "720P";
        }

        if (height >= 480) {
            return "480P";
        }

        if (height >= 360) {
            return "360P";
        }

        return height + "P";
    }

    /**
     * 画质选项。
     */
    public static class QualityOption {

        private final int value;
        private final String name;
        private final int width;
        private final int height;

        public QualityOption(
                int value,
                String name,
                int width,
                int height
        ) {
            this.value = value;
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isAuto() {
            return value == QUALITY_AUTO;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
