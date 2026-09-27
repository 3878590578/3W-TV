package com.threew.tv.player;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 播放器右上角时钟管理器。
 *
 * 默认开启。
 * 控制栏隐藏后仍可由上层 UI 决定是否继续显示。
 */
public class PlayerClockManager {

    public interface Listener {
        void onTimeChanged(String time);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateNow();

            if (running) {
                handler.postDelayed(this, 1000L);
            }
        }
    };

    private Listener listener;

    private boolean running;
    private boolean enabled = true;

    private String timeFormat = "HH:mm";

    private String currentTime = "";

    public PlayerClockManager() {
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    /**
     * 开始更新时间。
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;

        handler.removeCallbacks(clockRunnable);
        handler.post(clockRunnable);
    }

    /**
     * 停止更新时间。
     */
    public void stop() {
        running = false;
        handler.removeCallbacks(clockRunnable);
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 开关时钟。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            currentTime = "";

            if (listener != null) {
                listener.onTimeChanged("");
            }

            return;
        }

        updateNow();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        setEnabled(true);
    }

    public void disable() {
        setEnabled(false);
    }

    /**
     * 设置时间格式。
     *
     * 默认 HH:mm。
     *
     * 常见格式：
     * HH:mm
     * HH:mm:ss
     * h:mm a
     */
    public void setTimeFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return;
        }

        timeFormat = format.trim();
        updateNow();
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    /**
     * 获取当前显示时间。
     */
    public String getCurrentTime() {
        return currentTime;
    }

    /**
     * 立即刷新一次。
     */
    public void updateNow() {
        if (!enabled) {
            return;
        }

        try {
            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            timeFormat,
                            Locale.getDefault()
                    );

            currentTime = formatter.format(new Date());

        } catch (IllegalArgumentException e) {
            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    );

            currentTime = formatter.format(new Date());
        }

        if (listener != null) {
            listener.onTimeChanged(currentTime);
        }
    }

    /**
     * 判断当前时间格式是否包含秒。
     */
    public boolean showsSeconds() {
        return timeFormat.contains("s");
    }

    /**
     * 恢复默认时间格式。
     */
    public void resetFormat() {
        timeFormat = "HH:mm";
        updateNow();
    }

    /**
     * 释放资源。
     */
    public void release() {
        stop();
        listener = null;
    }
}
