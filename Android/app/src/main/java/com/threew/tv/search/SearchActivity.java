package com.threew.tv.search;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.model.Video;
import com.threew.tv.ui.DetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 3W影视搜索页面
 *
 * 功能：
 * - 多源搜索
 * - 同名结果合并
 * - 搜索历史
 * - 点击进入详情
 * - 清空搜索历史
 * - 搜索过程中显示已返回结果
 */
public class SearchActivity extends AppCompatActivity
        implements SourceSearchManager.Listener {

    private EditText searchInput;

    private LinearLayout resultContainer;

    private ProgressBar progressBar;

    private TextView statusText;

    private SourceSearchManager searchManager;

    private SearchHistoryManager historyManager;

    private String currentKeyword = "";

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        searchManager =
                new SourceSearchManager(this);

        searchHistoryManager();

        searchManager.setListener(this);

        buildPage();

        String keyword =
                getIntent().getStringExtra(
                        "keyword"
                );

        if (keyword != null &&
                !keyword.trim().isEmpty()) {

            searchInput.setText(keyword);

            search(
                    keyword
            );
        } else {

            showHistory();
        }
    }

    private void searchHistoryManager() {

        historyManager =
                new SearchHistoryManager(this);
    }

    // =========================================================
    // 页面
    // =========================================================

    private void buildPage() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        root.setBackgroundColor(
                0xFF070B12
        );

        // -----------------------------------------------------
        // 搜索栏
        // -----------------------------------------------------

        LinearLayout searchBar =
                new LinearLayout(this);

        searchBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        searchInput =
                new EditText(this);

        searchInput.setSingleLine(true);

        searchInput.setHint(
                "搜索影视名称"
        );

        searchInput.setHintTextColor(
                0xFF697487
        );

        searchInput.setTextColor(
                0xFFFFFFFF
        );

        searchInput.setTextSize(
                16
        );

        searchInput.setImeOptions(
                EditorInfo.IME_ACTION_SEARCH
        );

        searchInput.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        searchBar.addView(
                searchInput,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        TextView searchButton =
                button("搜索");

        searchButton.setOnClickListener(
                v -> search(
                        searchInput.getText()
                                .toString()
                )
        );

        searchBar.addView(
                searchButton,
                new LinearLayout.LayoutParams(
                        dp(76),
                        dp(44)
                )
        );

        root.addView(
                searchBar
        );

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId ==
                            EditorInfo.IME_ACTION_SEARCH ||
                            (
                                    event != null &&
                                    event.getKeyCode() ==
                                            KeyEvent.KEYCODE_ENTER &&
                                    event.getAction() ==
                                            KeyEvent.ACTION_DOWN
                            )) {

                        search(
                                searchInput.getText()
                                        .toString()
                        );

                        return true;
                    }

                    return false;
                }
        );

        // -----------------------------------------------------
        // 状态
        // -----------------------------------------------------

        LinearLayout statusRow =
                new LinearLayout(this);

        statusRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        statusText =
                text(
                        "",
                        12,
                        0xFF8993A5
                );

        statusRow.addView(
                statusText,
                new LinearLayout.LayoutParams(
                        0,
                        dp(36),
                        1
                )
        );

        TextView historyButton =
                button("历史");

        historyButton.setOnClickListener(
                v -> showHistoryDialog()
        );

        statusRow.addView(
                historyButton,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(34)
                )
        );

        root.addView(
                statusRow
        );

        // -----------------------------------------------------
        // 进度
        // -----------------------------------------------------

        progressBar =
                new ProgressBar(this);

        progressBar.setVisibility(
                View.GONE
        );

        root.addView(
                progressBar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(4)
                )
        );

        // -----------------------------------------------------
        // 结果
        // -----------------------------------------------------

        ScrollView scroll =
                new ScrollView(this);

        resultContainer =
                new LinearLayout(this);

        resultContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(
                resultContainer
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    // =========================================================
    // 搜索
    // =========================================================

    private void search(
            String keyword) {

        if (keyword == null) {
            return;
        }

        keyword =
                keyword.trim();

        if (keyword.isEmpty()) {

            Toast.makeText(
                    this,
                    "请输入影视名称",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        currentKeyword =
                keyword;

        historyManager.add(
                keyword
        );

        resultContainer.removeAllViews();

        progressBar.setVisibility(
                View.VISIBLE
        );

        statusText.setText(
                "正在搜索：" + keyword
        );

        searchManager.search(
                keyword
        );
    }

    // =========================================================
    // 搜索回调
    // =========================================================

    @Override
    public void onSearchStarted(
            String keyword,
            int sourceCount) {

        runOnUiThread(() -> {

            progressBar.setVisibility(
                    View.VISIBLE
            );

            statusText.setText(
                    "正在搜索 " +
                            sourceCount +
                            " 个视频源…"
            );
        });
    }

    @Override
    public void onSourceStarted(
            com.threew.tv.model.VideoSource source) {

        // 单源状态不直接刷新 UI，
        // 避免多个线程频繁刷新页面。
    }

    @Override
    public void onSourceFinished(
            com.threew.tv.model.VideoSource source,
            int resultCount,
            long responseTimeMs) {

        // 单源结果由 onResult 统一更新。
    }

    @Override
    public void onResult(
            List<Video> results,
            boolean finished) {

        runOnUiThread(() -> {

            showResults(
                    results
            );

            if (finished) {

                progressBar.setVisibility(
                        View.GONE
                );
            }
        });
    }

    @Override
    public void onFinished(
            List<Video> results) {

        runOnUiThread(() -> {

            progressBar.setVisibility(
                    View.GONE
            );

            int count =
                    results == null
                            ? 0
                            : results.size();

            statusText.setText(
                    count == 0
                            ? "没有找到相关内容"
                            : "找到 " +
                              count +
                              " 个结果"
            );
        });
    }

    @Override
    public void onError(
            String message) {

        runOnUiThread(() -> {

            progressBar.setVisibility(
                    View.GONE
            );

            statusText.setText(
                    message == null
                            ? "搜索失败"
                            : message
            );

            if (resultContainer
                    .getChildCount() == 0) {

                showEmpty(
                        message
                );
            }
        });
    }

    // =========================================================
    // 显示结果
    // =========================================================

    private void showResults(
            List<Video> results) {

        resultContainer.removeAllViews();

        if (results == null ||
                results.isEmpty()) {

            showEmpty(
                    "暂无搜索结果"
            );

            return;
        }

        for (Video video :
                results) {

            if (video == null) {
                continue;
            }

            resultContainer.addView(
                    createVideoCard(
                            video
                    )
            );
        }
    }

    private View createVideoCard(
            Video video) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        card.setBackgroundColor(
                0xFF101722
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(100)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        card.setLayoutParams(params);

        // -----------------------------------------------------
        // 左侧编号
        // -----------------------------------------------------

        TextView index =
                text(
                        "▶",
                        18,
                        0xFF6FC3FF
                );

        index.setGravity(
                Gravity.CENTER
        );

        card.addView(
                index,
                new LinearLayout.LayoutParams(
                        dp(42),
                        -1
                )
        );

        // -----------------------------------------------------
        // 中间信息
        // -----------------------------------------------------

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        info.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                text(
                        video.getName(),
                        17,
                        0xFFFFFFFF
                );

        info.addView(
                name
        );

        StringBuilder meta =
                new StringBuilder();

        if (!isEmpty(video.getYear())) {

            meta.append(
                    video.getYear()
            );
        }

        if (!isEmpty(video.getArea())) {

            appendSeparator(
                    meta
            );

            meta.append(
                    video.getArea()
            );
        }

        if (!isEmpty(video.getCategory())) {

            appendSeparator(
                    meta
            );

            meta.append(
                    video.getCategory()
            );
        }

        TextView metadata =
                text(
                        meta.toString(),
                        12,
                        0xFF8993A5
                );

        metadata.setPadding(
                0,
                dp(5),
                0,
                0
        );

        info.addView(
                metadata
        );

        String remark =
                video.getRemarks();

        if (isEmpty(remark) &&
                video.getEpisodes() != null) {

            remark =
                    video.getEpisodes().size() +
                    " 集";
        }

        if (!isEmpty(remark)) {

            TextView remarks =
                    text(
                            remark,
                            12,
                            0xFF6F7B8F
                    );

            remarks.setPadding(
                    0,
                    dp(3),
                    0,
                    0
            );

            info.addView(
                    remarks
            );
        }

        card.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        // -----------------------------------------------------
        // 右侧箭头
        // -----------------------------------------------------

        TextView arrow =
                text(
                        "›",
                        28,
                        0xFF697487
                );

        arrow.setGravity(
                Gravity.CENTER
        );

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(34),
                        -1
                )
        );

        card.setOnClickListener(
                v -> openDetail(
                        video
                )
        );

        return card;
    }

    private void openDetail(
            Video video) {

        Intent intent =
                new Intent(
                        this,
                        DetailActivity.class
                );

        /*
         * Gson 直接传输会让 Intent 过大，
         * 所以这里优先传视频基本信息和来源 ID。
         */
        intent.putExtra(
                "video_id",
                video.getId()
        );

        intent.putExtra(
                "video_name",
                video.getName()
        );

        intent.putExtra(
                "source_id",
                video.getSourceId()
        );

        intent.putExtra(
                "source_name",
                video.getSourceName()
        );

        intent.putExtra(
                "poster",
                video.getPoster()
        );

        intent.putExtra(
                "year",
                video.getYear()
        );

        intent.putExtra(
                "area",
                video.getArea()
        );

        intent.putExtra(
                "category",
                video.getCategory()
        );

        startActivity(intent);
    }

    // =========================================================
    // 搜索历史
    // =========================================================

    private void showHistory() {

        List<String> history =
                historyManager.getAll();

        if (history == null ||
                history.isEmpty()) {

            showEmpty(
                    "搜索影视名称后，结果会显示在这里"
            );

            statusText.setText(
                    "搜索历史为空"
            );

            return;
        }

        resultContainer.removeAllViews();

        statusText.setText(
                "最近搜索"
        );

        for (String keyword :
                history) {

            TextView item =
                    button(keyword);

            item.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            item.setPadding(
                    dp(16),
                    0,
                    dp(16),
                    0
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(52)
                    );

            params.setMargins(
                    0,
                    0,
                    0,
                    dp(6)
            );

            item.setLayoutParams(
                    params
            );

            item.setOnClickListener(
                    v -> {

                        searchInput.setText(
                                keyword
                        );

                        search(
                                keyword
                        );
                    }
            );

            resultContainer.addView(
                    item
            );
        }
    }

    private void showHistoryDialog() {

        List<String> history =
                historyManager.getAll();

        if (history == null ||
                history.isEmpty()) {

            Toast.makeText(
                    this,
                    "暂无搜索历史",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final String[] items =
                history.toArray(
                        new String[0]
                );

        new AlertDialog.Builder(this)
                .setTitle("搜索历史")
                .setItems(
                        items,
                        (dialog, which) -> {

                            String keyword =
                                    items[which];

                            searchInput.setText(
                                    keyword
                            );

                            search(
                                    keyword
                            );
                        }
                )
                .setNegativeButton(
                        "关闭",
                        null
                )
                .setNeutralButton(
                        "清空历史",
                        (dialog, which) -> {

                            historyManager.clear();

                            showHistory();

                            Toast.makeText(
                                    this,
                                    "搜索历史已清空",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    // =========================================================
    // 空状态
    // =========================================================

    private void showEmpty(
            String message) {

        resultContainer.removeAllViews();

        TextView empty =
                text(
                        message,
                        15,
                        0xFF8993A5
                );

        empty.setGravity(
                Gravity.CENTER
        );

        empty.setPadding(
                dp(20),
                dp(80),
                dp(20),
                dp(80)
        );

        resultContainer.addView(
                empty,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }

    // =========================================================
    // 工具
    // =========================================================

    private void appendSeparator(
            StringBuilder builder) {

        if (builder.length() > 0) {
            builder.append(" · ");
        }
    }

    private boolean isEmpty(
            String value) {

        return value == null ||
                value.trim().isEmpty();
    }

    private TextView text(
            String value,
            float size,
            int color) {

        TextView view =
                new TextView(this);

        view.setText(
                value == null
                        ? ""
                        : value
        );

        view.setTextSize(size);
        view.setTextColor(color);

        return view;
    }

    private TextView button(
            String value) {

        TextView view =
                text(
                        value,
                        13,
                        0xFFE6ECF5
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setBackgroundColor(
                0xFF182231
        );

        return view;
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (searchManager != null) {
            searchManager.shutdown();
        }
    }
}
