package com.threew.tv.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用视频列表适配器。
 *
 * 用于：
 * - 搜索结果
 * - 首页视频列表
 * - 分类列表
 * - 多来源合并后的结果
 *
 * 不负责网络请求，只负责展示 Video。
 */
public class VideoListAdapter
        extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(
                Video video,
                int position
        );
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(
                Video video,
                int position
        );
    }

    private final List<Video> data =
            new ArrayList<>();

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public VideoListAdapter(
            OnItemClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public VideoListAdapter(
            OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Video> videos
    ) {

        data.clear();

        if (videos != null) {
            data.addAll(videos);
        }

        notifyDataSetChanged();
    }

    public void addAll(
            List<Video> videos
    ) {

        if (videos == null ||
                videos.isEmpty()) {

            return;
        }

        int start =
                data.size();

        data.addAll(videos);

        notifyItemRangeInserted(
                start,
                videos.size()
        );
    }

    public void add(
            Video video
    ) {

        if (video == null) {
            return;
        }

        data.add(video);

        notifyItemInserted(
                data.size() - 1
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return;
        }

        data.remove(position);

        notifyItemRemoved(position);
    }

    public void clear() {

        int count =
                data.size();

        if (count == 0) {
            return;
        }

        data.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public Video getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return null;
        }

        return data.get(position);
    }

    public List<Video> getItems() {
        return new ArrayList<>(data);
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
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 14),
                dp(parent, 10),
                dp(parent, 14),
                dp(parent, 10)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 94)
                );

        params.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(params);

        LinearLayout titleRow =
                new LinearLayout(
                        parent.getContext()
                );

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                titleRow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 30)
                )
        );

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setText("▶");

        icon.setTextSize(12);

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(
                        105,
                        205,
                        235
                )
        );

        titleRow.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 30),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(15);

        title.setTextColor(
                Color.rgb(
                        240,
                        243,
                        248
                )
        );

        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        title.setMaxLines(1);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        titleRow.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView source =
                new TextView(
                        parent.getContext()
                );

        source.setTextSize(9);

        source.setTextColor(
                Color.rgb(
                        110,
                        205,
                        230
                )
        );

        source.setGravity(
                Gravity.CENTER
        );

        source.setMaxLines(1);

        source.setEllipsize(
                TextUtils.TruncateAt.END
        );

        titleRow.addView(
                source,
                new LinearLayout.LayoutParams(
                        dp(parent, 72),
                        dp(parent, 22)
                )
        );

        TextView detail =
                new TextView(
                        parent.getContext()
                );

        detail.setTextSize(10);

        detail.setTextColor(
                Color.rgb(
                        145,
                        153,
                        165
                )
        );

        detail.setGravity(
                Gravity.CENTER_VERTICAL
        );

        detail.setMaxLines(1);

        detail.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                detail,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 25)
                )
        );

        TextView remarks =
                new TextView(
                        parent.getContext()
                );

        remarks.setTextSize(9);

        remarks.setTextColor(
                Color.rgb(
                        92,
                        103,
                        115
                )
        );

        remarks.setGravity(
                Gravity.CENTER_VERTICAL
        );

        remarks.setMaxLines(1);

        remarks.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                remarks,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        return new VideoViewHolder(
                root,
                title,
                source,
                detail,
                remarks
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoViewHolder holder,
            int position
    ) {

        Video video =
                data.get(position);

        String title =
                safe(video.getName());

        if (title.isEmpty()) {
            title = "未命名视频";
        }

        holder.title.setText(title);

        String sourceName =
                safe(video.getSourceName());

        if (sourceName.isEmpty()) {
            sourceName = "来源";
        }

        holder.source.setText(
                sourceName
        );

        holder.detail.setText(
                buildDetail(video)
        );

        holder.remarks.setText(
                buildRemarks(video)
        );

        holder.itemView.setBackgroundColor(
                Color.rgb(
                        20,
                        23,
                        30
                )
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                Color.rgb(
                                        32,
                                        45,
                                        56
                                )
                        );

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackgroundColor(
                                Color.rgb(
                                        20,
                                        23,
                                        30
                                )
                        );

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

                        clickListener.onItemClick(
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
                                .onItemLongClick(
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private String buildDetail(
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

        int episodeCount = 0;

        if (video.getEpisodes() != null) {

            episodeCount =
                    video.getEpisodes().size();
        }

        if (episodeCount > 0) {

            appendSeparator(builder);

            builder.append(
                    episodeCount
            ).append("集");
        }

        if (builder.length() == 0) {

            return "暂无详细信息";
        }

        return builder.toString();
    }

    private String buildRemarks(
            Video video
    ) {

        String remarks =
                safe(video.getRemarks());

        if (!remarks.isEmpty()) {
            return remarks;
        }

        String actor =
                safe(video.getActor());

        if (!actor.isEmpty()) {
            return "主演：" + actor;
        }

        String director =
                safe(video.getDirector());

        if (!director.isEmpty()) {
            return "导演：" + director;
        }

        return "点击查看详情";
    }

    private void appendSeparator(
            StringBuilder builder
    ) {

        if (builder.length() > 0) {
            builder.append("  ·  ");
        }
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VideoViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView source;
        final TextView detail;
        final TextView remarks;

        VideoViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView source,
                TextView detail,
                TextView remarks
        ) {

            super(itemView);

            this.title = title;
            this.source = source;
            this.detail = detail;
            this.remarks = remarks;
        }
    }
}
