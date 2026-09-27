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
 * 视频管理/列表适配器。
 *
 * 用于：
 * - 搜索结果
 * - 首页视频列表
 * - 视频管理
 * - 同名合并后的结果展示
 * - TV 遥控器焦点操作
 */
public class VideoManageAdapter
        extends RecyclerView.Adapter<VideoManageAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(Video video, int position);
    }

    public interface OnVideoLongClickListener {
        boolean onVideoLongClick(Video video, int position);
    }

    private final List<Video> videos =
            new ArrayList<>();

    private final OnVideoClickListener clickListener;
    private final OnVideoLongClickListener longClickListener;

    public VideoManageAdapter(
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

        int count = videos.size();

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
    public VideoViewHolder onCreateViewHolder(
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

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 108)
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(rootParams);

        ImageView poster =
                new ImageView(
                        parent.getContext()
                );

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        poster.setBackgroundColor(
                Color.rgb(
                        30,
                        35,
                        42
                )
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        dp(parent, 66),
                        dp(parent, 92)
                )
        );

        LinearLayout content =
                new LinearLayout(
                        parent.getContext()
                );

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

        contentParams.setMargins(
                dp(parent, 10),
                0,
                dp(parent, 6),
                0
        );

        root.addView(
                content,
                contentParams
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(15);

        title.setTextColor(
                Color.rgb(
                        240,
                        244,
                        248
                )
        );

        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        title.setSingleLine(true);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 30)
                )
        );

        TextView remarks =
                new TextView(
                        parent.getContext()
                );

        remarks.setTextSize(9);

        remarks.setTextColor(
                Color.rgb(
                        135,
                        150,
                        162
                )
        );

        remarks.setGravity(
                Gravity.CENTER_VERTICAL
        );

        remarks.setSingleLine(true);

        remarks.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
                remarks,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                )
        );

        TextView info =
                new TextView(
                        parent.getContext()
                );

        info.setTextSize(9);

        info.setTextColor(
                Color.rgb(
                        105,
                        190,
                        210
                )
        );

        info.setGravity(
                Gravity.CENTER_VERTICAL
        );

        info.setSingleLine(true);

        info.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
                info,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                )
        );

        TextView source =
                new TextView(
                        parent.getContext()
                );

        source.setTextSize(8);

        source.setTextColor(
                Color.rgb(
                        110,
                        118,
                        130
                )
        );

        source.setGravity(
                Gravity.CENTER_VERTICAL
        );

        source.setSingleLine(true);

        source.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
                source,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        TextView arrow =
                new TextView(
                        parent.getContext()
                );

        arrow.setText("›");

        arrow.setTextSize(24);

        arrow.setTextColor(
                Color.rgb(
                        95,
                        110,
                        125
                )
        );

        arrow.setGravity(
                Gravity.CENTER
        );

        root.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(parent, 24),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new VideoViewHolder(
                root,
                poster,
                title,
                remarks,
                info,
                source,
                arrow
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoViewHolder holder,
            int position
    ) {

        Video video =
                videos.get(position);

        String name =
                safe(video.getName());

        if (name.isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        String remarks =
                safe(video.getRemarks());

        if (remarks.isEmpty()) {

            remarks = buildRemarks(video);
        }

        holder.remarks.setText(
                remarks
        );

        holder.info.setText(
                buildInfo(video)
        );

        holder.source.setText(
                buildSource(video)
        );

        holder.poster.setImageDrawable(null);

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

                    holder.arrow.setTextColor(
                            hasFocus
                                    ? Color.rgb(
                                            105,
                                            210,
                                            230
                                    )
                                    : Color.rgb(
                                            95,
                                            110,
                                            125
                                    )
                    );

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
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

    private String buildRemarks(
            Video video
    ) {

        StringBuilder builder =
                new StringBuilder();

        String year =
                safe(video.getYear());

        String area =
                safe(video.getArea());

        String category =
                safe(video.getCategory());

        if (!year.isEmpty()) {

            builder.append(year);
        }

        if (!area.isEmpty()) {

            appendSeparator(builder);
            builder.append(area);
        }

        if (!category.isEmpty()) {

            appendSeparator(builder);
            builder.append(category);
        }

        return builder.toString();
    }

    private String buildInfo(
            Video video
    ) {

        int episodeCount = 0;

        if (video.getEpisodes() != null) {

            episodeCount =
                    video.getEpisodes().size();
        }

        String actor =
                safe(video.getActor());

        String director =
                safe(video.getDirector());

        StringBuilder builder =
                new StringBuilder();

        if (episodeCount > 0) {

            builder.append(
                    episodeCount
            ).append("集");
        }

        if (!actor.isEmpty()) {

            appendSeparator(builder);
            builder.append("主演：")
                    .append(
                            limit(
                                    actor,
                                    34
                            )
                    );
        }

        if (!director.isEmpty()) {

            appendSeparator(builder);
            builder.append("导演：")
                    .append(
                            limit(
                                    director,
                                    20
                            )
                    );
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

            builder.append("  ·  ");
        }
    }

    private String limit(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                Math.max(
                        0,
                        maxLength - 1
                )
        ) + "…";
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
                                32,
                                45,
                                56
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
                                135,
                                155
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

    static class VideoViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView remarks;
        final TextView info;
        final TextView source;
        final TextView arrow;

        VideoViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView remarks,
                TextView info,
                TextView source,
                TextView arrow
        ) {

            super(itemView);

            this.poster = poster;
            this.title = title;
            this.remarks = remarks;
            this.info = info;
            this.source = source;
            this.arrow = arrow;
        }
    }
}
