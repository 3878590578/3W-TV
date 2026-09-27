package com.threew.tv.ui;

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

import com.threew.tv.download.DownloadManager;
import com.threew.tv.model.DownloadItem;
import com.threew.tv.utils.FormatUtils;

import java.util.List;

/**
 * 3W影视 - 下载管理
 *
 * 功能：
 * 1. 查看下载任务
 * 2. 显示下载进度
 * 3. 暂停
 * 4. 继续
 * 5. 重试
 * 6. 删除
 * 7. 显示下载状态、大小和错误信息
 */
public class DownloadActivity extends AppCompatActivity {

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int CARD_2 = Color.rgb(27, 30, 40);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);
    private static final int DANGER = Color.rgb(255, 90, 90);

    private LinearLayout listContainer;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadManager = new DownloadManager(this);

        buildUi();
        loadDownloads();
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
        bar.setPadding(
                dp(8),
                dp(6),
                dp(10),
                dp(6)
        );
        bar.setBackgroundColor(
                Color.rgb(13, 16, 23)
        );

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
        title.setText("下载管理");
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

        TextView refresh = new TextView(this);
        refresh.setText("刷新");
        refresh.setTextColor(ACCENT);
        refresh.setTextSize(14);
        refresh.setGravity(Gravity.CENTER);

        refresh.setOnClickListener(
                v -> loadDownloads()
        );

        bar.addView(
                refresh,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(50)
                )
        );

        return bar;
    }

    private void loadDownloads() {

        if (listContainer == null) {
            return;
        }

        listContainer.removeAllViews();

        List<DownloadItem> list;

        try {
            list = downloadManager.getAllDownloads();
        } catch (Exception e) {
            list = null;
        }

        if (list == null || list.isEmpty()) {

            TextView empty = new TextView(this);

            empty.setText("暂无下载任务");
            empty.setTextColor(SUB_TEXT);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(
                    0,
                    dp(80),
                    0,
                    dp(80)
            );

            listContainer.addView(empty);

            return;
        }

        for (DownloadItem item : list) {

            if (item == null) {
                continue;
            }

            listContainer.addView(
                    createDownloadItem(item)
            );
        }
    }

    private View createDownloadItem(
            DownloadItem item
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

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

        cardLp.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        card.setLayoutParams(cardLp);

        TextView title = new TextView(this);

        String titleText =
                item.getVideoName();

        if (TextUtils.isEmpty(titleText)) {
            titleText = "未知影片";
        }

        String episodeName =
                item.getEpisodeName();

        if (!TextUtils.isEmpty(episodeName)) {
            titleText += "  ·  " + episodeName;
        }

        title.setText(titleText);
        title.setTextColor(TEXT);
        title.setTextSize(16);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        title.setSingleLine(true);
        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        card.addView(title);

        TextView status =
                new TextView(this);

        status.setText(
                getStatusText(item)
        );
        status.setTextColor(
                getStatusColor(item)
        );
        status.setTextSize(13);

        status.setPadding(
                0,
                dp(7),
                0,
                dp(3)
        );

        card.addView(status);

        TextView progress =
                new TextView(this);

        int percent =
                item.getProgress();

        long downloaded =
                item.getDownloadedBytes();

        long total =
                item.getTotalBytes();

        String sizeText;

        if (total > 0) {

            sizeText =
                    FormatUtils.formatFileSize(
                            downloaded
                    )
                            + " / "
                            + FormatUtils.formatFileSize(
                            total
                    );

        } else {

            sizeText =
                    FormatUtils.formatFileSize(
                            downloaded
                    );
        }

        progress.setText(
                percent + "%  ·  " + sizeText
        );

        progress.setTextColor(
                SUB_TEXT
        );

        progress.setTextSize(12);

        card.addView(progress);

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actions.setGravity(
                Gravity.CENTER_VERTICAL
        );

        actions.setPadding(
                0,
                dp(8),
                0,
                0
        );

        addActionButton(
                actions,
                getPrimaryActionText(item),
                ACCENT,
                v -> handlePrimaryAction(item)
        );

        addActionButton(
                actions,
                "重试",
                SUB_TEXT,
                v -> retry(item)
        );

        addActionButton(
                actions,
                "删除",
                DANGER,
                v -> delete(item)
        );

        card.addView(actions);

        return card;
    }

    private String getStatusText(
            DownloadItem item
    ) {

        if (item.isWaiting()) {
            return "等待中";
        }

        if (item.isDownloading()) {
            return "下载中";
        }

        if (item.isPaused()) {
            return "已暂停";
        }

        if (item.isCompleted()) {
            return "已完成";
        }

        if (item.isFailed()) {
            return "下载失败";
        }

        if (item.isDeleted()) {
            return "已删除";
        }

        return "未知状态";
    }

    private int getStatusColor(
            DownloadItem item
    ) {

        if (item.isCompleted()) {
            return ACCENT;
        }

        if (item.isFailed()) {
            return DANGER;
        }

        return SUB_TEXT;
    }

    private String getPrimaryActionText(
            DownloadItem item
    ) {

        if (item.isDownloading()) {
            return "暂停";
        }

        if (item.isPaused()) {
            return "继续";
        }

        if (item.isWaiting()) {
            return "暂停";
        }

        if (item.isFailed()) {
            return "继续";
        }

        if (item.isCompleted()) {
            return "已完成";
        }

        return "继续";
    }

    private void handlePrimaryAction(
            DownloadItem item
    ) {

        try {

            if (item.isDownloading() ||
                    item.isWaiting()) {

                downloadManager.pause(
                        item.getId()
                );

                showMessage("已暂停");

            } else if (item.isPaused()) {

                downloadManager.resume(
                        item.getId()
                );

                showMessage("已继续");

            } else if (item.isFailed()) {

                downloadManager.retry(
                        item.getId()
                );

                showMessage("已加入重试");

            }

            loadDownloads();

        } catch (Exception e) {

            showMessage("操作失败");
        }
    }

    private void retry(
            DownloadItem item
    ) {

        try {

            downloadManager.retry(
                    item.getId()
            );

            showMessage("已加入重试");

            loadDownloads();

        } catch (Exception e) {

            showMessage("重试失败");
        }
    }

    private void delete(
            DownloadItem item
    ) {

        try {

            downloadManager.delete(
                    item.getId()
            );

            showMessage("已删除");

            loadDownloads();

        } catch (Exception e) {

            showMessage("删除失败");
        }
    }

    private void addActionButton(
            LinearLayout container,
            String text,
            int textColor,
            View.OnClickListener listener
    ) {

        TextView button =
                new TextView(this);

        button.setText(text);
        button.setTextColor(textColor);
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER);
        button.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );
        button.setBackgroundColor(CARD_2);

        button.setOnClickListener(listener);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(38)
                );

        lp.setMargins(
                0,
                0,
                dp(8),
                0
        );

        container.addView(
                button,
                lp
        );
    }

    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listContainer != null) {
            loadDownloads();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            downloadManager.shutdown();
        } catch (Exception ignored) {
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
