package com.threew.tv;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.search.SearchActivity;
import com.threew.tv.ui.DownloadActivity;
import com.threew.tv.ui.FavoriteActivity;
import com.threew.tv.ui.HistoryActivity;
import com.threew.tv.ui.LocalVideoActivity;
import com.threew.tv.ui.SettingsActivity;
import com.threew.tv.ui.SourceActivity;
import com.threew.tv.ui.SubscriptionActivity;

/**
 * 3W影视 - 首页
 *
 * 主入口：
 * 首页 / 搜索 / 本地 / 历史 / 收藏 / 下载 / 设置
 *
 * 采用程序化 UI，方便手机、平板和 Android TV
 * 后续统一调整视觉风格。
 */
public class MainActivity extends AppCompatActivity {

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int CARD_2 = Color.rgb(27, 30, 40);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUi();
    }

    private void buildUi() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(
                LinearLayout.VERTICAL
        );
        root.setBackgroundColor(BG);

        setContentView(root);

        root.addView(createTopBar());

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        content = new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(18),
                dp(16),
                dp(35)
        );

        scrollView.addView(content);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        buildHomeContent();
    }

    private View createTopBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        bar.setPadding(
                dp(16),
                dp(10),
                dp(12),
                dp(10)
        );

        bar.setBackgroundColor(
                Color.rgb(13, 16, 23)
        );

        LinearLayout titleBox =
                new LinearLayout(this);

        titleBox.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText("3W影视");
        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        titleBox.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "多源 · 高清 · 本地 · 下载"
        );

        subtitle.setTextColor(SUB_TEXT);
        subtitle.setTextSize(11);

        titleBox.addView(
                subtitle
        );

        bar.addView(
                titleBox,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView settings =
                new TextView(this);

        settings.setText("⚙");
        settings.setTextColor(TEXT);
        settings.setTextSize(23);
        settings.setGravity(
                Gravity.CENTER
        );

        settings.setOnClickListener(
                v -> open(
                        SettingsActivity.class
                )
        );

        bar.addView(
                settings,
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        return bar;
    }

    private void buildHomeContent() {

        addSearchBox();

        addSectionTitle(
                "快捷入口"
        );

        LinearLayout quickRow =
                new LinearLayout(this);

        quickRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        addQuickButton(
                quickRow,
                "🔎\n搜索",
                SearchActivity.class
        );

        addQuickButton(
                quickRow,
                "▶\n本地",
                LocalVideoActivity.class
        );

        addQuickButton(
                quickRow,
                "◷\n历史",
                HistoryActivity.class
        );

        addQuickButton(
                quickRow,
                "★\n收藏",
                FavoriteActivity.class
        );

        content.addView(
                quickRow
        );

        addSectionTitle(
                "我的影视"
        );

        addMenuCard(
                "下载管理",
                "管理正在下载、已暂停和已完成的视频",
                "↓",
                DownloadActivity.class
        );

        addMenuCard(
                "视频源",
                "管理 API 来源、启用状态、优先级和默认来源",
                "◈",
                SourceActivity.class
        );

        addMenuCard(
                "订阅管理",
                "导入 TXT / JSON / M3U 等订阅并提取视频源",
                "↻",
                SubscriptionActivity.class
        );

        addMenuCard(
                "本地视频",
                "添加多个本地文件夹，直接读取原始视频",
                "▣",
                LocalVideoActivity.class
        );

        addMenuCard(
                "观看历史",
                "继续观看最近播放的影片和剧集",
                "◷",
                HistoryActivity.class
        );

        addMenuCard(
                "我的收藏",
                "快速找到收藏的影片",
                "★",
                FavoriteActivity.class
        );

        addSectionTitle(
                "系统"
        );

        addMenuCard(
                "播放器设置",
                "倍速、画面比例、时钟、缓存和下载设置",
                "⚙",
                SettingsActivity.class
        );

        addInfoCard();
    }

    private void addSearchBox() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.HORIZONTAL
        );

        box.setGravity(
                Gravity.CENTER_VERTICAL
        );

        box.setPadding(
                dp(14),
                dp(4),
                dp(4),
                dp(4)
        );

        box.setBackgroundColor(CARD);

        EditText search =
                new EditText(this);

        search.setHint(
                "搜索影片、电视剧、动漫……"
        );

        search.setHintTextColor(
                Color.rgb(110, 118, 132)
        );

        search.setTextColor(TEXT);
        search.setTextSize(14);
        search.setSingleLine(true);

        search.setPadding(
                dp(4),
                0,
                dp(4),
                0
        );

        box.addView(
                search,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1f
                )
        );

        TextView button =
                new TextView(this);

        button.setText("搜索");
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setGravity(
                Gravity.CENTER
        );
        button.setBackgroundColor(
                ACCENT
        );

        button.setOnClickListener(v -> {

            String keyword =
                    search.getText()
                            .toString()
                            .trim();

            if (TextUtils.isEmpty(keyword)) {

                Toast.makeText(
                        this,
                        "请输入搜索内容",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            this,
                            SearchActivity.class
                    );

            intent.putExtra(
                    "keyword",
                    keyword
            );

            startActivity(intent);
        });

        box.addView(
                button,
                new LinearLayout.LayoutParams(
                        dp(72),
                        dp(46)
                )
        );

        LinearLayout.LayoutParams boxLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(60)
                );

        boxLp.setMargins(
                0,
                0,
                0,
                dp(12)
        );

        content.addView(
                box,
                boxLp
        );
    }

    private void addSectionTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextColor(TEXT);
        title.setTextSize(17);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        title.setPadding(
                dp(3),
                dp(18),
                dp(3),
                dp(10)
        );

        content.addView(title);
    }

    private void addQuickButton(
            LinearLayout row,
            String text,
            Class<?> target
    ) {

        TextView button =
                new TextView(this);

        button.setText(text);
        button.setTextColor(TEXT);
        button.setTextSize(13);
        button.setGravity(
                Gravity.CENTER
        );
        button.setLineSpacing(
                0,
                1.2f
        );
        button.setBackgroundColor(
                CARD
        );

        button.setFocusable(true);
        button.setClickable(true);

        button.setOnClickListener(
                v -> open(target)
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(76),
                        1f
                );

        lp.setMargins(
                0,
                0,
                dp(7),
                0
        );

        row.addView(
                button,
                lp
        );
    }

    private void addMenuCard(
            String title,
            String description,
            String icon,
            Class<?> target
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(15),
                dp(12),
                dp(10),
                dp(12)
        );

        card.setBackgroundColor(
                CARD
        );

        LinearLayout.LayoutParams cardLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                );

        cardLp.setMargins(
                0,
                0,
                0,
                dp(5)
        );

        card.setLayoutParams(cardLp);

        TextView iconView =
                new TextView(this);

        iconView.setText(icon);
        iconView.setTextColor(ACCENT);
        iconView.setTextSize(23);
        iconView.setGravity(
                Gravity.CENTER
        );

        card.addView(
                iconView,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(50)
                )
        );

        LinearLayout texts =
                new LinearLayout(this);

        texts.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(15);
        titleView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        texts.addView(titleView);

        TextView descView =
                new TextView(this);

        descView.setText(description);
        descView.setTextColor(SUB_TEXT);
        descView.setTextSize(11);
        descView.setPadding(
                0,
                dp(4),
                0,
                0
        );
        descView.setSingleLine(true);
        descView.setEllipsize(
                TextUtils.TruncateAt.END
        );

        texts.addView(descView);

        card.addView(
                texts,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView arrow =
                new TextView(this);

        arrow.setText("›");
        arrow.setTextColor(SUB_TEXT);
        arrow.setTextSize(26);
        arrow.setGravity(
                Gravity.CENTER
        );

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(35),
                        dp(50)
                )
        );

        card.setFocusable(true);
        card.setClickable(true);

        card.setOnClickListener(
                v -> open(target)
        );

        content.addView(card);
    }

    private void addInfoCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackgroundColor(
                CARD
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                dp(18),
                0,
                0
        );

        card.setLayoutParams(lp);

        TextView title =
                new TextView(this);

        title.setText("3W影视");
        title.setTextColor(ACCENT);
        title.setTextSize(15);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        card.addView(title);

        TextView info =
                new TextView(this);

        info.setText(
                "多源聚合播放器\n" +
                        "支持在线视频、本地视频、订阅、下载与播放历史\n\n" +
                        "版本 1.0.0"
        );

        info.setTextColor(SUB_TEXT);
        info.setTextSize(12);
        info.setLineSpacing(
                0,
                1.25f
        );

        info.setPadding(
                0,
                dp(8),
                0,
                0
        );

        card.addView(info);

        content.addView(card);
    }

    private void open(
            Class<?> target
    ) {

        startActivity(
                new Intent(
                        this,
                        target
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
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
