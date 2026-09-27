package com.threew.tv.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.local.LocalVideoManager;
import com.threew.tv.model.LocalFolder;
import com.threew.tv.model.Video;
import com.threew.tv.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 3W影视 - 本地视频
 *
 * 功能：
 * 1. 管理多个本地视频文件夹
 * 2. 使用系统文件夹选择器添加目录
 * 3. 不复制、不移动原视频
 * 4. 自动读取当前文件夹内容
 * 5. 新增/删除的视频可以重新扫描后同步
 * 6. 点击本地视频进入统一播放器
 */
public class LocalVideoActivity extends AppCompatActivity {

    private static final int REQUEST_FOLDER = 7101;

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int CARD_2 = Color.rgb(27, 30, 40);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);
    private static final int DANGER = Color.rgb(255, 90, 90);

    private LinearLayout listContainer;
    private LocalVideoManager localVideoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localVideoManager = new LocalVideoManager(this);

        buildUi();
        loadFolders();
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
        title.setText("本地视频");
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

        TextView add = new TextView(this);
        add.setText("+ 文件夹");
        add.setTextColor(ACCENT);
        add.setTextSize(14);
        add.setGravity(Gravity.CENTER);

        add.setOnClickListener(
                v -> chooseFolder()
        );

        bar.addView(
                add,
                new LinearLayout.LayoutParams(
                        dp(90),
                        dp(50)
                )
        );

        return bar;
    }

    private void loadFolders() {

        if (listContainer == null) {
            return;
        }

        listContainer.removeAllViews();

        List<LocalFolder> folders;

        try {
            folders =
                    localVideoManager.getLocalFolders();
        } catch (Exception e) {
            folders = null;
        }

        if (folders == null || folders.isEmpty()) {

            TextView empty = new TextView(this);

            empty.setText(
                    "还没有添加本地视频文件夹\n\n" +
                    "点击右上角「+ 文件夹」选择一个目录"
            );

            empty.setTextColor(SUB_TEXT);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(
                    dp(20),
                    dp(80),
                    dp(20),
                    dp(80)
            );

            listContainer.addView(empty);
            return;
        }

        for (LocalFolder folder : folders) {

            if (folder == null) {
                continue;
            }

            listContainer.addView(
                    createFolderItem(folder)
            );
        }
    }

    private View createFolderItem(
            LocalFolder folder
    ) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(14),
                dp(12),
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

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                new TextView(this);

        String displayName =
                localVideoManager.getDisplayName(
                        folder
                );

        if (TextUtils.isEmpty(displayName)) {
            displayName = "本地视频文件夹";
        }

        name.setText(displayName);
        name.setTextColor(TEXT);
        name.setTextSize(16);
        name.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        name.setSingleLine(true);
        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        titleRow.addView(
                name,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView enabled =
                new TextView(this);

        enabled.setText(
                folder.isEnabled()
                        ? "已启用"
                        : "已停用"
        );

        enabled.setTextColor(
                folder.isEnabled()
                        ? ACCENT
                        : SUB_TEXT
        );

        enabled.setTextSize(12);
        enabled.setGravity(Gravity.CENTER);

        titleRow.addView(
                enabled,
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(32)
                )
        );

        card.addView(titleRow);

        TextView path =
                new TextView(this);

        String uri = folder.getTreeUri();

        if (TextUtils.isEmpty(uri)) {
            uri = "未获取目录权限";
        }

        path.setText(uri);
        path.setTextColor(SUB_TEXT);
        path.setTextSize(11);
        path.setSingleLine(true);
        path.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        path.setPadding(
                0,
                dp(8),
                0,
                dp(5)
        );

        card.addView(path);

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actions.setGravity(
                Gravity.CENTER_VERTICAL
        );

        addActionButton(
                actions,
                "扫描",
                ACCENT,
                v -> scanFolder(folder)
        );

        addActionButton(
                actions,
                folder.isEnabled()
                        ? "停用"
                        : "启用",
                SUB_TEXT,
                v -> toggleFolder(folder)
        );

        addActionButton(
                actions,
                "删除",
                DANGER,
                v -> deleteFolder(folder)
        );

        card.addView(actions);

        return card;
    }

    private void addActionButton(
            LinearLayout container,
            String text,
            int color,
            View.OnClickListener listener
    ) {

        TextView button =
                new TextView(this);

        button.setText(text);
        button.setTextColor(color);
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
                dp(8),
                dp(8),
                0
        );

        container.addView(
                button,
                lp
        );
    }

    private void chooseFolder() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT_TREE
                );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                REQUEST_FOLDER
        );
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

        if (requestCode != REQUEST_FOLDER ||
                resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {

            localVideoManager.takePersistablePermission(
                    uri
            );

            localVideoManager.addFolder(
                    uri
            );

            Toast.makeText(
                    this,
                    "文件夹已添加",
                    Toast.LENGTH_SHORT
            ).show();

            loadFolders();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "添加文件夹失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void scanFolder(
            LocalFolder folder
    ) {

        if (folder == null) {
            return;
        }

        try {

            List<Video> videos =
                    localVideoManager.scanFolder(
                            folder.getId()
                    );

            int count =
                    videos == null
                            ? 0
                            : videos.size();

            Toast.makeText(
                    this,
                    "扫描完成，共发现 " +
                            count +
                            " 个视频",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "扫描失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void toggleFolder(
            LocalFolder folder
    ) {

        if (folder == null) {
            return;
        }

        try {

            localVideoManager.setEnabled(
                    folder.getId(),
                    !folder.isEnabled()
            );

            loadFolders();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "操作失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void deleteFolder(
            LocalFolder folder
    ) {

        if (folder == null) {
            return;
        }

        try {

            localVideoManager.deleteFolder(
                    folder.getId()
            );

            Toast.makeText(
                    this,
                    "文件夹已移除",
                    Toast.LENGTH_SHORT
            ).show();

            loadFolders();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "删除失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * 打开本地视频播放。
     *
     * 这里不复制视频文件，
     * 直接使用原始 URI。
     */
    private void playLocalVideo(
            Video video
    ) {

        if (video == null ||
                video.getEpisodes() == null ||
                video.getEpisodes().isEmpty()) {

            Toast.makeText(
                    this,
                    "没有可播放的视频",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (video.getEpisodes().get(0) == null) {
            return;
        }

        String uri =
                video.getEpisodes()
                        .get(0)
                        .getPlayUrl();

        if (TextUtils.isEmpty(uri)) {
            return;
        }

        Intent intent =
                new Intent(
                        this,
                        PlayerActivity.class
                );

        intent.putExtra(
                "video_id",
                video.getId()
        );

        intent.putExtra(
                "video_name",
                video.getName()
        );

        intent.putExtra(
                "episode_id",
                video.getEpisodes()
                        .get(0)
                        .getId()
        );

        intent.putExtra(
                "episode_name",
                video.getEpisodes()
                        .get(0)
                        .getName()
        );

        intent.putExtra(
                "play_url",
                uri
        );

        intent.putExtra(
                "local_video",
                true
        );

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listContainer != null) {
            loadFolders();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            localVideoManager.shutdown();
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
