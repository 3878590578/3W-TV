package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Favorite;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏卡片适配器。
 *
 * 用于“我的 / 收藏”页面。
 *
 * 显示：
 * - 视频海报
 * - 视频名称
 * - 来源
 * - 收藏时间
 */
public class FavoriteCardAdapter
        extends RecyclerView.Adapter<FavoriteCardAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(
                Favorite favorite,
                int position
        );
    }

    private final Context context;
    private final List<Favorite> favorites =
            new ArrayList<>();

    private OnFavoriteClickListener listener;

    public FavoriteCardAdapter(Context context) {
        this.context = context;
    }

    public FavoriteCardAdapter(
            Context context,
            List<Favorite> data
    ) {
        this.context = context;

        if (data != null) {
            favorites.addAll(data);
        }
    }

    public void setOnFavoriteClickListener(
            OnFavoriteClickListener listener
    ) {
        this.listener = listener;
    }

    public void setFavorites(
            List<Favorite> data
    ) {
        favorites.clear();

        if (data != null) {
            favorites.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addFavorites(
            List<Favorite> data
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = favorites.size();

        favorites.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void remove(int position) {
        if (position < 0 ||
                position >= favorites.size()) {
            return;
        }

        favorites.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        favorites.clear();
        notifyDataSetChanged();
    }

    public Favorite getItem(int position) {
        if (position < 0 ||
                position >= favorites.size()) {
            return null;
        }

        return favorites.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setFocusable(true);
        root.setClickable(true);

        int margin = dp(5);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(118)
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        ImageView poster =
                new ImageView(context);

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        dp(82),
                        dp(106)
                )
        );

        LinearLayout content =
                new LinearLayout(context);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );

        contentParams.left
