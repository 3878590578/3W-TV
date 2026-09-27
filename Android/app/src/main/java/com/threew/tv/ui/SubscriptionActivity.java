package com.threew.tv.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.threew.tv.model.Subscription;
import com.threew.tv.source.SubscriptionManager;

import java.util.List;

/**
 * 3W影视订阅管理
 *
 * 订阅和视频源分开管理。
 *
 * 支持：
 * - URL 订阅
 * - 本地 TXT / JSON
 * - 刷新订阅
 * - 启用 / 停用
 * - 删除订阅
 * - 导入本地文件
 * - 查看订阅内容
 * - 从已导入内容提取视频源
 */
public class SubscriptionActivity extends AppCompatActivity {

    private static final int REQUEST_IMPORT_FILE = 3101;

    private LinearLayout container;

    private ProgressBar progressBar;

    private SubscriptionManager subscriptionManager;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        subscriptionManager =
                new SubscriptionManager(this);

        buildPage();

        loadSubscriptions();
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
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        root.setBackgroundColor(
                0xFF070B12
        );

        // -----------------------------------------------------
        // 标题
        // -----------------------------------------------------

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "订阅管理",
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
                button("+ 添加");

        add.setOnClickListener(
                v -> showAddMenu()
        );

        header.addView(
                add,
                new LinearLayout.LayoutParams(
                        dp(88),
                        dp(44)
                )
        );

        root.addView(header);

        // -----------------------------------------------------
        // 说明
        // -----------------------------------------------------

        TextView tip =
                text(
                        "订阅内容会保存到本地。刷新订阅后，再从本地内容提取视频源。",
                        13,
                        0xFF8993A5
                );

        tip.setPadding(
                dp(4),
                0,
                dp(4),
                dp(12)
        );

        root.addView(tip);

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
        // 列表
        // -----------------------------------------------------

        ScrollView scroll =
                new ScrollView(this);

        container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(container);

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
    // 加载
    // =========================================================

    private void loadSubscriptions() {

        showLoading(true);

        container.removeAllViews();

        List<Subscription> list =
                subscriptionManager.getAll();

        showLoading(false);

        if (list == null ||
                list.isEmpty()) {

            TextView empty =
                    text(
                            "暂无订阅\n点击右上角「+ 添加」",
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

            container.addView(empty);

            return;
        }

        for (Subscription subscription :
                list) {

            if (subscription == null) {
                continue;
            }

            container.addView(
                    createCard(subscription)
            );
        }
    }

    // =========================================================
    // 订阅卡片
    // =========================================================

    private View createCard(
            Subscription subscription) {

        LinearLayout card =
                new LinearLayout(this);

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

        card.setLayoutParams(cardParams);

        // -----------------------------------------------------
        // 第一行
        // -----------------------------------------------------

        LinearLayout top =
                new LinearLayout(this);

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                text(
                        subscription.getDisplayName(),
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

        TextView state =
                text(
                        subscription.isEnabled()
                                ? "已启用"
                                : "已停用",
                        12,
                        subscription.isEnabled()
                                ? 0xFF58D68D
                                : 0xFF8993A5
                );

        state.setGravity(
                Gravity.CENTER
        );

        top.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(30)
                )
        );

        card.addView(top);

        // -----------------------------------------------------
        // 地址
        // -----------------------------------------------------

        String location =
                subscription.isLocalFile()
                        ? subscription.getLocalUri()
                        : subscription.getUrl();

        TextView url =
                text(
                        location,
                        12,
                        0xFF8993A5
                );

        url.setPadding(
                0,
                dp(4),
                0,
                dp(6)
        );

        card.addView(url);

        // -----------------------------------------------------
        // 类型
        // -----------------------------------------------------

        String type =
                subscription.getType();

        if (type == null ||
                type.trim().isEmpty()) {

            type = "raw";
        }

        TextView meta =
                text(
                        "类型：" + type +
                                "    内容：" +
                                (subscription.hasContent()
                                        ? "已导入"
                                        : "未导入"),
                        12,
                        0xFF6F7B8F
                );

        card.addView(meta);

        // -----------------------------------------------------
        // 操作
        // -----------------------------------------------------

        LinearLayout actions =
                new LinearLayout(this);

        actions.setPadding(
                0,
                dp(12),
                0,
                0
        );

        TextView enable =
                button(
                        subscription.isEnabled()
                                ? "停用"
                                : "启用"
                );

        enable.setOnClickListener(
                v -> {

                    subscriptionManager.setEnabled(
                            subscription.getId(),
                            !subscription.isEnabled()
                    );

                    loadSubscriptions();
                }
        );

        actions.addView(
                enable,
                actionParams()
        );

        TextView refresh =
                button("刷新");

        refresh.setOnClickListener(
                v -> refreshSubscription(
                        subscription
                )
        );

        actions.addView(
                refresh,
                actionParams()
        );

        TextView extract =
                button("提取源");

        extract.setOnClickListener(
                v -> extractSources(
                        subscription
                )
        );

        actions.addView(
                extract,
                actionParams()
        );

        TextView view =
                button("查看");

        view.setOnClickListener(
                v -> showContent(
                        subscription
                )
        );

        actions.addView(
                view,
                actionParams()
        );

        TextView delete =
                button("删除");

        delete.setOnClickListener(
                v -> confirmDelete(
                        subscription
                )
        );

        actions.addView(
                delete,
                actionParams()
        );

        card.addView(actions);

        return card;
    }

    // =========================================================
    // 添加菜单
    // =========================================================

