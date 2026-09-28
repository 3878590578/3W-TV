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

        contentParams.leftMargin = dp(13);

        root.addView(
                content,
                contentParams
        );

        TextView title =
                new TextView(context);

        title.setTextSize(15);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setMaxLines(2);

        title.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        content.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView source =
                new TextView(context);

        source.setTextSize(12);
        source.setAlpha(0.72f);
        source.setSingleLine(true);

        LinearLayout.LayoutParams sourceParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        sourceParams.topMargin = dp(5);

        content.addView(
                source,
                sourceParams
        );

        TextView time =
                new TextView(context);

        time.setTextSize(10);
        time.setAlpha(0.55f);
        time.setSingleLine(true);

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        timeParams.topMargin = dp(4);

        content.addView(
                time,
                timeParams
        );

        return new ViewHolder(
                root,
                poster,
                title,
                source,
                time
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Favorite favorite =
                favorites.get(position);

        String name =
                favorite.getName();

        if (name == null ||
                name.trim().isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        String poster =
                favorite.getPoster();

        if (poster != null &&
                !poster.trim().isEmpty()) {

            ImageLoader.load(
                    context,
                    poster,
                    holder.poster
            );

        } else {
            holder.poster.setImageDrawable(null);
        }

        String sourceName =
                favorite.getSourceName();

        if (sourceName == null ||
                sourceName.trim().isEmpty()) {
            sourceName = "默认来源";
        }

        holder.source.setText(
                "来源："
                        + sourceName
        );

        long createTime =
                favorite.getCreateTime();

        if (createTime > 0) {
            holder.time.setText(
                    formatTime(createTime)
            );
        } else {
            holder.time.setText("");
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onFavoriteClick(
                        favorites.get(adapterPosition),
                        adapterPosition
                );
            }
        });
    }

    private String formatTime(long time) {
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        java.util.Locale.getDefault()
                );

        return format.format(
                new java.util.Date(time)
        );
    }

    private int dp(int value) {
        return (int) (
                value
                        * context.getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        private final ImageView poster;
        private final TextView title;
        private final TextView source;
        private final TextView time;

        public ViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView source,
                TextView time
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.source = source;
            this.time = time;
        }
    }
}