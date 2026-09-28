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
import com.threew.tv.source.SourceSearchManager;
import com.threew.tv.ui.DetailActivity;

import java.util.ArrayList;
import java.util.List;

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
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        searchManager =
                new SourceSearchManager(this);

        historyManager =
                new SearchHistoryManager(this);

        searchManager.setListener(this);

        buildPage();

        String keyword =
                getIntent().getStringExtra(
                        "keyword"
                );

        if (keyword != null &&
                !keyword.trim().isEmpty()) {

            searchInput.setText(keyword);
            search(keyword);

        } else {
            showHistory();
        }
    }

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

        LinearLayout searchBar =
                new LinearLayout(this);

        searchBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        searchInput =
                new EditText(this);

        searchInput.setSingleLine(true);
        searchInput.setHint("搜索影视名称");
        searchInput.setHintTextColor(
                0xFF697487
        );
        searchInput.setTextColor(
                0xFFFFFFFF
        );
        searchInput.setTextSize(16);
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
                        searchInput
                                .getText()
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

        root.addView(searchBar);

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
                                searchInput
                                        .getText()
                                        .toString()
                        );

                        return true;
                    }

                    return false;
                }
        );

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

        root.addView(statusRow);

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

    private void search(String keyword) {
        if (keyword == null) {
            return;
        }

        keyword = keyword.trim();

        if (keyword.isEmpty()) {
            Toast.makeText(
                    this,
                    "请输入搜索内容",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        currentKeyword = keyword;

        resultContainer.removeAllViews();

        progressBar.setVisibility(
                View.VISIBLE
        );

        statusText.setText(
                "正在搜索：" + keyword
        );

        historyManager.add(keyword);

        searchManager.search(keyword);
    }

    @Override
    public void onSearchStarted(
            String keyword
    ) {
        runOnUiThread(() -> {
            progressBar.setVisibility(
                    View.VISIBLE
            );

            statusText.setText(
                    "正在搜索：" + keyword
            );
        });
    }

    @Override
    public void onSearchResult(
            Video video
    ) {
        runOnUiThread(() -> {
            if (video == null) {
                return;
            }

            addResult(video);
        });
    }

    @Override
    public void onSearchCompleted(
            List<Video> videos
    ) {
        runOnUiThread(() -> {
            progressBar.setVisibility(
                    View.GONE
            );

            int count =
                    videos == null
                            ? 0
                            : videos.size();

            statusText.setText(
                    "搜索完成，共 " +
                            count +
                            " 个结果"
            );
        });
    }

    @Override
    public void onSearchError(
            String message
    ) {
        runOnUiThread(() -> {
            progressBar.setVisibility(
                    View.GONE
            );

            statusText.setText(
                    message == null
                            ? "搜索失败"
                            : message
            );
        });
    }

    private void addResult(Video video) {
        TextView item =
                text(
                        video.getName(),
                        16,
                        0xFFFFFFFF
                );

        item.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        item.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    SearchActivity.this,
                                    DetailActivity.class
                            );

                    intent.putExtra(
                            "video_id",
                            video.getId()
                    );

                    startActivity(intent);
                }
        );

        resultContainer.addView(
                item,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }

    private void showHistory() {
        if (historyManager == null) {
            return;
        }

        List<String> history =
                historyManager.getAll();

        resultContainer.removeAllViews();

        if (history == null ||
                history.isEmpty()) {

            statusText.setText(
                    "暂无搜索历史"
            );

            return;
        }

        statusText.setText(
                "搜索历史"
        );

        for (String item : history) {

            TextView view =
                    text(
                            item,
                            15,
                            0xFFFFFFFF
                    );

            view.setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
            );

            view.setOnClickListener(
                    v -> {
                        searchInput.setText(item);
                        search(item);
                    }
            );

            resultContainer.addView(view);
        }
    }

    private void showHistoryDialog() {
        if (historyManager == null) {
            return;
        }

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

        String[] items =
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

                            search(keyword);
                        }
                )
                .setNegativeButton(
                        "关闭",
                        null
                )
                .setNeutralButton(
                        "清空",
                        (dialog, which) -> {
                            historyManager.clear();
                            showHistory();
                        }
                )
                .show();
    }

    private TextView button(String value) {
        TextView view =
                text(
                        value,
                        15,
                        0xFFFFFFFF
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setBackgroundColor(
                0xFF1677FF
        );

        return view;
    }

    private TextView text(
            String value,
            int size,
            int color
    ) {
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

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    protected void onDestroy() {
        if (searchManager != null) {
            searchManager.release();
        }

        super.onDestroy();
    }
}