package com.threew.tv.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.threew.tv.model.Video;
import com.threew.tv.utils.FormatUtils;

import java.util.Locale;

/**
 * 3W影视播放器信息层
 *
 * 负责：
 * 1. 播放器顶部时钟
 * 2. 左下角视频信息
 * 3. 手势提示
 * 4. 播放错误提示
 * 5. 自动下一集倒计时
 * 6. 临时倍速提示
 * 7. 缓冲提示
 *
 * 注意：
 * 本类只负责 Overlay UI，
 * 不直接控制播放器播放状态。
 */
public class PlayerOverlay {

    public interface Listener {

        void onNextEpisode();

        void onCancelNextEpisode();

        void onRetry();

        void onDismissMessage();
    }

    private static final long MESSAGE_DEFAULT_DURATION = 1200L;
    private static final long ERROR_MESSAGE_DURATION = 5000L;

    private final Context context;
    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final PlayerSettings settings;

    private FrameLayout root;

    private TextView clockView;
    private TextView infoView;
    private TextView gestureView;
    private TextView messageView;
    private TextView speedView;

    private TextView errorTitleView;
    private TextView errorDetailView;
    private TextView retryView;

    private LinearLayout nextPanel;
    private TextView nextTitleView;
    private TextView nextCountdownView;
    private TextView nextCancelView;

    private ProgressBar loadingView;

    private Listener listener;

    private Runnable clockRunnable;
    private Runnable messageHideRunnable;
    private Runnable nextCountdownRunnable;
    private Runnable nextTimeoutRunnable;

    private int nextSeconds = 5;

    private boolean attached;

    public PlayerOverlay(Context context) {

        this.context =
                context.getApplicationContext();

        settings =
                new PlayerSettings(this.context);
    }

    // ============================================================
    // 创建
    // ============================================================

    /**
     * 创建播放器 Overlay。
     *
     * root 应该是 PlayerActivity 中覆盖播放器的 FrameLayout。
     */
    public void attach(
            FrameLayout root
    ) {

        if (root == null) {
            return;
        }

        detach();

        this.root = root;
        this.attached = true;

        buildViews();

        applySettings();

        startClock();
    }

    public void detach() {

        stopClock();

        handler.removeCallbacksAndMessages(null);

        if (root != null) {

            root.removeView(
                    clockView
            );

            root.removeView(
                    infoView
            );

            root.removeView(
                    gestureView
            );

            root.removeView(
                    messageView
            );

            root.removeView(
                    speedView
            );

            root.removeView(
                    errorContainer()
            );

            root.removeView(
                    nextPanel
            );

            root.removeView(
                    loadingView
            );
        }

        root = null;
        attached = false;
    }

    public void setListener(
            Listener listener
    ) {
        this.listener = listener;
    }

    // ============================================================
    // UI 创建
    // ============================================================

    private void buildViews() {

        clockView =
                createTextView();

        infoView =
                createTextView();

        gestureView =
                createCenterTextView();

        messageView =
                createCenterTextView();

        speedView =
                createCenterTextView();

        errorTitleView =
                createTextView();

        errorDetailView =
                createTextView();

        retryView =
                createButton("重试");

        nextPanel =
                new LinearLayout(context);

        nextPanel.setOrientation(
                LinearLayout.VERTICAL
        );

        nextPanel.setGravity(
                Gravity.CENTER
        );

        nextPanel.setPadding(
                dp(22),
                dp(16),
                dp(22),
                dp(16)
        );

        nextPanel.setBackgroundColor(
                Color.argb(
                        220,
                        12,
                        16,
                        24
                )
        );

        nextTitleView =
                createTextView();

        nextCountdownView =
                createTextView();

        nextCancelView =
                createButton("取消");

        nextPanel.addView(
                nextTitleView,
                wrapContent()
        );

        nextPanel.addView(
                nextCountdownView,
                wrapContent()
        );

        nextPanel.addView(
                nextCancelView,
                wrapContent()
        );

        loadingView =
                new ProgressBar(
                        context
                );

        loadingView.setIndeterminate(true);

        addOverlayViews();
    }

