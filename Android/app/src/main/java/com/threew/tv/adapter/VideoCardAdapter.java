package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Video;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用视频卡片适配器。
 *
 * 用于：
 * - 首页横向推荐区
 * - 猜你喜欢
 * - 最近观看推荐
 * - 收藏推荐
 * - 搜索结果横向卡片
 *
 * 设计目标：
 * - 手机 / 平板 / TV 通用
 * - 适合横向 RecyclerView
 * - 支持遥控器焦点
 * - 不依赖 XML 布局
 * - 保持深色科技风
 */
public class VideoCardAdapter
        extends RecyclerView.Adapter<VideoCardAdapter.VideoCardViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(
                Video video,
                int position
        );
    }

    public interface OnVideoLongClickListener {
        boolean onVideoLongClick(
                Video video,
                int position
        );
    }

    private final List<Video> videos =
            new ArrayList<>();

    private final OnVideoClickListener clickListener;
    private final OnVideoLongClickListener longClickListener;

    private int cardWidthDp = 148;
    private int posterHeightDp = 208;

    public VideoCardAdapter(
            OnVideoClickListener clickListener
    ) {

        this(
                clickListener,
                null
        );
    }

    public VideoCardAdapter(
            OnVideoClickListener clickListener,
            OnVideoLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    /**
     * 设置卡片尺寸。
     */
    public void setCardSize(
            int widthDp,
            int posterHeightDp
    ) {

        if (widthDp > 0) {
            this.cardWidthDp = widthDp;
        }

        if (posterHeightDp > 0) {
            this.posterHeightDp = posterHeightDp;
        }

        notifyDataSetChanged();
    }

    public void setData(
            List<Video> data
    ) {

        videos.clear();

        if (data != null) {
            videos.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void add(
            Video video
    ) {

        if (video == null) {
            return;
        }

        videos.add(video);

        notifyItemInserted(
                videos.size() - 1
        );
    }

    public void addAll(
            List<Video> data
    ) {

        if (data == null ||
                data.isEmpty()) {

            return;
        }

        int start =
                videos.size();

        videos.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= videos.size()) {

            return;
        }

        videos.remove(position);

        notifyItemRemoved(position);
    }

    public void clear() {

        int count =
                videos.size();

        if (count == 0) {
            return;
        }

        videos.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public Video getItem(
            int position
    ) {

        if (position < 0 ||
                position >= videos.size()) {

            return null;
        }

        return videos.get(position);
    }

    public List<Video> getItems() {
        return new ArrayList<>(videos);
    }

    @NonNull
    @Override
    public VideoCardViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LinearLayout root =
                new LinearLayout(
                        parent.getContext()
                );

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.TOP
        );

        root.setPadding(
                dp(parent, 5),
                dp(parent, 5),
                dp(parent, 5),
                dp(parent, 6)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        dp(
                                parent,
                                cardWidthDp
                        ),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(
                rootParams
        );

        ImageView poster =
                new ImageView(
                        parent.getContext()
                );

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        poster.setBackgroundColor(
                Color.rgb(
                        28,
                        33,
                        40
                )
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(
                                parent,
                                posterHeightDp
                        )
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(12);

        title.setTextColor(
                Color.rgb(
                        240,
                        243,
                        247
                )
        );

        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        title.setSingleLine(true);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 30)
                );

        titleParams.setMargins(
                dp(parent, 2),
                dp(parent, 3),
                dp(parent, 2),
                0
        );

        root.addView(
                title,
                titleParams
        );

        TextView meta =
                new TextView(
                        parent.getContext()
                );

        meta.setTextSize(8);

        meta.setTextColor(
                Color.rgb(
                        125,
                        140,
                        153
                )
        );

        meta.setGravity(
                Gravity.CENTER_VERTICAL
        );

        meta.setSingleLine(true);

        meta.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                meta,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        return new VideoCardViewHolder(
                root,
                poster,
                title,
                meta
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoCardViewHolder holder,
            int position
    ) {

        Video video =
                videos.get(position);

        String title =
                safe(video.getName());

        if (title.isEmpty()) {
            title = "未命名视频";
        }

        holder.title.setText(
                title
        );

        holder.meta.setText(
                buildMeta(video)
        );

        holder.poster.setImageDrawable(
                null
        );

        String poster =
                safe(video.getPoster());

        if (!poster.isEmpty()) {

            try {

                ImageLoader.load(
                        holder.itemView.getContext(),
                        poster,
                        holder.poster
                );

            } catch (Exception ignored) {
            }
        }

        applyBackground(
                holder.itemView,
                false
        );

        holder.itemView.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    applyBackground(
                            view,
                            hasFocus
                    );

                    if (hasFocus) {

                        view.animate()
                                .scaleX(1.035f)
                                .scaleY(1.035f)
                                .setDuration(110)
                                .start();

                        holder.title.setTextColor(
                                Color.rgb(
                                        255,
                                        255,
                                        255
                                )
                        );

                    } else {

                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(110)
                                .start();

                        holder.title.setTextColor(
                                Color.rgb(
                                        240,
                                        243,
                                        247
                                )
                        );
                    }
                }
        );

        holder.itemView.setOnClickListener(
                view -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    if (clickListener != null) {

                        clickListener.onVideoClick(
                                videos.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );

        holder.itemView.setOnLongClickListener(
                view -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return false;
                    }

                    if (longClickListener != null) {

                        return longClickListener
                                .onVideoLongClick(
                                        videos.get(
                                                adapterPosition
                                        ),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private String buildMeta(
            Video video
    ) {

        StringBuilder builder =
                new StringBuilder();

        String year =
                safe(video.getYear());

        String area =
                safe(video.getArea());

        int episodeCount = 0;

        if (video.getEpisodes() != null) {

            episodeCount =
                    video.getEpisodes().size();
        }

        if (!year.isEmpty()) {

            builder.append(
                    year
            );
        }

        if (!area.isEmpty()) {

            appendSeparator(
                    builder
            );

            builder.append(
                    area
            );
        }

        if (episodeCount > 0) {

            appendSeparator(
                    builder
            );

            builder.append(
                    episodeCount
            ).append("集");
        }

        if (builder.length() == 0) {

            String category =
                    safe(video.getCategory());

            if (!category.isEmpty()) {

                builder.append(
                        category
                );
            }
        }

        return builder.toString();
    }

    private void appendSeparator(
            StringBuilder builder
    ) {

        if (builder.length() > 0) {

            builder.append(
                    " · "
            );
        }
    }

    private void applyBackground(
            View view,
            boolean focused
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                focused
                        ? Color.rgb(
                                34,
                                48,
                                60
                        )
                        : Color.rgb(
                                20,
                                23,
                                30
                        )
        );

        drawable.setStroke(
                dpValue(view, 1),
                focused
                        ? Color.rgb(
                                75,
                                140,
                                160
                        )
                        : Color.rgb(
                                43,
                                51,
                                61
                        )
        );

        drawable.setCornerRadius(
                dpValue(view, 9)
        );

        view.setBackground(
                drawable
        );
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

    private int dpValue(
            View view,
            int value
    ) {

        return Math.round(
                value *
                        view.getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoCardViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView meta;

        VideoCardViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView meta
        ) {

            super(itemView);

            this.poster = poster;
            this.title = title;
            this.meta = meta;
        }
    }
}
