package com.threew.tv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.model.VideoSource;
import com.threew.tv.source.SourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源管理页面
 *
 * 功能：
 * - 查看全部视频源
 * - 启用 / 禁用
 * - 设置默认源
 * - 调整优先级
 * - 测试视频源
 * - 添加 API
 * - 编辑 API
 * - 删除 API
 * - 手动导入
 *
 * 订阅管理独立于本页面。
 */
public class SourceActivity extends AppCompatActivity {

    private LinearLayout container;

    private ProgressBar progressBar;

    private SourceManager sourceManager;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );

        sourceManager =
                new SourceManager(this);

        buildPage();

        loadSources();
    }

    // =========================================================
    // 页面
    // =========================================================

    private void buildPage() {

        LinearLayout root =
                new LinearLayout(
                        this
                );

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        root.setBackgroundColor(
                0xFF070B12
        );

        // -----------------------------------------------------
        // 顶部
        // -----------------------------------------------------

        LinearLayout header =
                new LinearLayout(
                        this
                );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "视频源",
                        22,
                        0xFFFFFFFF
                );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView add =
                button(
                        "+ 添加"
                );

        add.setOnClickListener(
                v -> showAddDialog()
        );

        header.addView(
                add,
                new LinearLayout.LayoutParams(
                        dp(88),
                        dp(44)
                )
        );

        root.addView(
                header
        );

        // -----------------------------------------------------
        // 提示
        // -----------------------------------------------------

        TextView tip =
                text(
                        "启用的视频源会参与搜索。默认源用于优先选择播放来源。",
                        13,
                        0xFF8993A5
                );

        tip.setPadding(
                dp(4),
                dp(2),
                dp(4),
                dp(12)
        );

        root.addView(
                tip
        );

        // -----------------------------------------------------
        // 进度
        // -----------------------------------------------------

        progressBar =
                new ProgressBar(
                        this
                );

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
        // 列表
        // -----------------------------------------------------

        ScrollView scroll =
                new ScrollView(
                        this
                );

        container =
                new LinearLayout(
                        this
                );

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(
                container
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(
                root
        );
    }

    // =========================================================
    // 加载
    // =========================================================

    private void loadSources() {

        showLoading(true);

        container.removeAllViews();

        List<VideoSource> sources =
                sourceManager.getAll();

        showLoading(false);

        if (sources == null ||
                sources.isEmpty()) {

            TextView empty =
                    text(
                            "暂无视频源\n点击右上角「+ 添加」添加 CMS API",
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

            container.addView(
                    empty
            );

            return;
        }

        for (VideoSource source :
                sources) {

            if (source == null) {
                continue;
            }

            container.addView(
                    createSourceCard(
                            source
                    )
            );
        }
    }

    // =========================================================
    // 单个源
    // =========================================================

    private View createSourceCard(
            VideoSource source) {

        LinearLayout card =
                new LinearLayout(
                        this
                );

        card.setOrientation(
                LinearLayout.VERTICAL
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

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        card.setLayoutParams(
                cardParams
        );

        // -----------------------------------------------------
        // 第一行
        // -----------------------------------------------------

        LinearLayout top =
                new LinearLayout(
                        this
                );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                text(
                        source.getDisplayName(),
                        17,
                        0xFFFFFFFF
                );

        top.addView(
                name,
                new LinearLayout.LayoutParams(
                        0,
                        dp(34),
                        1
                )
        );

        TextView status =
                text(
                        source.isEnabled()
                                ? "已启用"
                                : "已停用",
                        12,
                        source.isEnabled()
                                ? 0xFF58D68D
                                : 0xFF8993A5
                );

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                dp(10),
                0,
                dp(10),
                0
        );

        top.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(30)
                )
        );

        card.addView(
                top
        );

        // -----------------------------------------------------
        // API
        // -----------------------------------------------------

        TextView api =
                text(
                        source.getApiUrl(),
                        12,
                        0xFF8993A5
                );

        api.setPadding(
                0,
                dp(4),
                0,
                dp(8)
        );

        card.addView(
                api
        );

        // -----------------------------------------------------
        // 信息
        // -----------------------------------------------------

        StringBuilder info =
                new StringBuilder();

        info.append("优先级 ")
                .append(source.getPriority());

        if (source.isDefaultSource()) {

            info.append("   ·   默认源");
        }

        if (source.isCmsSource()) {

            info.append("   ·   CMS");
        }

        TextView meta =
                text(
                        info.toString(),
                        12,
                        0xFF6F7B8F
                );

        card.addView(
                meta
        );

        // -----------------------------------------------------
        // 按钮
        // -----------------------------------------------------

        LinearLayout actions =
                new LinearLayout(
                        this
                );

        actions.setGravity(
                Gravity.CENTER_VERTICAL
        );

        actions.setPadding(
                0,
                dp(12),
                0,
                0
        );

        TextView enable =
                button(
                        source.isEnabled()
                                ? "停用"
                                : "启用"
                );

        enable.setOnClickListener(
                v -> {

                    sourceManager.setEnabled(
                            source.getId(),
                            !source.isEnabled()
                    );

                    loadSources();
                }
        );

        actions.addView(
                enable,
                actionParams()
        );

        TextView defaultButton =
                button(
                        source.isDefaultSource()
                                ? "默认"
                                : "设为默认"
                );

        defaultButton.setOnClickListener(
                v -> {

                    sourceManager.setDefault(
                            source.getId()
                    );

                    loadSources();

                    Toast.makeText(
                            this,
                            "已设置默认源",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        actions.addView(
                defaultButton,
                actionParams()
        );

        TextView test =
                button(
                        "测试"
                );

        test.setOnClickListener(
                v -> testSource(
                        source
                )
        );

        actions.addView(
                test,
                actionParams()
        );

        TextView edit =
                button(
                        "编辑"
                );

        edit.setOnClickListener(
                v -> showEditDialog(
                        source
                )
        );

        actions.addView(
                edit,
                actionParams()
        );

        TextView delete =
                button(
                        "删除"
                );

        delete.setOnClickListener(
                v -> confirmDelete(
                        source
                )
        );

        actions.addView(
                delete,
                actionParams()
        );

        card.addView(
                actions
        );

        // -----------------------------------------------------
        // 上移 / 下移
        // -----------------------------------------------------

        LinearLayout order =
                new LinearLayout(
                        this
                );

        order.setGravity(
                Gravity.END
        );

        TextView up =
                button(
                        "↑"
                );

        up.setOnClickListener(
                v -> {

                    sourceManager.moveUp(
                            source.getId()
                    );

                    loadSources();
                }
        );

        order.addView(
                up,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(38)
                )
        );

        TextView down =
                button(
                        "↓"
                );

        down.setOnClickListener(
                v -> {

                    sourceManager.moveDown(
                            source.getId()
                    );

                    loadSources();
                }
        );

        order.addView(
                down,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(38)
                )
        );

        card.addView(
                order
        );

        return card;
    }

    // =========================================================
    // 添加
    // =========================================================

    private void showAddDialog() {

        LinearLayout layout =
                createEditLayout();

        EditText name =
                createInput(
                        "名称，例如 xx"
                );

        EditText api =
                createInput(
                        "API，例如 https://example.com/api.php/provide/vod/"
                );

        api.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_URI
        );

        layout.addView(
                name
        );

        layout.addView(
                api
        );

        new AlertDialog.Builder(this)
                .setTitle("添加视频源")
                .setView(layout)
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "保存",
                        (dialog, which) -> {

                            String sourceName =
                                    name.getText()
                                            .toString()
                                            .trim();

                            String sourceApi =
                                    api.getText()
                                            .toString()
                                            .trim();

                            if (sourceName.isEmpty() ||
                                    sourceApi.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "名称和 API 不能为空",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            VideoSource source =
                                    new VideoSource();

                            source.setName(
                                    sourceName
                            );

                            source.setApiUrl(
                                    sourceApi
                            );

                            source.setEnabled(
                                    true
                            );

                            sourceManager.add(
                                    source
                            );

                            loadSources();
                        }
                )
                .show();
    }

    // =========================================================
    // 编辑
    // =========================================================

    private void showEditDialog(
            VideoSource source) {

        LinearLayout layout =
                createEditLayout();

        EditText name =
                createInput(
                        "名称"
                );

        name.setText(
                source.getName()
        );

        EditText api =
                createInput(
                        "API"
                );

        api.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_URI
        );

        api.setText(
                source.getApiUrl()
        );

        layout.addView(
                name
        );

        layout.addView(
                api
        );

        new AlertDialog.Builder(this)
                .setTitle("编辑视频源")
                .setView(layout)
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "保存",
                        (dialog, which) -> {

                            source.setName(
                                    name.getText()
                                            .toString()
                                            .trim()
                            );

                            source.setApiUrl(
                                    api.getText()
                                            .toString()
                                            .trim()
                            );

                            sourceManager.update(
                                    source
                            );

                            loadSources();
                        }
                )
                .show();
    }

    // =========================================================
    // 测试
    // =========================================================

    private void testSource(
            VideoSource source) {

        Toast.makeText(
                this,
                "正在测试：" +
                        source.getDisplayName(),
                Toast.LENGTH_SHORT
        ).show();

        sourceManager.test(
                source,
                new SourceManager.TestCallback() {

                    @Override
                    public void onResult(
                            boolean success,
                            String message) {

                        runOnUiThread(
                                () -> {

                                    Toast.makeText(
                                            SourceActivity.this,
                                            message == null
                                                    ? (
                                                    success
                                                            ? "连接成功"
                                                            : "连接失败"
                                            )
                                                    : message,
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadSources();
                                }
                        );
                    }
                }
        );
    }

    // =========================================================
    // 删除
    // =========================================================

    private void confirmDelete(
            VideoSource source) {

        new AlertDialog.Builder(this)
                .setTitle("删除视频源")
                .setMessage(
                        "确定删除「" +
                                source.getDisplayName() +
                                "」？"
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "删除",
                        (dialog, which) -> {

                            sourceManager.delete(
                                    source.getId()
                            );

                            loadSources();
                        }
                )
                .show();
    }

    // =========================================================
    // UI 工具
    // =========================================================

    private LinearLayout createEditLayout() {

        LinearLayout layout =
                new LinearLayout(
                        this
                );

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(8),
                dp(8),
                dp(8),
                0
        );

        return layout;
    }

    private EditText createInput(
            String hint) {

        EditText input =
                new EditText(
                        this
                );

        input.setHint(
                hint
        );

        input.setTextColor(
                0xFFFFFFFF
        );

        input.setHintTextColor(
                0xFF697487
        );

        input.setSingleLine(
                true
        );

        input.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
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
                dp(8)
        );

        input.setLayoutParams(
                params
        );

        return input;
    }

    private TextView text(
            String value,
            float size,
            int color) {

        TextView view =
                new TextView(
                        this
                );

        view.setText(
                value == null
                        ? ""
                        : value
        );

        view.setTextSize(
                size
        );

        view.setTextColor(
                color
        );

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

    private LinearLayout.LayoutParams actionParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(38),
                        1
                );

        params.setMargins(
                dp(2),
                0,
                dp(2),
                0
        );

        return params;
    }

    private int dp(
            int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private void showLoading(
            boolean show) {

        if (progressBar == null) {
            return;
        }

        progressBar.setVisibility(
                show
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (sourceManager != null) {
            sourceManager.shutdown();
        }
    }
}
