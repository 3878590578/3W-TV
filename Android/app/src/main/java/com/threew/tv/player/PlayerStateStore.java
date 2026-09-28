package com.threew.tv.player;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 播放器轻量状态保存。
 *
 * 用于保存播放器上次关闭时的基础状态，
 * 不替代 HistoryManager。
 */
public class PlayerStateStore {

    private static final String PREF_NAME = "threew_player_state";

    private static final String KEY_LAST_VIDEO_ID = "last_video_id";
    private static final String KEY_LAST_EPISODE_ID = "last_episode_id";
    private static final String KEY_LAST_SOURCE_ID = "last_source_id";
    private static final String KEY_LAST_POSITION = "last_position";
    private static final String KEY_LAST_SPEED = "last_speed";

    private final SharedPreferences preferences;

    public PlayerStateStore(Context context) {
        Context appContext = context.getApplicationContext();

        preferences = appContext.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveSession(PlayerSession session) {
        if (session == null) {
            return;
        }

        preferences.edit()
                .putString(KEY_LAST_VIDEO_ID, session.getVideoId())
                .putString(KEY_LAST_EPISODE_ID, session.getEpisodeId())
                .putString(KEY_LAST_SOURCE_ID, session.getSourceId())
                .apply();
    }

    public PlayerSession restoreSession() {
        String videoId = preferences.getString(
                KEY_LAST_VIDEO_ID,
                null
        );

        String episodeId = preferences.getString(
                KEY_LAST_EPISODE_ID,
                null
        );

        String sourceId = preferences.getString(
                KEY_LAST_SOURCE_ID,
                null
        );

        if (videoId == null
                && episodeId == null
                && sourceId == null) {
            return null;
        }

        PlayerSession session =
                new PlayerSession("restored");

        session.setVideoId(videoId);
        session.setEpisodeId(episodeId);
        session.setSourceId(sourceId);

        return session;
    }

    public void savePosition(long positionMs) {
        preferences.edit()
                .putLong(
                        KEY_LAST_POSITION,
                        Math.max(0L, positionMs)
                )
                .apply();
    }

    public long getPosition() {
        return preferences.getLong(
                KEY_LAST_POSITION,
                0L
        );
    }

    public void saveSpeed(float speed) {
        if (speed <= 0f) {
            speed = 1.0f;
        }

        preferences.edit()
                .putFloat(KEY_LAST_SPEED, speed)
                .apply();
    }

    public float getSpeed() {
        return preferences.getFloat(
                KEY_LAST_SPEED,
                1.0f
        );
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}