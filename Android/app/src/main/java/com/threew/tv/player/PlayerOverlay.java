package com.threew.tv.player;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.threew.tv.model.Episode;

public class PlayerOverlay {

    public interface Listener {

        void onPlayPauseClicked();

        void onNextClicked();

        void onPreviousClicked();

        void onSeekTo(long positionMs);

        void onSpeedClicked();

        void onLockClicked();

        void onRetryClicked();

        void onEpisodeSelected(int index);

        void onSettingsClicked();

        void onInfoClicked();

        void onFullscreenClicked();
    }

    private final Context context;
    private FrameLayout root;

    private LinearLayout panel;

    private TextView title;
    private TextView progress;
    private TextView message;

    private boolean visible = true;
    private boolean locked;

    private Listener listener;

    public PlayerOverlay(Context context) {
        this.context = context;
    }

    public PlayerOverlay(
            Context context,
            FrameLayout root) {

        this(context);
        this.root = root;
    }

    public void setListener(
            Listener listener) {

        this.listener = listener;
    }

    public void attach() {

        if (root == null) {
            return;
        }

        if (panel != null) {
            root.removeView(panel);
        }

        panel =
                new LinearLayout(context);

        panel.setOrientation(
                LinearLayout.VERTICAL);

        panel.setGravity(
                Gravity.BOTTOM);

        panel.setPadding(
                20,
                20,
                20,
                20);

        panel.setBackgroundColor(
                0x99070B12);

        title =
                text(
                        "正在播放",
                        16);

        progress =
                text(
                        "00:00 / 00:00",
                        13);

        message =
                text(
                        "",
                        14);

        panel.addView(title);
        panel.addView(progress);
        panel.addView(message);

        LinearLayout buttons =
                new LinearLayout(context);

        buttons.setGravity(
                Gravity.CENTER);

        addButton(
                buttons,
                "上一集",
                v -> {
                    if (listener != null) {
                        listener.onPreviousClicked();
                    }
                });

        addButton(
                buttons,
                "播放/暂停",
                v -> {
                    if (listener != null) {
                        listener.onPlayPauseClicked();
                    }
                });

        addButton(
                buttons,
                "下一集",
                v -> {
                    if (listener != null) {
                        listener.onNextClicked();
                    }
                });

        addButton(
                buttons,
                "倍速",
                v -> {
                    if (listener != null) {
                        listener.onSpeedClicked();
                    }
                });

        addButton(
                buttons,
                "锁定",
                v -> {
                    if (listener != null) {
                        listener.onLockClicked();
                    }
                });

        addButton(
                buttons,
                "设置",
                v -> {
                    if (listener != null) {
                        listener.onSettingsClicked();
                    }
                });

        panel.addView(buttons);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        -1,
                        -2,
                        Gravity.BOTTOM);

        root.addView(
                panel,
                params);
    }

    public void attach(
            FrameLayout root) {

        this.root = root;
        attach();
    }

    private TextView text(
            String value,
            float size) {

        TextView view =
                new TextView(context);

        view.setText(value);
        view.setTextColor(
                Color.WHITE);
        view.setTextSize(size);

        view.setPadding(
                8,
                5,
                8,
                5);

        return view;
    }

    private void addButton(
            LinearLayout parent,
            String value,
            View.OnClickListener listener) {

        TextView button =
                text(value, 13);

        button.setGravity(
                Gravity.CENTER);

        button.setOnClickListener(
                listener);

        parent.addView(
                button,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1f));
    }

    public void show() {

        visible = true;

        if (panel != null) {
            panel.setVisibility(
                    View.VISIBLE);
        }
    }

    public void hide() {

        visible = false;

        if (panel != null) {
            panel.setVisibility(
                    View.GONE);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setLocked(
            boolean locked) {

        this.locked = locked;

        if (locked) {
            showMessage("已锁定");
        }
    }

    public void setLoading(
            boolean loading) {

        if (loading) {
            showMessage("正在加载…");
        }
    }

    public void setPlaying(
            boolean playing) {

        if (playing) {
            showMessage("播放中");
        }
    }

    public void updateProgress(
            long positionMs,
            long durationMs) {

        if (progress == null) {
            return;
        }

        progress.setText(
                formatTime(positionMs)
                        + " / "
                        + formatTime(durationMs));
    }

    public void setEpisode(
            Episode episode,
            int index,
            int total) {

        if (episode == null) {
            return;
        }

        String name =
                episode.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name =
                    "第 "
                            + episode.getNumber()
                            + " 集";
        }

        title.setText(
                name
                        + "  ·  "
                        + (index + 1)
                        + "/"
                        + total);
    }

    public void showNextEpisode(
            boolean hasNext) {

        if (hasNext) {
            showMessage(
                    "即将播放下一集");
        }
    }

    public void showNextEpisode(
            String episodeName,
            int seconds) {

        showMessage(
                "下一集："
                        + (episodeName == null
                        ? ""
                        : episodeName)
                        + " · "
                        + seconds
                        + " 秒");
    }

    public void showSpeed(
            float speed) {

        showMessage(
                "倍速 "
                        + SpeedManager.formatSpeed(
                        speed));
    }

    public void showBrightness(
            float value) {

        showMessage(
                "亮度 "
                        + Math.round(
                        value * 100f)
                        + "%");
    }

    public void showVolume(
            float value) {

        showMessage(
                "音量 "
                        + Math.round(
                        value * 100f)
                        + "%");
    }

    public void showVideoSize(
            int width,
            int height) {

        showMessage(
                width
                        + " × "
                        + height);
    }

    public void setVideoSize(
            int width,
            int height) {

        showVideoSize(
                width,
                height);
    }

    public void showMessage(
            String value) {

        if (message == null) {
            return;
        }

        message.setText(
                value == null
                        ? ""
                        : value);

        message.setVisibility(
                View.VISIBLE);

        message.postDelayed(
                () -> {
                    if (message != null) {
                        message.setVisibility(
                                View.GONE);
                    }
                },
                1500L);
    }

    private String formatTime(
            long ms) {

        if (ms <= 0) {
            return "00:00";
        }

        long total =
                ms / 1000L;

        long seconds =
                total % 60L;

        long minutes =
                (total / 60L) % 60L;

        long hours =
                total / 3600L;

        if (hours > 0) {
            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds);
        }

        return String.format(
                "%02d:%02d",
                minutes,
                seconds);
    }

    public void detach() {

        if (root != null &&
                panel != null) {

            root.removeView(panel);
        }

        panel = null;
        title = null;
        progress = null;
        message = null;
    }
}