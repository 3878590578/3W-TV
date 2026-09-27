package com.threew.tv.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.settings.AppSettings;
import com.threew.tv.settings.BackgroundSettings;
import com.threew.tv.settings.CacheSettings;
import com.threew.tv.settings.OverlaySettings;
import com.threew.tv.settings.SpeedSettings;

import java.util.Locale;

/**
 * 3W影视 - 设置
 *
 * 功能：
 * 1. 播放速度
 * 2. 长按倍速
 * 3. 自动下一集
 * 4. 播放画面比例
 * 5. 屏幕方向
 * 6. 播放器时钟
 * 7. 播放信息
 * 8. 缓存大小
 * 9. 下载并发数
 * 10. Wi-Fi 下载
 * 11. 自定义应用背景
 * 12. 恢复默认设置
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_BACKGROUND = 7201;

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int CARD_2 = Color.rgb(27, 30, 40);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);
    private static final int DANGER = Color.rgb(255, 90, 90);

    private AppSettings appSettings;
    private SpeedSettings speedSettings;
    private CacheSettings cacheSettings;
    private OverlaySettings overlaySettings;
    private BackgroundSettings backgroundSettings;

    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSettings = new AppSettings(this);
        speedSettings = new SpeedSettings(this);
        cacheSettings = new CacheSettings(this);
        overlaySettings = new OverlaySettings(this);
        backgroundSettings = new BackgroundSettings(this);

        buildUi();
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
                dp(12),
                dp(14),
                dp(35)
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

        buildSettings();
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
        title.setText("设置");
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

        return bar;
    }

    private void buildSettings() {

        addSection("播放");

        addSetting(
                "默认播放速度",
                formatSpeed(
                        speedSettings.getGlobalSpeed()
                ),
                v -> chooseNormalSpeed()
        );

        addSetting(
                "长按临时倍速",
                formatSpeed(
                        speedSettings.getLongPressSpeed()
                ),
                v -> chooseLongPressSpeed()
        );

        addSetting(
                "播放画面",
                getDisplayModeText(
                        appSettings.getDisplayMode()
                ),
                v -> chooseDisplayMode()
        );

        addSetting(
                "屏幕方向",
                getOrientationText(
                        appSettings.getOrientationMode()
                ),
                v -> chooseOrientation()
        );

        addSetting(
                "自动下一集",
                appSettings.isAutoNext()
                        ? "开启"
                        : "关闭",
                v -> toggleAutoNext()
        );

        addSection("播放器显示");

        addSetting(
                "播放器时钟",
                overlaySettings.isClockEnabled()
                        ? "开启"
                        : "关闭",
                v -> toggleClock()
        );

        addSetting(
                "时钟大小",
                overlaySettings.getClockSize()
                        + " sp",
                v -> chooseClockSize()
        );

        addSetting(
                "时钟位置",
                getPositionText(
                        overlaySettings.getClockPosition()
                ),
                v -> chooseClockPosition()
        );

        addSetting(
                "视频信息",
                overlaySettings.isInfoEnabled()
                        ? "开启"
                        : "关闭",
                v -> toggleVideoInfo()
        );

        addSetting(
                "视频信息大小",
                overlaySettings.getInfoSize()
                        + " sp",
                v -> chooseInfoSize()
        );

        addSetting(
                "视频信息位置",
                getPositionText(
                        overlaySettings.getInfoPosition()
                ),
                v -> chooseInfoPosition()
        );

        addSection("缓存");

        addSetting(
                "缓存模式",
                getCacheModeText(
                        cacheSettings.getMode()
                ),
                v -> chooseCacheMode()
        );

        addSetting(
                "缓存目标时长",
                cacheSettings.getTargetMinutes()
                        + " 分钟",
                v -> chooseCacheMinutes()
        );

        addSetting(
                "缓存目标大小",
                cacheSettings.getTargetMb()
                        + " MB",
                v -> chooseCacheSize()
        );

        addSection("下载");

        addSetting(
                "下载并发",
                appSettings.getDownloadConcurrency()
                        + " 个任务",
                v -> chooseDownloadConcurrency()
        );

        addSetting(
                "仅 Wi-Fi 下载",
                appSettings.isDownloadWifiOnly()
                        ? "开启"
                        : "关闭",
                v -> toggleWifiOnly()
        );

        addSection("界面");

        addSetting(
                "应用背景",
                backgroundSettings.isEnabled()
                        ? "自定义背景"
                        : "默认科技黑",
                v -> chooseBackground()
        );

        addSetting(
                "清除自定义背景",
                "恢复默认",
                v -> clearBackground()
        );

        addSection("数据");

        addSetting(
                "恢复全部默认设置",
                "恢复",
                v -> resetAll()
        );
    }

    private void addSection(String title) {

        TextView section = new TextView(this);

        section.setText(title);
        section.setTextColor(ACCENT);
        section.setTextSize(14);
        section.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        section.setPadding(
                dp(4),
                dp(18),
                dp(4),
                dp(8)
        );

        listContainer.addView(section);
    }

    private void addSetting(
            String title,
            String summary,
            View.OnClickListener listener
    ) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );
        row.setGravity(
                Gravity.CENTER_VERTICAL
        );
        row.setPadding(
                dp(16),
                dp(12),
                dp(12),
                dp(12)
        );
        row.setBackgroundColor(CARD);

        LinearLayout.LayoutParams rowLp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(62)
                );

        rowLp.setMargins(
                0,
                0,
                0,
                dp(2)
        );

        row.setLayoutParams(rowLp);

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

        texts.addView(titleView);

        TextView summaryView =
                new TextView(this);

        summaryView.setText(summary);
        summaryView.setTextColor(SUB_TEXT);
        summaryView.setTextSize(12);
        summaryView.setPadding(
                0,
                dp(4),
                0,
                0
        );

        texts.addView(summaryView);

        row.addView(
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
        arrow.setGravity(Gravity.CENTER);

        row.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(38),
                        dp(50)
                )
        );

        row.setOnClickListener(listener);
        row.setFocusable(true);
        row.setClickable(true);

        listContainer.addView(row);
    }

    private void rebuild() {

        listContainer.removeAllViews();
        buildSettings();
    }

    private void chooseNormalSpeed() {

        String[] values = {
                "1×",
                "1.5×",
                "2×",
                "2.5×",
                "3×",
                "5×",
                "8×"
        };

        final float[] speeds = {
                1f,
                1.5f,
                2f,
                2.5f,
                3f,
                5f,
                8f
        };

        showChoiceDialog(
                "默认播放速度",
                values,
                findIndex(
                        speeds,
                        speedSettings.getGlobalSpeed()
                ),
                which -> {

                    speedSettings.setGlobalSpeed(
                            speeds[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseLongPressSpeed() {

        String[] values = {
                "2×",
                "3×",
                "5×",
                "8×"
        };

        final float[] speeds = {
                2f,
                3f,
                5f,
                8f
        };

        showChoiceDialog(
                "长按临时倍速",
                values,
                findIndex(
                        speeds,
                        speedSettings.getLongPressSpeed()
                ),
                which -> {

                    speedSettings.setLongPressSpeed(
                            speeds[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseDisplayMode() {

        String[] values = {
                "显示全部画面",
                "填充",
                "裁剪",
                "原始比例",
                "16:9",
                "4:3"
        };

        String[] keys = {
                "fit",
                "fill",
                "crop",
                "original",
                "16:9",
                "4:3"
        };

        showChoiceDialog(
                "播放画面",
                values,
                findIndex(
                        keys,
                        appSettings.getDisplayMode()
                ),
                which -> {

                    appSettings.setDisplayMode(
                            keys[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseOrientation() {

        String[] values = {
                "自动",
                "竖屏",
                "横屏"
        };

        String[] keys = {
                "auto",
                "portrait",
                "landscape"
        };

        showChoiceDialog(
                "屏幕方向",
                values,
                findIndex(
                        keys,
                        appSettings.getOrientationMode()
                ),
                which -> {

                    appSettings.setOrientationMode(
                            keys[which]
                    );

                    rebuild();
                }
        );
    }

    private void toggleAutoNext() {

        appSettings.setAutoNext(
                !appSettings.isAutoNext()
        );

        rebuild();
    }

    private void toggleClock() {

        overlaySettings.setClockEnabled(
                !overlaySettings.isClockEnabled()
        );

        rebuild();
    }

    private void chooseClockSize() {

        String[] values = {
                "12 sp",
                "14 sp",
                "16 sp",
                "18 sp",
                "20 sp",
                "24 sp"
        };

        final int[] sizes = {
                12,
                14,
                16,
                18,
                20,
                24
        };

        showChoiceDialog(
                "时钟大小",
                values,
                findIndex(
                        sizes,
                        overlaySettings.getClockSize()
                ),
                which -> {

                    overlaySettings.setClockSize(
                            sizes[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseClockPosition() {

        String[] values = {
                "左上",
                "右上",
                "左下",
                "右下"
        };

        String[] keys = {
                "top_left",
                "top_right",
                "bottom_left",
                "bottom_right"
        };

        showChoiceDialog(
                "时钟位置",
                values,
                findIndex(
                        keys,
                        overlaySettings.getClockPosition()
                ),
                which -> {

                    overlaySettings.setClockPosition(
                            keys[which]
                    );

                    rebuild();
                }
        );
    }

    private void toggleVideoInfo() {

        overlaySettings.setInfoEnabled(
                !overlaySettings.isInfoEnabled()
        );

        rebuild();
    }

    private void chooseInfoSize() {

        String[] values = {
                "11 sp",
                "12 sp",
                "13 sp",
                "14 sp",
                "16 sp",
                "18 sp"
        };

        final int[] sizes = {
                11,
                12,
                13,
                14,
                16,
                18
        };

        showChoiceDialog(
                "视频信息大小",
                values,
                findIndex(
                        sizes,
                        overlaySettings.getInfoSize()
                ),
                which -> {

                    overlaySettings.setInfoSize(
                            sizes[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseInfoPosition() {

        String[] values = {
                "左上",
                "右上",
                "左下",
                "右下"
        };

        String[] keys = {
                "top_left",
                "top_right",
                "bottom_left",
                "bottom_right"
        };

        showChoiceDialog(
                "视频信息位置",
                values,
                findIndex(
                        keys,
                        overlaySettings.getInfoPosition()
                ),
                which -> {

                    overlaySettings.setInfoPosition(
                            keys[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseCacheMode() {

        String[] values = {
                "按时长",
                "按大小"
        };

        String[] keys = {
                "time",
                "size"
        };

        showChoiceDialog(
                "缓存模式",
                values,
                findIndex(
                        keys,
                        cacheSettings.getMode()
                ),
                which -> {

                    cacheSettings.setMode(
                            keys[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseCacheMinutes() {

        String[] values = {
                "10 分钟",
                "20 分钟",
                "30 分钟",
                "60 分钟",
                "90 分钟",
                "120 分钟"
        };

        final int[] minutes = {
                10,
                20,
                30,
                60,
                90,
                120
        };

        showChoiceDialog(
                "缓存目标时长",
                values,
                findIndex(
                        minutes,
                        cacheSettings.getTargetMinutes()
                ),
                which -> {

                    cacheSettings.setTargetMinutes(
                            minutes[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseCacheSize() {

        String[] values = {
                "100 MB",
                "256 MB",
                "512 MB",
                "1 GB",
                "1.5 GB",
                "2 GB"
        };

        final int[] sizes = {
                100,
                256,
                512,
                1024,
                1536,
                2048
        };

        showChoiceDialog(
                "缓存目标大小",
                values,
                findIndex(
                        sizes,
                        cacheSettings.getTargetMb()
                ),
                which -> {

                    cacheSettings.setTargetMb(
                            sizes[which]
                    );

                    rebuild();
                }
        );
    }

    private void chooseDownloadConcurrency() {

        String[] values = {
                "2 个",
                "4 个",
                "6 个",
                "8 个"
        };

        final int[] counts = {
                2,
                4,
                6,
                8
        };

        showChoiceDialog(
                "下载并发",
                values,
                findIndex(
                        counts,
                        appSettings.getDownloadConcurrency()
                ),
                which -> {

                    appSettings.setDownloadConcurrency(
                            counts[which]
                    );

                    rebuild();
                }
        );
    }

    private void toggleWifiOnly() {

        appSettings.setDownloadWifiOnly(
                !appSettings.isDownloadWifiOnly()
        );

        rebuild();
    }

    private void chooseBackground() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("image/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                REQUEST_BACKGROUND
        );
    }

    private void clearBackground() {

        backgroundSettings.clear();

        Toast.makeText(
                this,
                "已恢复默认背景",
                Toast.LENGTH_SHORT
        ).show();

        rebuild();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != REQUEST_BACKGROUND ||
                resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {

            getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

        } catch (Exception ignored) {
        }

        backgroundSettings.setBackgroundUri(
                uri.toString()
        );

        backgroundSettings.setEnabled(true);

        Toast.makeText(
                this,
                "应用背景已更新",
                Toast.LENGTH_SHORT
        ).show();

        rebuild();
    }

    private void resetAll() {

        new AlertDialog.Builder(this)
                .setTitle("恢复默认设置")
                .setMessage(
                        "将恢复播放器、缓存、下载和界面设置。\n\n" +
                                "收藏、历史、订阅和视频源不会删除。"
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .setPositiveButton(
                        "恢复",
                        (dialog, which) -> {

                            appSettings.reset();
                            speedSettings.reset();
                            cacheSettings.reset();
                            overlaySettings.reset();
                            backgroundSettings.clear();

                            Toast.makeText(
                                    this,
                                    "已恢复默认设置",
                                    Toast.LENGTH_SHORT
                            ).show();

                            rebuild();
                        }
                )
                .show();
    }

    private void showChoiceDialog(
            String title,
            String[] values,
            int checked,
            final ChoiceListener listener
    ) {

        if (values == null ||
                values.length == 0) {
            return;
        }

        if (checked < 0 ||
                checked >= values.length) {
            checked = 0;
        }

        final int initial = checked;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(
                        values,
                        initial,
                        (dialog, which) -> {

                            listener.onChoice(which);

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .show();
    }

    private int findIndex(
            String[] values,
            String target
    ) {

        if (values == null ||
                target == null) {
            return 0;
        }

        for (int i = 0; i < values.length; i++) {

            if (target.equals(values[i])) {
                return i;
            }
        }

        return 0;
    }

    private int findIndex(
            float[] values,
            float target
    ) {

        if (values == null) {
            return 0;
        }

        for (int i = 0; i < values.length; i++) {

            if (Math.abs(values[i] - target) < 0.01f) {
                return i;
            }
        }

        return 0;
    }

    private int findIndex(
            int[] values,
            int target
    ) {

        if (values == null) {
            return 0;
        }

        for (int i = 0; i < values.length; i++) {

            if (values[i] == target) {
                return i;
            }
        }

        return 0;
    }

    private String formatSpeed(float speed) {

        if (Math.abs(speed - Math.round(speed)) < 0.01f) {
            return String.format(
                    Locale.US,
                    "%.0f×",
                    speed
            );
        }

        return String.format(
                Locale.US,
                "%.1f×",
                speed
        );
    }

    private String getDisplayModeText(
            String mode
    ) {

        if ("fill".equals(mode)) {
            return "填充";
        }

        if ("crop".equals(mode)) {
            return "裁剪";
        }

        if ("original".equals(mode)) {
            return "原始比例";
        }

        if ("16:9".equals(mode)) {
            return "16:9";
        }

        if ("4:3".equals(mode)) {
            return "4:3";
        }

        return "显示全部画面";
    }

    private String getOrientationText(
            String mode
    ) {

        if ("portrait".equals(mode)) {
            return "竖屏";
        }

        if ("landscape".equals(mode)) {
            return "横屏";
        }

        return "自动";
    }

    private String getPositionText(
            String position
    ) {

        if ("top_left".equals(position)) {
            return "左上";
        }

        if ("top_right".equals(position)) {
            return "右上";
        }

        if ("bottom_left".equals(position)) {
            return "左下";
        }

        if ("bottom_right".equals(position)) {
            return "右下";
        }

        return "右上";
    }

    private String getCacheModeText(
            String mode
    ) {

        if ("size".equals(mode)) {
            return "按大小";
        }

        return "按时长";
    }

    private interface ChoiceListener {
        void onChoice(int which);
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
