package com.threew.tv.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.model.History;
import com.threew.tv.player.PlayerActivity;
import com.threew.tv.history.HistoryManager;
import com.threew.tv.utils.FormatUtils;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);

    private LinearLayout listContainer;
    private HistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        historyManager = new HistoryManager(this);

        buildUi();
        loadHistory();
    }

    private void buildUi() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        root.addView(createTopBar());

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(30)
        );

        scrollView.addView(listContainer);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );
    }

    private View createTopBar() {

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(6), dp(10), dp(6));
        bar.setBackgroundColor(Color.rgb(13, 16, 23));

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(TEXT);
        back.setTextSize(36);
        back.setGravity(Gravity.CENTER);

        back.setOnClickListener(v -> finish());

        bar.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(50)
                )
        );

        TextView title = new TextView(this);
        title.setText("观看历史");
        title.setTextColor(TEXT);
        title.setTextSize(18);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        bar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView clear = new TextView(this);
        clear.setText("清空");
        clear.setTextColor(ACCENT);
        clear.setTextSize(14);
        clear.setGravity(Gravity.CENTER);

        clear.setOnClickListener(v -> clearHistory());

        bar.addView(
                clear,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(50)
                )
        );

        return bar;
    }

    private void loadHistory() {

        listContainer.removeAllViews();

        List<History> list;

        try {
            list = historyManager.getRecent(100);
        } catch (Exception e) {
            list = null;
        }

        if (list == null || list.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("暂无观看记录");
            empty.setTextColor(SUB_TEXT);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(80), 0, dp(80));

            listContainer.addView(empty);
            return;
        }

        for (History history : list) {

            if (history == null) {
                continue;
            }

            listContainer.addView(
                    createHistoryItem(history)
            );
        }
    }

    private View createHistoryItem(History history) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );
        card.setBackgroundColor(CARD);

        LinearLayout.LayoutParams cardLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardLp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardLp);

        TextView title = new TextView(this);

        String videoName = history.getVideoName();

        if (TextUtils.isEmpty(videoName)) {
            videoName = "未知影片";
        }

        title.setText(videoName);
        title.setTextColor(TEXT);
        title.setTextSize(16);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);

        card.addView(title);

        TextView episode = new TextView(this);

        String episodeName = history.getEpisodeName();

        if (TextUtils.isEmpty(episodeName)) {
            episodeName = "继续观看";
        }

        episode.setText(episodeName);
        episode.setTextColor(SUB_TEXT);
        episode.setTextSize(14);
        episode.setPadding(0, dp(7), 0, dp(4));

        card.addView(episode);

        TextView progress = new TextView(this);

        long position = history.getPositionMs();
        long duration = history.getDurationMs();

        String progressText;

        if (duration > 0) {

            int percent = FormatUtils.progress(
                    position,
                    duration
            );

            progressText =
                    "观看进度 " +
                    percent +
                    "%  ·  " +
                    FormatUtils.formatTime(position) +
                    " / " +
                    FormatUtils.formatTime(duration);

        } else {

            progressText =
                    "观看至 " +
                    FormatUtils.formatTime(position);
        }

        progress.setText(progressText);
        progress.setTextColor(SUB_TEXT);
        progress.setTextSize(12);

        card.addView(progress);

        TextView action = new TextView(this);
        action.setText(
                history.isCompleted()
                        ? "重新播放"
                        : "继续播放"
        );
        action.setTextColor(ACCENT);
        action.setTextSize(14);
        action.setGravity(Gravity.CENTER);
        action.setPadding(0, dp(10), 0, 0);

        card.addView(action);

        View.OnClickListener listener =
                v -> playHistory(history);

        card.setOnClickListener(listener);
        action.setOnClickListener(listener);

        card.setFocusable(true);
        card.setClickable(true);

        return card;
    }

    private void playHistory(History history) {

        if (history == null ||
                TextUtils.isEmpty(history.getPlayUrl())) {

            Toast.makeText(
                    this,
                    "播放地址已失效",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent = new Intent(
                this,
                PlayerActivity.class
        );

        intent.putExtra(
                "video_id",
                history.getVideoId()
        );

        intent.putExtra(
                "video_name",
                history.getVideoName()
        );

        intent.putExtra(
                "episode_id",
                history.getEpisodeId()
        );

        intent.putExtra(
                "episode_name",
                history.getEpisodeName()
        );

        intent.putExtra(
                "episode_number",
                history.getEpisodeNumber()
        );

        intent.putExtra(
                "play_url",
                history.getPlayUrl()
        );

        intent.putExtra(
                "position_ms",
                history.getResumePositionMs()
        );

        intent.putExtra(
                "duration_ms",
                history.getDurationMs()
        );

        intent.putExtra(
                "speed",
                history.getSpeed()
        );

        intent.putExtra(
                "source_id",
                String.valueOf(history.getVideoId())
        );

        startActivity(intent);
    }

    private void clearHistory() {

        try {
            historyManager.clearAll();

            Toast.makeText(
                    this,
                    "观看历史已清空",
                    Toast.LENGTH_SHORT
            ).show();

            loadHistory();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "清空失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listContainer != null) {
            loadHistory();
        }
    }

    @Override
    protected void onDestroy() {
        if (historyManager != null) {
            historyManager.shutdown();
        }

        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}
