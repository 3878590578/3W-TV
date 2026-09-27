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

import com.threew.tv.model.Video;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页/搜索结果的网格视频适配器。
 *
 * 适合：
 * - 手机双列
 * - 平板多列
 * - Android TV 横向焦点浏览
 *
 * 显示：
 * - 海报
 * - 视频名称
 * - 年份/地区
 * - 来源
 * - 备注
 */
public class VideoGridAdapter
        extends RecyclerView.Adapter<VideoGridAdapter.VideoGridViewHolder> {

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

    private final List<Video> data =
            new ArrayList<>();

    private final OnVideoClickListener clickListener;
    private final OnVideoLongClickListener longClickListener;

    public VideoGridAdapter(
            OnVideoClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public VideoGridAdapter(
            OnVideoClickListener clickListener,
            OnVideoLongClickListener longClickListener
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

    public void addData(
            List<Video> videos
    ) {

        if (videos == null ||
                videos.isEmpty()) {
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

        root.setPadding(
                dp(parent, 6),
                dp(parent, 6),
                dp(parent, 6),
                dp(parent, 8)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rootParams.setMargins(
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 5)
        );

        root.setLayoutParams(rootParams);

        FrameLayoutCompat posterContainer =
                new FrameLayoutCompat(
                        parent
                );

        root.addView(
                posterContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 210)
                )
        );

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

        posterContainer.addView(
                poster,
                new android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView sourceTag =
                new TextView(
                        parent.getContext()
                );

        sourceTag.setTextColor(
                Color.WHITE
        );

        sourceTag.setTextSize(9);

        sourceTag.setGravity(
                Gravity.CENTER
        );

        sourceTag.setPadding(
                dp(parent, 6),
                dp(parent, 3),
                dp(parent, 6),
                dp(parent, 3)
        );

        sourceTag.setBackgroundColor(
                Color.rgb(30, 120, 165)
        );

        android.widget.FrameLayout.LayoutParams tagParams =
                new android.widget.FrameLayout.LayoutParams(
                        dp(parent, 64),
                        dp(parent, 24),
                        Gravity.TOP | Gravity.LEFT
                );

        tagParams.setMargins(
                dp(parent, 6),
                dp(parent, 6),
                0,
                0
        );

        posterContainer.addView(
                sourceTag,
                tagParams
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextColor(
                Color.rgb(245, 247, 250)
        );

        title.setTextSize(14);

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setMaxLines(2);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.setMargins(
                dp(parent, 3),
                dp(parent, 8),
                dp(parent, 3),
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

        meta.setTextColor(
                Color.rgb(130, 140, 155)
        );

        meta.setTextSize(10);

        meta.setMaxLines(1);

        meta.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams metaParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        metaParams.setMargins(
                dp(parent, 3),
                dp(parent, 4),
                dp(parent, 3),
                0
        );

        root.addView(
                meta,
                metaParams
        );

        TextView remark =
                new TextView(
                        parent.getContext()
                );

        remark.setTextColor(
                Color.rgb(80, 190, 245)
        );

        remark.setTextSize(9);

        remark.setMaxLines(1);

        remark.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams remarkParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        remarkParams.setMargins(
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3),
                0
        );

        root.addView(
                remark,
                remarkParams
        );

        return new VideoGridViewHolder(
                root,
                poster,
                sourceTag,
                title,
                meta,
                remark
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoGridViewHolder holder,
            int position
    ) {

        Video video =
                data.get(position);

        String name =
                safe(video.getName());

        if (name.isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        String source =
                safe(video.getSourceName());

        if (source.isEmpty()) {

            holder.sourceTag.setText(
                    "未知来源"
            );

        } else {

            holder.sourceTag.setText(
                    source
            );
        }

        StringBuilder meta =
                new StringBuilder();

        String year =
                safe(video.getYear());

        String area =
                safe(video.getArea());

        String category =
                safe(video.getCategory());

        if (!year.isEmpty()) {
            meta.append(year);
        }

        if (!area.isEmpty()) {

            if (meta.length() > 0) {
                meta.append(" · ");
            }

            meta.append(area);
        }

        if (!category.isEmpty()) {

            if (meta.length() > 0) {
                meta.append(" · ");
            }

            meta.append(category);
        }

        if (meta.length() == 0) {
            meta.append("视频");
        }

        holder.meta.setText(
                meta.toString()
        );

        String remark =
                safe(video.getRemarks());

        if (remark.isEmpty()) {

            holder.remark.setVisibility(
                    View.GONE
            );

        } else {

            holder.remark.setVisibility(
                    View.VISIBLE
            );

            holder.remark.setText(
                    remark
            );
        }

        String poster =
                safe(video.getPoster());

        holder.poster.setImageDrawable(
                null
        );

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

                    v.setScaleX(
                            hasFocus ? 1.015f : 1.0f
                    );

                    v.setScaleY(
                            hasFocus ? 1.015f : 1.0f
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

                        clickListener.onVideoClick(
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
                                .onVideoLongClick(
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

    /**
     * 简单 FrameLayout 封装，
     * 用于避免额外 XML 布局文件。
     */
    private static class FrameLayoutCompat
            extends android.widget.FrameLayout {

        FrameLayoutCompat(
                ViewGroup parent
        ) {

            super(parent.getContext());

            setClipChildren(false);
            setClipToPadding(false);
        }
    }

    static class VideoGridViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView sourceTag;
        final TextView title;
        final TextView meta;
        final TextView remark;

        VideoGridViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView sourceTag,
                TextView title,
                TextView meta,
                TextView remark
        ) {

            super(itemView);

            this.poster = poster;
            this.sourceTag = sourceTag;
            this.title = title;
            this.meta = meta;
            this.remark = remark;
        }
    }
}
