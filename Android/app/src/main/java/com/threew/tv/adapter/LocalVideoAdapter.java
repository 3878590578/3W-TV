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
import com.threew.tv.utils.FormatUtils;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地视频列表适配器。
 *
 * 本地视频特点：
 * - 不上传
 * - 不复制到应用缓存
 * - 直接使用原始 SAF Uri 播放
 *
 * 显示：
 * - 文件/视频名称
 * - 文件大小
 * - 视频时长
 * - 所在文件夹
 */
public class LocalVideoAdapter
        extends RecyclerView.Adapter<LocalVideoAdapter.LocalVideoViewHolder> {

    public interface OnLocalVideoClickListener {
        void onLocalVideoClick(
                Video video,
                int position
        );
    }

    public interface OnLocalVideoLongClickListener {
        boolean onLocalVideoLongClick(
                Video video,
                int position
        );
    }

    private final List<Video> data =
            new ArrayList<>();

    private final OnLocalVideoClickListener clickListener;
    private final OnLocalVideoLongClickListener longClickListener;

    public LocalVideoAdapter(
            OnLocalVideoClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public LocalVideoAdapter(
            OnLocalVideoClickListener clickListener,
            OnLocalVideoLongClickListener longClickListener
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
    public LocalVideoViewHolder onCreateViewHolder(
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

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 96)
                );

        rootParams.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
        );

        root.setLayoutParams(rootParams);

        ImageView icon =
                new ImageView(
                        parent.getContext()
                );

        icon.setScaleType(
                ImageView.ScaleType.CENTER
        );

        icon.setBackgroundColor(
                Color.rgb(31, 38, 48)
        );

        icon.setImageResource(
                android.R.drawable.ic_media_play
        );

        icon.setColorFilter(
                Color.rgb(65, 190, 255)
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 66),
                        dp(parent, 78)
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

        title.setTextSize(14);

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setMaxLines(2);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        info.addView(title);

        TextView detail =
                new TextView(
                        parent.getContext()
                );

        detail.setTextColor(
                Color.rgb(135, 145, 160)
        );

        detail.setTextSize(10);

        detail.setMaxLines(2);

        detail.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams detailParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        detailParams.setMargins(
                0,
                dp(parent, 5),
                0,
                0
        );

        info.addView(
                detail,
                detailParams
        );

        TextView localTag =
                new TextView(
                        parent.getContext()
                );

        localTag.setText(
                "本地"
        );

        localTag.setTextColor(
                Color.rgb(80, 205, 155)
        );

        localTag.setTextSize(10);

        localTag.setGravity(
                Gravity.CENTER
        );

        localTag.setPadding(
                dp(parent, 8),
                dp(parent, 4),
                dp(parent, 8),
                dp(parent, 4)
        );

        localTag.setBackgroundColor(
                Color.rgb(24, 61, 49)
        );

        root.addView(
                localTag,
                new LinearLayout.LayoutParams(
                        dp(parent, 48),
                        dp(parent, 30)
                )
        );

        return new LocalVideoViewHolder(
                root,
                icon,
                title,
                detail,
                localTag
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull LocalVideoViewHolder holder,
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

        StringBuilder detail =
                new StringBuilder();

        String area =
                safe(video.getArea());

        String year =
                safe(video.getYear());

        String category =
                safe(video.getCategory());

        if (!area.isEmpty()) {
            detail.append(area);
        }

        if (!year.isEmpty()) {

            if (detail.length() > 0) {
                detail.append(" · ");
            }

            detail.append(year);
        }

        if (!category.isEmpty()) {

            if (detail.length() > 0) {
                detail.append(" · ");
            }

            detail.append(category);
        }

        if (video.getEpisodes() != null &&
                !video.getEpisodes().isEmpty()) {

            if (detail.length() > 0) {
                detail.append(" · ");
            }

            detail.append(
                    video.getEpisodes().size()
            );

            detail.append(" 个文件");
        }

        holder.detail.setText(
                detail.length() == 0
                        ? "本地视频"
                        : detail.toString()
        );

        /*
         * 本地视频的海报通常不存在。
         * 如果扫描器以后从文件夹读取到了缩略图，
         * 这里可以直接复用 ImageLoader。
         */
        String poster =
                safe(video.getPoster());

        if (!poster.isEmpty()) {

            holder.icon.clearColorFilter();

            ImageLoader.load(
                    holder.itemView.getContext(),
                    poster,
                    holder.icon
            );

        } else {

            holder.icon.setImageResource(
                    android.R.drawable.ic_media_play
            );

            holder.icon.setColorFilter(
                    Color.rgb(65, 190, 255)
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

                        clickListener.onLocalVideoClick(
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
                                .onLocalVideoLongClick(
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

    static class LocalVideoViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView title;
        final TextView detail;
        final TextView localTag;

        LocalVideoViewHolder(
                @NonNull View itemView,
                ImageView icon,
                TextView title,
                TextView detail,
                TextView localTag
        ) {

            super(itemView);

            this.icon = icon;
            this.title = title;
            this.detail = detail;
            this.localTag = localTag;
        }
    }
        }
