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

import com.threew.tv.model.Favorite;
import com.threew.tv.model.Video;
import com.threew.tv.model.Episode;
import com.threew.tv.database.FavoriteDao;
import com.threew.tv.player.PlayerActivity;
import com.threew.tv.utils.FormatUtils;

import java.util.List;

/**
 * 3W影视 - 收藏
 *
 * 功能：
 * 1. 显示全部收藏
 * 2. 点击进入影片详情
 * 3. 长按删除收藏
 * 4. 支持手机、平板、TV
 */
public class FavoriteActivity extends AppCompatActivity {

    private static final int BG = Color.rgb(10, 12, 18);
    private static final int CARD = Color.rgb(20, 23, 31);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int SUB_TEXT = Color.rgb(160, 168, 182);
    private static final int ACCENT = Color.rgb(55, 180, 255);
    private static final int DANGER = Color.rgb(255, 90, 90);

    private LinearLayout listContainer;
    private FavoriteDao favoriteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoriteDao = new FavoriteDao(this);

        buildUi();
        loadFavorites();
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
        title.setText("我的收藏");
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

        clear.setOnClickListener(v -> clearFavorites());

        bar.addView(
                clear,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(50)
                )
        );

        return bar;
    }

    private void loadFavorites() {

        listContainer.removeAllViews();

        List<Favorite> list;

        try {
            list = favoriteDao.getAll();
        } catch (Exception e) {
            list = null;
        }

        if (list == null || list.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("暂无收藏影片");
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

        for (Favorite favorite : list) {

            if (favorite == null) {
                continue;
            }

            listContainer.addView(
                    createFavoriteItem(favorite)
            );
        }
    }

    private View createFavoriteItem(
            Favorite favorite
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(
                LinearLayout.HORIZONTAL
        );
        card.setGravity(
                Gravity.CENTER_VERTICAL
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

        LinearLayout textContainer =
                new LinearLayout(this);

        textContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        String name = favorite.getName();

        if (TextUtils.isEmpty(name)) {
            name = "未知影片";
        }

        TextView title = new TextView(this);
        title.setText(name);
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

        textContainer.addView(title);

        String sourceName =
                favorite.getSourceName();

        if (!TextUtils.isEmpty(sourceName)) {

            TextView source =
                    new TextView(this);

            source.setText(
                    "来源：" + sourceName
            );
            source.setTextColor(SUB_TEXT);
            source.setTextSize(13);
            source.setPadding(
                    0,
                    dp(7),
                    0,
                    0
            );

            textContainer.addView(source);
        }

        card.addView(
                textContainer,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView remove =
                new TextView(this);

        remove.setText("×");
        remove.setTextColor(DANGER);
        remove.setTextSize(26);
        remove.setGravity(Gravity.CENTER);

        remove.setPadding(
                dp(10),
                0,
                dp(4),
                0
        );

        remove.setOnClickListener(
                v -> removeFavorite(favorite)
        );

        card.addView(
                remove,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(50)
                )
        );

        card.setOnClickListener(
                v -> openFavorite(favorite)
        );

        card.setOnLongClickListener(v -> {

            removeFavorite(favorite);
            return true;
        });

        card.setFocusable(true);
        card.setClickable(true);

        return card;
    }

    private void openFavorite(
            Favorite favorite
    ) {

        if (favorite == null) {
            return;
        }

        Intent intent = new Intent(
                this,
                DetailActivity.class
        );

        intent.putExtra(
                "video_id",
                favorite.getVideoId()
        );

        intent.putExtra(
                "video_name",
                favorite.getName()
        );

        intent.putExtra(
                "source_id",
                String.valueOf(
                        favorite.getSourceId()
                )
        );

        intent.putExtra(
                "source_name",
                favorite.getSourceName()
        );

        intent.putExtra(
                "poster",
                favorite.getPoster()
        );

        startActivity(intent);
    }

    private void removeFavorite(
            Favorite favorite
    ) {

        if (favorite == null) {
            return;
        }

        try {

            favoriteDao.removeById(
                    favorite.getId()
            );

            Toast.makeText(
                    this,
                    "已取消收藏",
                    Toast.LENGTH_SHORT
            ).show();

            loadFavorites();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "取消收藏失败",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void clearFavorites() {

        try {

            favoriteDao.clearAll();

            Toast.makeText(
                    this,
                    "收藏已清空",
                    Toast.LENGTH_SHORT
            ).show();

            loadFavorites();

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
            loadFavorites();
        }
    }

    @Override
    protected void onDestroy() {
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