    private void addOverlayViews() {

        addView(
                clockView,
                gravity(
                        Gravity.TOP |
                                Gravity.END,
                        dp(18),
                        dp(14),
                        dp(18),
                        dp(14)
                )
        );

        addView(
                infoView,
                gravity(
                        Gravity.BOTTOM |
                                Gravity.START,
                        dp(18),
                        dp(14),
                        dp(18),
                        dp(20)
                )
        );

        addView(
                gestureView,
                centerLayoutParams()
        );

        addView(
                messageView,
                centerLayoutParams()
        );

        addView(
                speedView,
                centerLayoutParams()
        );

        addView(
                loadingView,
                centerLayoutParams()
        );

        addView(
                nextPanel,
                centerPanelLayoutParams()
        );

        nextPanel.setVisibility(
                View.GONE
        );

        createErrorContainer();

        gestureView.setVisibility(
                View.GONE
        );

        messageView.setVisibility(
                View.GONE
        );

        speedView.setVisibility(
                View.GONE
        );

        loadingView.setVisibility(
                View.GONE
        );
    }

    private void createErrorContainer() {

        LinearLayout container =
                new LinearLayout(context);

        container.setTag(
                "threew_error_container"
        );

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setGravity(
                Gravity.CENTER
        );

        container.setPadding(
                dp(28),
                dp(20),
                dp(28),
                dp(20)
        );

        container.setBackgroundColor(
                Color.argb(
                        230,
                        12,
                        16,
                        24
                )
        );

        container.addView(
                errorTitleView,
                wrapContent()
        );

        container.addView(
                errorDetailView,
                wrapContent()
        );

        container.addView(
                retryView,
                wrapContent()
        );

        retryView.setOnClickListener(
                v -> {

                    hideError();

                    if (listener != null) {
                        listener.onRetry();
                    }
                }
        );

        addView(
                container,
                centerPanelLayoutParams()
        );

        container.setVisibility(
                View.GONE
        );
    }

    private View errorContainer() {

        if (root == null) {
            return null;
        }

        for (int i = 0;
             i < root.getChildCount();
             i++) {

            View child =
                    root.getChildAt(i);

            Object tag =
                    child.getTag();

            if ("threew_error_container"
                    .equals(tag)) {

                return child;
            }
        }

        return null;
    }

    // ============================================================
    // 时钟
    // ============================================================

