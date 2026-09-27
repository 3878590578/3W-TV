package com.threew.tv.adapter;

import android.content.Context;
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

import com.threew.tv.model.Video;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用视频列表适配器。
 *
 * 用于：
 * 搜索结果
 * 历史记录
 * 收藏
 * 后续首页推荐
 *
 * 不依赖固定 XML，方便手机、平板、电视统一适配。
 */
public class VideoAdapter
        extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(Video video, int position);
    }

    private final Context context;
    private final List<Video> data = new ArrayList<>();
    private final OnVideoClickListener listener;

    public VideoAdapter(
            Context context,
            OnVideoClickListener listener
    ) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<Video> videos) {

        data.clear();

        if (videos != null) {
            data.addAll(videos);
        }

        notifyDataSetChanged();
    }

    public void addData(List<Video> videos) {

        if (videos == null || videos.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(videos);

        notifyItemRangeInserted(
                start,
                videos.size()
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

    public Video getItem(int position) {

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
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(108)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(6)
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
                        dp(70),
                        dp(92)
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
                dp(12),
                0,
                dp(4),
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

        TextView meta =
                new TextView(
                        parent.getContext()
                );

        meta.setTextColor(
                Color.rgb(160, 168, 182)
        );

        meta.setTextSize(11);

        meta.setMaxLines(2);

        meta.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams metaLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        metaLp.setMargins(
                0,
                dp(6),
                0,
                0
        );

        info.addView(
                meta,
                metaLp
        );

        TextView remarks =
                new TextView(
                        parent.getContext()
                );

        remarks.setTextColor(
                Color.rgb(115, 124, 140)
        );

        remarks.setTextSize(10);

        remarks.setMaxLines(2);

        remarks.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams remarkLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        remarkLp.setMargins(
                0,
                dp(5),
                0,
                0
        );

        info.addView(
                remarks,
                remarkLp
        );

        TextView arrow =
                new TextView(
                        parent.getContext()
                );

        arrow.setText("›");

        arrow.setTextColor(
                Color.rgb(120, 130, 145)
        );

        arrow.setTextSize(25);

        arrow.setGravity(
                Gravity.CENTER
        );

        root.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(70)
                )
        );

        return new VideoViewHolder(
                root,
                poster,
                title,
                meta,
                remarks
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoViewHolder holder,
            int position
    ) {

        Video video = data.get(position);

        String name =
                safe(video.getName());

        if (name.isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        StringBuilder meta =
                new StringBuilder();

        if (!safe(video.getYear()).isEmpty()) {
            meta.append(
                    video.getYear()
            );
        }

        if (!safe(video.getArea()).isEmpty()) {

            if (meta.length() > 0) {
                meta.append("  ·  ");
            }

            meta.append(
                    video.getArea()
            );
        }

        if (!safe(video.getCategory()).isEmpty()) {

            if (meta.length() > 0) {
                meta.append("  ·  ");
            }

            meta.append(
                    video.getCategory()
            );
        }

        if (!safe(video.getSourceName()).isEmpty()) {

            if (meta.length() > 0) {
                meta.append("  ·  ");
            }

            meta.append(
                    "来源："
            );

            meta.append(
                    video.getSourceName()
            );
        }

        holder.meta.setText(
                meta.toString()
        );

        String remarks =
                safe(video.getRemarks());

        if (remarks.isEmpty()) {
            holder.remarks.setVisibility(
                    View.GONE
            );
        } else {
            holder.remarks.setVisibility(
                    View.VISIBLE
            );

            holder.remarks.setText(
                    remarks
            );
        }

        holder.poster.setImageDrawable(
                null
        );

        String poster =
                safe(video.getPoster());

        if (!poster.isEmpty()) {

            ImageLoader.load(
                    context,
                    poster,
                    holder.poster
            );
        }

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onVideoClick(
                                video,
                                holder.getBindingAdapterPosition()
                        );
                    }
                }
        );

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VideoViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView meta;
        final TextView remarks;

        VideoViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView meta,
                TextView remarks
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.meta = meta;
            this.remarks = remarks;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private int dp(int value) {

        return Math.round(
                value *
                        context.getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}