    private void showAddMenu() {

        String[] items = {
                "添加网络订阅",
                "导入本地 TXT / JSON"
        };

        new AlertDialog.Builder(this)
                .setTitle("添加订阅")
                .setItems(
                        items,
                        (dialog, which) -> {

                            if (which == 0) {
                                showAddUrlDialog();
                            } else {
                                openFilePicker();
                            }
                        }
                )
                .show();
    }

    // =========================================================
    // 添加 URL
    // =========================================================

    private void showAddUrlDialog() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(8),
                dp(8),
                dp(8),
                0
        );

        EditText name =
                createInput(
                        "订阅名称"
                );

        EditText url =
                createInput(
                        "订阅 URL"
                );

        url.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_URI
        );

        layout.addView(name);
        layout.addView(url);

        new AlertDialog.Builder(this)
                .setTitle("添加网络订阅")
                .setView(layout)
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "保存",
                        (dialog, which) -> {

                            String n =
                                    name.getText()
                                            .toString()
                                            .trim();

                            String u =
                                    url.getText()
                                            .toString()
                                            .trim();

                            if (u.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "订阅 URL 不能为空",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            if (n.isEmpty()) {
                                n = "网络订阅";
                            }

                            Subscription subscription =
                                    new Subscription();

                            subscription.setName(n);
                            subscription.setUrl(u);
                            subscription.setType(
                                    detectType(u)
                            );
                            subscription.setEnabled(
                                    true
                            );

                            subscriptionManager.add(
                                    subscription
                            );

                            loadSubscriptions();
                        }
                )
                .show();
    }

    // =========================================================
    // 本地文件
    // =========================================================

    private void openFilePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "*/*"
        );

        startActivityForResult(
                intent,
                REQUEST_IMPORT_FILE
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode !=
                REQUEST_IMPORT_FILE) {
            return;
        }

        if (resultCode !=
                RESULT_OK ||
                data == null) {
            return;
        }

        Uri uri =
                data.getData();

        if (uri == null) {
            return;
        }

        try {

            getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

        } catch (Exception ignored) {
        }

        importLocalFile(uri);
    }

    private void importLocalFile(
            Uri uri) {

        showLoading(true);

        new Thread(() -> {

            try {

                Subscription subscription =
                        subscriptionManager
                                .importLocalFile(
                                        uri
                                );

                runOnUiThread(() -> {

                    showLoading(false);

                    if (subscription != null) {

                        Toast.makeText(
                                this,
                                "导入成功",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadSubscriptions();

                    } else {

                        Toast.makeText(
                                this,
                                "导入失败",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "导入失败：" +
                                    e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

        }).start();
    }

    // =========================================================
    // 刷新
    // =========================================================

    private void refreshSubscription(
            Subscription subscription) {

        showLoading(true);

        new Thread(() -> {

            try {

                boolean success =
                        subscriptionManager
                                .refresh(
                                        subscription
                                );

                runOnUiThread(() -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            success
                                    ? "刷新成功"
                                    : "刷新失败",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadSubscriptions();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "刷新失败：" +
                                    e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

        }).start();
    }

    // =========================================================
    // 提取视频源
    // =========================================================

    private void extractSources(
            Subscription subscription) {

        showLoading(true);

        new Thread(() -> {

            try {

                int count =
                        subscriptionManager
                                .extractSources(
                                        subscription
                                );

                runOnUiThread(() -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "已提取 " +
                                    count +
                                    " 个视频源",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "提取失败：" +
                                    e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

        }).start();
    }

    // =========================================================
    // 查看订阅内容
    // =========================================================

    private void showContent(
            Subscription subscription) {

        String content =
                subscription.getContent();

        if (content == null ||
                content.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "当前订阅还没有内容",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        EditText text =
                new EditText(this);

        text.setText(content);
        text.setTextColor(0xFFE6ECF5);
        text.setTextSize(12);
        text.setGravity(
                Gravity.TOP | Gravity.START
        );

        text.setSingleLine(false);
        text.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        scroll.addView(text);

        new AlertDialog.Builder(this)
                .setTitle(
                        subscription.getDisplayName()
                )
                .setView(scroll)
                .setPositiveButton(
                        "关闭",
                        null
                )
                .show();
    }

    // =========================================================
    // 删除
    // =========================================================

    private void confirmDelete(
            Subscription subscription) {

        new AlertDialog.Builder(this)
                .setTitle("删除订阅")
                .setMessage(
                        "确定删除「" +
                                subscription.getDisplayName() +
                                "」？\n\n" +
                                "只删除本地订阅记录，不会删除原始网络资源。"
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "删除",
                        (dialog, which) -> {

                            subscriptionManager.delete(
                                    subscription.getId()
                            );

                            loadSubscriptions();
                        }
                )
                .show();
    }

    // =========================================================
    // 类型判断
    // =========================================================

    private String detectType(
            String value) {

        if (value == null) {
            return "raw";
        }

        String lower =
                value.toLowerCase();

        if (lower.endsWith(".json")) {
            return "json";
        }

        if (lower.endsWith(".txt")) {
            return "txt";
        }

        if (lower.endsWith(".m3u") ||
                lower.endsWith(".m3u8")) {
            return "m3u";
        }

        if (lower.startsWith("cms://")) {
            return "cms";
        }

        return "raw";
    }

    // =========================================================
    // UI 工具
    // =========================================================

    private EditText createInput(
            String hint) {

        EditText input =
                new EditText(this);

        input.setHint(hint);

        input.setTextColor(
                0xFFFFFFFF
        );

        input.setHintTextColor(
                0xFF697487
        );

        input.setSingleLine(true);

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

        input.setLayoutParams(params);

        return input;
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

    private int dp(int value) {

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

        if (subscriptionManager != null) {
            subscriptionManager.shutdown();
        }
    }
}