    public void startClock() {

        stopClock();

        if (!settings.isClockEnabled()) {
            clockView.setVisibility(
                    View.GONE
            );
            return;
        }

        clockRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (!attached) {
                            return;
                        }

                        updateClock();

                        handler.postDelayed(
                                this,
                                1000L
                        );
                    }
                };

        handler.post(
                clockRunnable
        );
    }

    public void stopClock() {

        if (clockRunnable != null) {

            handler.removeCallbacks(
                    clockRunnable
            );

            clockRunnable = null;
        }
    }

    private void updateClock() {

        if (clockView == null) {
            return;
        }

        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        clockView.setText(
                format.format(
                        new java.util.Date()
                )
        );
    }

    // ============================================================
    // 视频信息
    // ============================================================

    public void showVideoInfo(
            Video video,
            String resolution,
            long fileSizeBytes
    ) {

        if (infoView == null) {
            return;
        }

        if (!settings.isInfoEnabled()) {

            infoView.setVisibility(
                    View.GONE
            );

            return;
        }

        StringBuilder text =
                new StringBuilder();

        if (resolution != null &&
                !resolution.trim().isEmpty()) {

            text.append(resolution.trim());
        }

        if (fileSizeBytes > 0) {

            if (text.length() > 0) {
                text.append("  ·  ");
            }

            text.append(
                    FormatUtils.formatFileSize(
                            fileSizeBytes
                    )
            );
        }

        if (video != null &&
                video.getName() != null &&
                !video.getName().trim().isEmpty()) {

            if (text.length() > 0) {
                text.append("  ·  ");
            }

            text.append(
                    video.getName().trim()
            );
        }

        if (text.length() == 0) {

            infoView.setVisibility(
                    View.GONE
            );

            return;
        }

        infoView.setText(
                text.toString()
        );

        infoView.setVisibility(
                View.VISIBLE
        );
    }

    public void hideVideoInfo() {

        if (infoView != null) {
            infoView.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // 手势提示
    // ============================================================

    public void showGesture(
            String message
    ) {

        if (gestureView == null) {
            return;
        }

        gestureView.setText(
                message == null
                        ? ""
                        : message
        );

        gestureView.setVisibility(
                View.VISIBLE
        );
    }

    public void hideGesture() {

        if (gestureView != null) {
            gestureView.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // 普通消息
    // ============================================================

    public void showMessage(
            String message
    ) {

        showMessage(
                message,
                MESSAGE_DEFAULT_DURATION
        );
    }

    public void showMessage(
            String message,
            long durationMs
    ) {

        if (messageView == null) {
            return;
        }

        if (message == null ||
                message.trim().isEmpty()) {

            hideMessage();
            return;
        }

        if (messageHideRunnable != null) {

            handler.removeCallbacks(
                    messageHideRunnable
            );
        }

        messageView.setText(
                message
        );

        messageView.setVisibility(
                View.VISIBLE
        );

        messageHideRunnable =
                this::hideMessage;

        handler.postDelayed(
                messageHideRunnable,
                Math.max(
                        300L,
                        durationMs
                )
        );
    }

    public void hideMessage() {

        if (messageView != null) {
            messageView.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // 倍速提示
    // ============================================================

    public void showTemporarySpeed(
            float speed
    ) {

        if (speedView == null) {
            return;
        }

        speedView.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f×",
                        speed
                )
        );

        speedView.setVisibility(
                View.VISIBLE
        );
    }

    public void hideTemporarySpeed() {

        if (speedView != null) {
            speedView.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // 缓冲
    // ============================================================

    public void showLoading() {

        if (loadingView != null) {
            loadingView.setVisibility(
                    View.VISIBLE
            );
        }
    }

    public void hideLoading() {

        if (loadingView != null) {
            loadingView.setVisibility(
                    View.GONE
            );
        }
    }

    public boolean isLoading() {

        return loadingView != null &&
                loadingView.getVisibility()
                        == View.VISIBLE;
    }

    // ============================================================
    // 播放错误
    // ============================================================

    public void showError(
            String title,
            String detail
    ) {

        View container =
                errorContainer();

        if (container == null) {
            return;
        }

        errorTitleView.setText(
                title == null ||
                        title.trim().isEmpty()
                        ? "播放失败"
                        : title
        );

        errorDetailView.setText(
                detail == null
                        ? ""
                        : detail
        );

        container.setVisibility(
                View.VISIBLE
        );

        handler.postDelayed(
                this::hideError,
                ERROR_MESSAGE_DURATION
        );
    }

    public void hideError() {

        View container =
                errorContainer();

        if (container != null) {
            container.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // 自动下一集
    // ============================================================

    public void showNextEpisode(
            String episodeName,
            int seconds
    ) {

        if (nextPanel == null) {
            return;
        }

        nextSeconds =
                Math.max(
                        1,
                        Math.min(
                                30,
                                seconds
                        )
                );

        nextTitleView.setText(
                episodeName == null ||
                        episodeName.trim().isEmpty()
                        ? "即将播放下一集"
                        : "下一集：" +
                                episodeName
        );

        updateNextCountdownText();

        nextPanel.setVisibility(
                View.VISIBLE
        );

        nextCancelView.setOnClickListener(
                v -> {

                    hideNextEpisode();

                    if (listener != null) {
                        listener.onCancelNextEpisode();
                    }
                }
        );

        if (nextCountdownRunnable != null) {

            handler.removeCallbacks(
                    nextCountdownRunnable
            );
        }

        nextCountdownRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (!attached ||
                                nextPanel.getVisibility()
                                        != View.VISIBLE) {

                            return;
                        }

                        nextSeconds--;

                        if (nextSeconds <= 0) {

                            hideNextEpisode();

                            if (listener != null) {
                                listener.onNextEpisode();
                            }

                            return;
                        }

                        updateNextCountdownText();

                        handler.postDelayed(
                                this,
                                1000L
                        );
                    }
                };

        handler.postDelayed(
                nextCountdownRunnable,
                1000L
        );
    }

    private void updateNextCountdownText() {

        nextCountdownView.setText(
                String.format(
                        Locale.getDefault(),
                        "%d 秒后自动播放",
                        nextSeconds
                )
        );
    }

    public void hideNextEpisode() {

        if (nextCountdownRunnable != null) {

            handler.removeCallbacks(
                    nextCountdownRunnable
            );

            nextCountdownRunnable = null;
        }

        if (nextTimeoutRunnable != null) {

            handler.removeCallbacks(
                    nextTimeoutRunnable
            );

            nextTimeoutRunnable = null;
        }

        if (nextPanel != null) {
            nextPanel.setVisibility(
                    View.GONE
            );
        }
    }

    public boolean isNextEpisodeVisible() {

        return nextPanel != null &&
                nextPanel.getVisibility()
                        == View.VISIBLE;
    }

    // ============================================================
    // 应用设置
    // ============================================================

    public void applySettings() {

        if (!attached) {
            return;
        }

        applyClockSettings();
        applyInfoSettings();
    }

    private void applyClockSettings() {

        if (!settings.isClockEnabled()) {

            clockView.setVisibility(
                    View.GONE
            );

            return;
        }

        clockView.setVisibility(
                View.VISIBLE
        );

        clockView.setTextSize(
                settings.getClockSize()
        );

        setGravity(
                clockView,
                settings.getClockPosition(),
                dp(18)
        );
    }

    private void applyInfoSettings() {

        if (!settings.isInfoEnabled()) {

            infoView.setVisibility(
                    View.GONE
            );

            return;
        }

        infoView.setTextSize(
                settings.getInfoSize()
        );

        setGravity(
                infoView,
                settings.getInfoPosition(),
                dp(18)
        );
    }

    // ============================================================
    // 基础 UI
    // ============================================================

    private TextView createTextView() {

        TextView view =
                new TextView(context);

        view.setTextColor(
                Color.WHITE
        );

        view.setTextSize(
                13
        );

        view.setGravity(
                Gravity.CENTER_VERTICAL
        );

        view.setTypeface(
                Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.NORMAL
                )
        );

        view.setShadowLayer(
                5f,
                1f,
                1f,
                Color.BLACK
        );

        return view;
    }

    private TextView createCenterTextView() {

        TextView view =
                createTextView();

        view.setTextSize(20);
        view.setGravity(
                Gravity.CENTER
        );

        view.setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
        );

        view.setBackgroundColor(
                Color.argb(
                        190,
                        8,
                        12,
                        18
                )
        );

        return view;
    }

    private TextView createButton(
            String text
    ) {

        TextView view =
                createTextView();

        view.setText(
                text
        );

        view.setGravity(
                Gravity.CENTER
        );

        view.setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
        );

        view.setTextSize(14);

        return view;
    }

    private void addView(
            View view,
            FrameLayout.LayoutParams params
    ) {

        if (root == null ||
                view == null) {
            return;
        }

        root.addView(
                view,
                params
        );
    }

    private FrameLayout.LayoutParams centerLayoutParams() {

        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
    }

    private FrameLayout.LayoutParams centerPanelLayoutParams() {

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                );

        params.leftMargin = dp(24);
        params.rightMargin = dp(24);

        return params;
    }

    private FrameLayout.LayoutParams gravity(
            int gravity,
            int left,
            int top,
            int right,
            int bottom
    ) {

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.gravity = gravity;
        params.leftMargin = left;
        params.topMargin = top;
        params.rightMargin = right;
        params.bottomMargin = bottom;

        return params;
    }

    private LinearLayout.LayoutParams wrapContent() {

        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setGravity(
            View view,
            String position,
            int margin
    ) {

        if (root == null ||
                view == null) {
            return;
        }

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        String value =
                position == null
                        ? ""
                        : position.toLowerCase();

        if ("top_left".equals(value)) {

            params.gravity =
                    Gravity.TOP |
                            Gravity.START;

        } else if ("top_right".equals(value)) {

            params.gravity =
                    Gravity.TOP |
                            Gravity.END;

        } else if ("bottom_right".equals(value)) {

            params.gravity =
                    Gravity.BOTTOM |
                            Gravity.END;

        } else {

            params.gravity =
                    Gravity.BOTTOM |
                            Gravity.START;
        }

        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.bottomMargin = margin;

        view.setLayoutParams(params);
    }

    private int dp(int value) {

        float density =
                context.getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }
}
