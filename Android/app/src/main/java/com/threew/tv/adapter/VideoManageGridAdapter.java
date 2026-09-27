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
 * 视频网格管理适配器。
 *
 * 适用于：
 * - 首页推荐
 * - 搜索结果网格
 * - 分类视频网格
 * - 平板
 * - Android TV
 * - TV 盒子
 *
 * 特点：
 * - 自动适配横屏/竖屏
 * - 海报优先
 * - 同名合并后的视频统一展示
 * - 显示来源
 * - 显示年份、地区、集数
 * - 支持遥控器焦点
 * - 支持点击和长按
 */
public class VideoManageGridAdapter
        extends RecyclerView.Adapter<VideoManageGridAdapter.VideoGridViewHolder> {

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

    public VideoManageGridAdapter(
            OnVideoClickListener clickListener,
            OnVideoLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
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
    public VideoGridViewHolder onCreateViewHolder(
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
                dp(parent, 6),
                dp(parent, 6),
                dp(parent, 6),
                dp(parent, 7)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rootParams.setMargins(
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3)
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
                        29,
                        34,
                        41
                )
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 210)
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(13);

        title.setTextColor(
                Color.rgb(
                        238,
                        242,
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
                        145,
                        157
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
                        dp(parent, 22)
                )
        );

        TextView source =
                new TextView(
                        parent.getContext()
                );

        source.setTextSize(8);

        source.setTextColor(
                Color.rgb(
                        90,
                        190,
                        212
                )
        );

        source.setGravity(
                Gravity.CENTER_VERTICAL
        );

        source.setSingleLine(true);

        source.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                source,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 21)
                )
        );

        return new VideoGridViewHolder(
                root,
                poster,
                title,
                meta,
                source
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoGridViewHolder holder,
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

        holder.source.setText(
                buildSource(video)
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
                (v, hasFocus) -> {

                    applyBackground(
                            v,
                            hasFocus
                    );

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.025f)
                                .scaleY(1.025f)
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

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(110)
                                .start();

                        holder.title.setTextColor(
                                Color.rgb(
                                        238,
                                        242,
                                        247
                                )
                        );
                    }
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
                v -> {

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

            builder.append(year);
        }

        if (!area.isEmpty()) {

            appendSeparator(builder);

            builder.append(
                    area
            );
        }

        if (episodeCount > 0) {

            appendSeparator(builder);

            builder.append(
                    episodeCount
            ).append("集");
        }

        if (builder.length() == 0) {

            String category =
                    safe(video.getCategory());

            if (!category.isEmpty()) {
                builder.append(category);
            }
        }

        return builder.toString();
    }

    private String buildSource(
            Video video
    ) {

        String source =
                safe(video.getSourceName());

        if (source.isEmpty()) {

            source = "未知来源";
        }

        return "来源：" + source;
    }

    private void appendSeparator(
            StringBuilder builder
    ) {

        if (builder.length() > 0) {

            builder.append(
                    "  ·  "
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
                                33,
                                47,
                                58
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

    static class VideoGridViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView meta;
        final TextView source;

        VideoGridViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView meta,
                TextView source
        ) {

            super(itemView);

            this.poster = poster;
            this.title = title;
            this.meta = meta;
            this.source = source;
        }
    }
}
