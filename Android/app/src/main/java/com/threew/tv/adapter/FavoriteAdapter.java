package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 收藏列表适配器。
 *
 * 显示：
 * - 海报
 * - 视频名称
 * - 来源
 * - 收藏时间
 * - 长按删除
 */
public class FavoriteAdapter
        extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(
                Favorite favorite,
                int position
        );
    }

    public interface OnFavoriteLongClickListener {
        boolean onFavoriteLongClick(
                Favorite favorite,
                int position
        );
    }

    private final List<Favorite> data =
            new ArrayList<>();

    private final OnFavoriteClickListener clickListener;
    private final OnFavoriteLongClickListener longClickListener;

    public FavoriteAdapter(
            OnFavoriteClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public FavoriteAdapter(
            OnFavoriteClickListener clickListener,
            OnFavoriteLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Favorite> favorites
    ) {

        data.clear();

        if (favorites != null) {
            data.addAll(favorites);
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<Favorite> favorites
    ) {

        if (favorites == null ||
                favorites.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(favorites);

        notifyItemRangeInserted(
                start,
                favorites.size()
        );
    }

    public void clear() {

        int size = data.size();

        data.clear();

        if (size > 0) {
            notifyItemRangeRemoved(
                    0,
                    size
            );
        }
    }

    public Favorite getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public List<Favorite> getItems() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LinearLayout root =
                new LinearLayout(
                        parent.getContext()
                );

        root.setOrientation(
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 10),
                dp(parent, 8),
                dp(parent, 10),
                dp(parent, 8)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 104)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
        );

        root.setLayoutParams(params);

        ImageView poster =
                new ImageView(
                        parent.getContext()
                );

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        poster.setBackgroundColor(
                Color.rgb(35, 39, 49)
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        dp(parent, 70),
                        dp(parent, 88)
                )
        );

        LinearLayout info =
                new LinearLayout(
                        parent.getContext()
                );

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        info.setGravity(
                Gravity.CENTER_VERTICAL
        );

        info.setPadding(
                dp(parent, 12),
                0,
                dp(parent, 4),
                0
        );

        root.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextColor(
                Color.rgb(245, 247, 250)
        );

        title.setTextSize(15);

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setMaxLines(2);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        info.addView(title);

        TextView source =
                new TextView(
                        parent.getContext()
                );

        source.setTextColor(
                Color.rgb(55, 180, 255)
        );

        source.setTextSize(11);

        source.setMaxLines(1);

        source.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams sourceParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        sourceParams.setMargins(
                0,
                dp(parent, 6),
                0,
                0
        );

        info.addView(
                source,
                sourceParams
        );

        TextView time =
                new TextView(
                        parent.getContext()
                );

        time.setTextColor(
                Color.rgb(105, 115, 130)
        );

        time.setTextSize(9);

        time.setMaxLines(1);

        time.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        timeParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        info.addView(
                time,
                timeParams
        );

        TextView star =
                new TextView(
                        parent.getContext()
                );

        star.setText("★");
        star.setTextColor(
                Color.rgb(255, 190, 70)
        );
        star.setTextSize(20);
        star.setGravity(
                Gravity.CENTER
        );

        root.addView(
                star,
                new LinearLayout.LayoutParams(
                        dp(parent, 44),
                        dp(parent, 50)
                )
        );

        return new FavoriteViewHolder(
                root,
                poster,
                title,
                source,
                time,
                star
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FavoriteViewHolder holder,
            int position
    ) {

        Favorite favorite =
                data.get(position);

        String name =
                safe(favorite.getName());

        if (name.isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        String sourceName =
                safe(favorite.getSourceName());

        if (sourceName.isEmpty()) {

            holder.source.setText(
                    "来源：未知"
            );

        } else {

            holder.source.setText(
                    "来源：" +
                            sourceName
            );
        }

        long createTime =
                favorite.getCreateTime();

        if (createTime > 0) {

            holder.time.setText(
                    "收藏于：" +
                            formatTime(createTime)
            );

        } else {

            holder.time.setText(
                    "已收藏"
            );
        }

        holder.poster.setImageDrawable(
                null
        );

        String poster =
                safe(favorite.getPoster());

        if (!poster.isEmpty()) {

            ImageLoader.load(
                    holder.itemView.getContext(),
                    poster,
                    holder.poster
            );
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    v.setBackgroundColor(
                            hasFocus
                                    ? Color.rgb(28, 58, 78)
                                    : Color.rgb(20, 23, 31)
                    );
                }
        );

        holder.itemView.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {
                        return;
                    }

                    if (clickListener != null) {

                        clickListener.onFavoriteClick(
                                data.get(adapterPosition),
                                adapterPosition
                        );
                    }
                }
        );

        holder.itemView.setOnLongClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {
                        return false;
                    }

                    if (longClickListener != null) {

                        return longClickListener
                                .onFavoriteLongClick(
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class FavoriteViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView source;
        final TextView time;
        final TextView star;

        FavoriteViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView source,
                TextView time,
                TextView star
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.source = source;
            this.time = time;
            this.star = star;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String formatTime(
            long timestamp
    ) {

        try {

            return new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
            ).format(
                    new Date(timestamp)
            );

        } catch (Exception e) {

            return "";
        }
    }

    private int dp(
            ViewGroup parent,
            int value
    ) {

        return Math.round(
                value *
                        parent.getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}
