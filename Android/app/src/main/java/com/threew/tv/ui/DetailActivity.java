package com.threew.tv.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threew.tv.R;
import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;
import com.threew.tv.model.VideoSource;
import com.threew.tv.player.PlayerActivity;
import com.threew.tv.source.SourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 3W影视 - 详情页
 *
 * 功能：
 * 1. 显示影片基本信息
 * 2. 显示来源
 * 3. 同名影片多来源切换
 * 4. 显示选中来源的剧集
 * 5. 点击剧集进入播放器
 * 6. 适配手机、平板、TV
 *
 * SearchActivity 会通过 Intent 传入：
 * video_id
 * video_name
 * source_id
 * source_name
 * poster
 * year
 * area
 * category
 * remarks
 */
public class DetailActivity extends AppCompatActivity {

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int CARD_2 = Color.rgb(27, 30, 40);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);

    private LinearLayout root;
    private LinearLayout sourceContainer;
    private LinearLayout episodeContainer;

    private TextView titleView;
    private TextView metaView;
    private TextView descriptionView;
    private TextView sourceTitleView;
    private TextView episodeTitleView;

    private Video currentVideo;
    private final List<VideoSource> sources = new ArrayList<>();

    private long currentSourceId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildVideoFromIntent();
        buildUi();
        loadSources();
        refreshEpisodeList();
    }

    /**
     * 从搜索页传入的信息构建基础 Video。
     *
     * 这样即使某个来源详情接口暂时不可用，
     * 详情页仍然可以正常打开，不会直接崩溃。
     */
    private void buildVideoFromIntent() {
        Intent intent = getIntent();

        String id = intent.getStringExtra("video_id");
        String name = intent.getStringExtra("video_name");
        String sourceName = intent.getStringExtra("source_name");

        currentVideo = new Video();

        currentVideo.setId(id);
        currentVideo.setName(name);
        currentVideo.setSourceName(sourceName);

        currentVideo.setPoster(intent.getStringExtra("poster"));
        currentVideo.setYear(intent.getStringExtra("year"));
        currentVideo.setArea(intent.getStringExtra("area"));
        currentVideo.setCategory(intent.getStringExtra("category"));
        currentVideo.setRemarks(intent.getStringExtra("remarks"));

        String sourceId = intent.getStringExtra("source_id");

        if (!TextUtils.isEmpty(sourceId)) {
            try {
                currentSourceId = Long.parseLong(sourceId);
                currentVideo.setSourceId(currentSourceId);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 创建整个详情页。
     * 使用代码创建 UI，减少 XML 文件数量。
     */
    private void buildUi() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        // 顶部栏
        root.addView(createTopBar());

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(12), dp(16), dp(30));

        scrollView.addView(content);
        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        // 标题
        titleView = new TextView(this);
        titleView.setText(
                TextUtils.isEmpty(currentVideo.getName())
                        ? "未知影片"
                        : currentVideo.getName()
        );
        titleView.setTextColor(TEXT);
        titleView.setTextSize(24);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        content.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        // 基础信息
        metaView = new TextView(this);
        metaView.setTextColor(SUB_TEXT);
        metaView.setTextSize(14);
        metaView.setPadding(0, dp(8), 0, dp(12));

        content.addView(metaView);

        updateMeta();

        // 简介卡片
        LinearLayout descriptionCard = createCard();

        TextView descriptionLabel = createSectionTitle("剧情简介");
        descriptionCard.addView(descriptionLabel);

        descriptionView = new TextView(this);
        descriptionView.setTextColor(SUB_TEXT);
        descriptionView.setTextSize(14);
        descriptionView.setLineSpacing(0, 1.3f);
        descriptionView.setText(
                TextUtils.isEmpty(currentVideo.getDescription())
                        ? "暂无剧情简介"
                        : currentVideo.getDescription()
        );

        descriptionCard.addView(
                descriptionView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        content.addView(descriptionCard);

        // 来源
        sourceTitleView = createSectionTitle("播放来源");
        sourceTitleView.setPadding(0, dp(20), 0, dp(10));
        content.addView(sourceTitleView);

        HorizontalScrollView sourceScroll = new HorizontalScrollView(this);
        sourceScroll.setHorizontalScrollBarEnabled(false);

        sourceContainer = new LinearLayout(this);
        sourceContainer.setOrientation(LinearLayout.HORIZONTAL);

        sourceScroll.addView(sourceContainer);
        content.addView(sourceScroll);

        // 剧集
        episodeTitleView = createSectionTitle("选集");
        episodeTitleView.setPadding(0, dp(20), 0, dp(10));
        content.addView(episodeTitleView);

        episodeContainer = new LinearLayout(this);
        episodeContainer.setOrientation(LinearLayout.VERTICAL);

        content.addView(episodeContainer);
    }

    private View createTopBar() {

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(6), dp(12), dp(6));
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
        title.setText("影片详情");
        title.setTextColor(TEXT);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        bar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView play = new TextView(this);
        play.setText("▶");
        play.setTextColor(ACCENT);
        play.setTextSize(20);
        play.setGravity(Gravity.CENTER);

        play.setOnClickListener(v -> playFirstEpisode());

        bar.addView(
                play,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(50)
                )
        );

        return bar;
    }

    /**
     * 加载当前影片对应的来源。
     */
    private void loadSources() {

        sourceContainer.removeAllViews();

        SourceManager manager = new SourceManager(this);

        try {
            List<VideoSource> all = manager.getAll();

            if (all != null) {
                for (VideoSource source : all) {

                    if (source == null || !source.isEnabled()) {
                        continue;
                    }

                    sources.add(source);
                }
            }
        } catch (Exception ignored) {
        } finally {
            manager.shutdown();
        }

        if (sources.isEmpty()) {

            String name = currentVideo.getSourceName();

            if (TextUtils.isEmpty(name)) {
                name = "当前来源";
            }

            TextView empty = createSourceButton(name);
            empty.setEnabled(false);

            sourceContainer.addView(empty);

            return;
        }

        // 优先选中搜索结果对应来源
        VideoSource selected = null;

        for (VideoSource source : sources) {
            if (source.getId() == currentSourceId) {
                selected = source;
                break;
            }
        }

        // 如果没有对应来源，则优先默认来源
        if (selected == null) {
            for (VideoSource source : sources) {
                if (source.isDefaultSource()) {
                    selected = source;
                    break;
                }
            }
        }

        if (selected == null) {
            selected = sources.get(0);
        }

        currentSourceId = selected.getId();

        for (VideoSource source : sources) {
            addSourceButton(source);
        }
    }

    private void addSourceButton(VideoSource source) {

        TextView button = createSourceButton(source.getDisplayName());

        updateSourceButtonStyle(button, source.getId() == currentSourceId);

        button.setOnClickListener(v -> {

            currentSourceId = source.getId();

            currentVideo.setSourceId(source.getId());
            currentVideo.setSourceName(source.getDisplayName());

            for (int i = 0; i < sourceContainer.getChildCount(); i++) {
                View child = sourceContainer.getChildAt(i);

                if (child instanceof TextView) {
                    String text = ((TextView) child).getText().toString();

                    updateSourceButtonStyle(
                            (TextView) child,
                            text.equals(source.getDisplayName())
                    );
                }
            }

            refreshEpisodeList();
        });

        sourceContainer.addView(button);
    }

    private TextView createSourceButton(String text) {

        TextView button = new TextView(this);

        button.setText(
                TextUtils.isEmpty(text)
                        ? "来源"
                        : text
        );

        button.setTextSize(14);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setPadding(dp(18), 0, dp(18), 0);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(42)
                );

        lp.setMargins(0, 0, dp(8), 0);

        button.setLayoutParams(lp);

        return button;
    }

    private void updateSourceButtonStyle(TextView button, boolean selected) {

        if (selected) {
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(ACCENT);
        } else {
            button.setTextColor(SUB_TEXT);
            button.setBackgroundColor(CARD_2);
        }
    }

    /**
     * 当前版本优先使用搜索页已经拿到的剧集。
     *
     * 如果没有剧集，则显示提示。
     * 后续来源详情接口接入后，这里可以直接替换成动态详情数据。
     */
    private void refreshEpisodeList() {

        episodeContainer.removeAllViews();

        List<Episode> episodes = currentVideo.getEpisodes();

        if (episodes == null || episodes.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("当前来源暂无可播放剧集");
            empty.setTextColor(SUB_TEXT);
            empty.setTextSize(14);
            empty.setPadding(dp(16), dp(20), dp(16), dp(20));

            episodeContainer.addView(empty);

            return;
        }

        for (int i = 0; i < episodes.size(); i++) {

            Episode episode = episodes.get(i);

            if (episode == null) {
                continue;
            }

            episodeContainer.addView(
                    createEpisodeButton(episode, i)
            );
        }
    }

    private TextView createEpisodeButton(
            Episode episode,
            int index
    ) {

        TextView button = new TextView(this);

        String name = episode.getName();

        if (TextUtils.isEmpty(name)) {
            name = "第 " + (index + 1) + " 集";
        }

        button.setText(name);
        button.setTextColor(TEXT);
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(16), 0, dp(16), 0);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        lp.setMargins(0, 0, 0, dp(7));

        button.setLayoutParams(lp);
        button.setBackgroundColor(CARD);

        button.setOnClickListener(v ->
                playEpisode(index)
        );

        return button;
    }

    private void playFirstEpisode() {

        List<Episode> episodes = currentVideo.getEpisodes();

        if (episodes == null || episodes.isEmpty()) {
            Toast.makeText(
                    this,
                    "暂无可播放剧集",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        playEpisode(0);
    }

    private void playEpisode(int index) {

        List<Episode> episodes = currentVideo.getEpisodes();

        if (episodes == null ||
                index < 0 ||
                index >= episodes.size()) {

            Toast.makeText(
                    this,
                    "剧集不存在",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Episode episode = episodes.get(index);

        if (episode == null ||
                TextUtils.isEmpty(episode.getPlayUrl())) {

            Toast.makeText(
                    this,
                    "当前剧集没有播放地址",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * PlayerActivity 使用 Intent 接收基础信息。
         * 同时把当前影片和剧集列表以 JSON 形式传递，
         * 方便后续继续完善播放器的连续播放与自动下一集。
         */
        Intent intent = new Intent(
                this,
                PlayerActivity.class
        );

        intent.putExtra(
                "video_id",
                currentVideo.getId()
        );

        intent.putExtra(
                "video_name",
                currentVideo.getName()
        );

        intent.putExtra(
                "source_id",
                String.valueOf(currentSourceId)
        );

        intent.putExtra(
                "source_name",
                currentVideo.getSourceName()
        );

        intent.putExtra(
                "episode_id",
                episode.getId()
        );

        intent.putExtra(
                "episode_name",
                episode.getName()
        );

        intent.putExtra(
                "episode_number",
                episode.getNumber()
        );

        intent.putExtra(
                "play_url",
                episode.getPlayUrl()
        );

        intent.putExtra(
                "episode_index",
                index
        );

        startActivity(intent);
    }

    private void updateMeta() {

        List<String> items = new ArrayList<>();

        if (!TextUtils.isEmpty(currentVideo.getYear())) {
            items.add(currentVideo.getYear());
        }

        if (!TextUtils.isEmpty(currentVideo.getArea())) {
            items.add(currentVideo.getArea());
        }

        if (!TextUtils.isEmpty(currentVideo.getCategory())) {
            items.add(currentVideo.getCategory());
        }

        if (!TextUtils.isEmpty(currentVideo.getRemarks())) {
            items.add(currentVideo.getRemarks());
        }

        if (!TextUtils.isEmpty(currentVideo.getSourceName())) {
            items.add("来源：" + currentVideo.getSourceName());
        }

        if (items.isEmpty()) {
            metaView.setText("暂无影片信息");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {

            if (i > 0) {
                builder.append("  ·  ");
            }

            builder.append(items.get(i));
        }

        metaView.setText(builder.toString());
    }

    private LinearLayout createCard() {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(15)
        );
        card.setBackgroundColor(CARD);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        card.setLayoutParams(lp);

        return card;
    }

    private TextView createSectionTitle(String text) {

        TextView title = new TextView(this);

        title.setText(text);
        title.setTextColor(TEXT);
        title.setTextSize(17);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        return title;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
