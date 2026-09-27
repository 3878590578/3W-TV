package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据访问对象。
 *
 * 负责：
 * - 保存视频源返回的视频
 * - 查询视频
 * - 搜索视频
 * - 同名视频合并查询
 *
 * 视频详情中的剧集数据保存在 Video 对象中，
 * 后续播放历史单独由 HistoryDao 保存。
 */
public class VideoDao {

    private final DatabaseHelper databaseHelper;

    public VideoDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 保存或更新一个视频。
     *
     * 视频本身不单独建立永久视频表，
     * 第一版主要使用接口实时获取数据。
     * 这里提供内存/数据库扩展入口。
     *
     * 当前方法主要用于统一处理视频对象，
     * 返回传入对象。
     */
    public Video save(Video video) {
        return video;
    }

    /**
     * 根据名称搜索历史缓存结果。
     *
     * 当前实际搜索由 API 实时完成，
     * 这个方法用于后续本地搜索扩展。
     */
    public List<Video> searchLocal(
            String keyword
    ) {
        List<Video> result =
                new ArrayList<>();

        if (keyword == null ||
                keyword.trim().isEmpty()) {
            return result;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        String like =
                "%" + keyword.trim() + "%";

        try (Cursor cursor = db.query(
                "history",
                new String[]{
                        "video_id",
                        "video_name",
                        "poster",
                        "episode_id",
                        "episode_name",
                        "episode_number",
                        "play_url"
                },
                "video_name LIKE ?",
                new String[]{like},
                null,
                null,
                "last_watch_time DESC"
        )) {

            while (cursor.moveToNext()) {

                String id =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "video_id"
                                )
                        );

                String name =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "video_name"
                                )
                        );

                Video video =
                        new Video(
                                id,
                                name
                        );

                video.setPoster(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "poster"
                                )
                        )
                );

                Episode episode =
                        new Episode();

                episode.setId(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "episode_id"
                                )
                        )
                );

                episode.setName(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "episode_name"
                                )
                        )
                );

                episode.setNumber(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "episode_number"
                                )
                        )
                );

                episode.setPlayUrl(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "play_url"
                                )
                        )
                );

                video.addEpisode(episode);

                result.add(video);
            }
        }

        return result;
    }

    /**
     * 获取最近观看的视频。
     */
    public List<Video> getRecentlyWatched(
            int limit
    ) {
        List<Video> result =
                new ArrayList<>();

        if (limit <= 0) {
            limit = 20;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        String sql =
                "SELECT h.* FROM history h " +
                "INNER JOIN (" +
                " SELECT video_id, MAX(last_watch_time) AS latest " +
                " FROM history " +
                " WHERE media_type = 'video' " +
                " GROUP BY video_id" +
                ") x ON h.video_id = x.video_id " +
                "AND h.last_watch_time = x.latest " +
                "ORDER BY h.last_watch_time DESC " +
                "LIMIT ?";

        try (Cursor cursor =
                     db.rawQuery(
                             sql,
                             new String[]{
                                     String.valueOf(limit)
                             }
                     )) {

            while (cursor.moveToNext()) {

                result.add(
                        videoFromHistory(
                                cursor
                        )
                );
            }
        }

        return result;
    }

    /**
     * 根据视频 ID 查询最近一次观看记录。
     */
    public Video getFromHistory(
            String videoId
    ) {
        if (videoId == null ||
                videoId.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "history",
                null,
                "video_id = ?",
                new String[]{videoId},
                null,
                null,
                "last_watch_time DESC",
                "1"
        )) {

            if (cursor.moveToFirst()) {
                return videoFromHistory(cursor);
            }
        }

        return null;
    }

    /**
     * 判断两个视频是否可以合并。
     *
     * 主要用于不同来源的同名影视。
     */
    public boolean canMerge(
            Video first,
            Video second
    ) {
        if (first == null ||
                second == null) {
            return false;
        }

        String firstKey =
                first.getMergeKey();

        String secondKey =
                second.getMergeKey();

        if (firstKey.isEmpty() ||
                secondKey.isEmpty()) {
            return false;
        }

        return firstKey.equals(
                secondKey
        );
    }

    /**
     * 合并两个来源的视频信息。
     *
     * 基础资料优先使用已有完整数据，
     * 剧集则追加到同一个 Video。
     */
    public Video merge(
            Video target,
            Video source
    ) {
        if (target == null) {
            return source;
        }

        if (source == null) {
            return target;
        }

        if (isEmpty(target.getPoster())) {
            target.setPoster(
                    source.getPoster()
            );
        }

        if (isEmpty(target.getDescription())) {
            target.setDescription(
                    source.getDescription()
            );
        }

        if (isEmpty(target.getActor())) {
            target.setActor(
                    source.getActor()
            );
        }

        if (isEmpty(target.getDirector())) {
            target.setDirector(
                    source.getDirector()
            );
        }

        if (isEmpty(target.getYear())) {
            target.setYear(
                    source.getYear()
            );
        }

        if (isEmpty(target.getArea())) {
            target.setArea(
                    source.getArea()
            );
        }

        if (isEmpty(target.getCategory())) {
            target.setCategory(
                    source.getCategory()
            );
        }

        if (isEmpty(target.getRemarks())) {
            target.setRemarks(
                    source.getRemarks()
            );
        }

        if (isEmpty(target.getOriginalName())) {
            target.setOriginalName(
                    source.getOriginalName()
            );
        }

        if (isEmpty(target.getStatus())) {
            target.setStatus(
                    source.getStatus()
            );
        }

        for (Episode episode :
                source.getEpisodes()) {

            if (episode == null) {
                continue;
            }

            if (!containsEpisode(
                    target,
                    episode
            )) {
                target.addEpisode(
                        episode
                );
            }
        }

        return target;
    }

    private boolean containsEpisode(
            Video video,
            Episode episode
    ) {
        for (Episode current :
                video.getEpisodes()) {

            if (current == null) {
                continue;
            }

            String currentUrl =
                    current.getPlayUrl();

            String episodeUrl =
                    episode.getPlayUrl();

            if (currentUrl != null &&
                    episodeUrl != null &&
                    currentUrl.equals(
                            episodeUrl
                    )) {
                return true;
            }

            if (current.getNumber() ==
                    episode.getNumber() &&
                    current.getName() != null &&
                    current.getName().equals(
                            episode.getName()
                    )) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从历史 Cursor 创建 Video。
     */
    private Video videoFromHistory(
            Cursor cursor
    ) {
        String videoId =
                getString(
                        cursor,
                        "video_id"
                );

        String videoName =
                getString(
                        cursor,
                        "video_name"
                );

        Video video =
                new Video(
                        videoId,
                        videoName
                );

        video.setPoster(
                getString(
                        cursor,
                        "poster"
                )
        );

        String episodeId =
                getString(
                        cursor,
                        "episode_id"
                );

        if (episodeId != null &&
                !episodeId.isEmpty()) {

            Episode episode =
                    new Episode();

            episode.setId(
                    episodeId
            );

            episode.setName(
                    getString(
                            cursor,
                            "episode_name"
                    )
            );

            episode.setNumber(
                    getInt(
                            cursor,
                            "episode_number"
                    )
            );

            episode.setPlayUrl(
                    getString(
                            cursor,
                            "play_url"
                    )
            );

            video.addEpisode(
                    episode
            );
        }

        return video;
    }

    private String getString(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return "";
        }

        return cursor.getString(index);
    }

    private int getInt(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return 0;
        }

        return cursor.getInt(index);
    }

    private boolean isEmpty(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
