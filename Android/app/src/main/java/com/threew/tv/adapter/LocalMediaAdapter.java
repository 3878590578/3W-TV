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
import com.threew.tv.model.Episode;
import com.threew.tv.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地媒体列表适配器。
 *
 * 用于：
 * - 本地视频列表
 * - 本地文件夹扫描结果
 * - Android 手机 / 平板 / TV
 *
 * 本适配器不复制、不移动本地文件，
 * 只显示扫描得到的媒体信息。
 */
public class LocalMediaAdapter
        extends RecyclerView.Adapter<LocalMediaAdapter.LocalMediaViewHolder> {

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

    public LocalMediaAdapter(
            OnItemClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public LocalMediaAdapter(
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
    public LocalMediaViewHolder onCreateViewHolder(
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
                dp(parent, 9),
                dp(parent, 14),
                dp(parent, 9)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 86)
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(rootParams);

        LinearLayout topRow =
                new LinearLayout(
                        parent.getContext()
                );

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                topRow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 28)
                )
        );

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setText("▶");

        icon.setTextSize(13);

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(
                        100,
                        205,
                        235
                )
        );

        topRow.addView(
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

        title.setTextSize(14);

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

        title.setMaxLines(1);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        topRow.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView type =
                new TextView(
                        parent.getContext()
                );

        type.setTextSize(9);

        type.setTextColor(
                Color.rgb(
                        120,
                        210,
                        235
                )
        );

        type.setGravity(
                Gravity.CENTER
        );

        topRow.addView(
                type,
                new LinearLayout.LayoutParams(
                        dp(parent, 54),
                        dp(parent, 22)
                )
        );

        TextView info =
                new TextView(
                        parent.getContext()
                );

        info.setTextSize(10);

        info.setTextColor(
                Color.rgb(
                        135,
                        145,
                        158
                )
        );

        info.setGravity(
                Gravity.CENTER_VERTICAL
        );

        info.setMaxLines(1);

        info.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                info,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                )
        );

        TextView path =
                new TextView(
                        parent.getContext()
                );

        path.setTextSize(8);

        path.setTextColor(
                Color.rgb(
                        90,
                        100,
                        112
                )
        );

        path.setGravity(
                Gravity.CENTER_VERTICAL
        );

        path.setMaxLines(1);

        path.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        root.addView(
                path,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        return new LocalMediaViewHolder(
                root,
                title,
                type,
                info,
                path
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull LocalMediaViewHolder holder,
            int position
    ) {

        Video video =
                data.get(position);

        String title =
                safe(video.getName());

        if (title.isEmpty()) {
            title = "本地视频";
        }

        holder.title.setText(title);

        holder.type.setText(
                "本地"
        );

        String info =
                buildInfo(video);

        holder.info.setText(info);

        holder.path.setText(
                buildPath(video)
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

    private String buildInfo(
            Video video
    ) {

        List<Episode> episodes =
                video.getEpisodes();

        if (episodes == null ||
                episodes.isEmpty()) {

            return "本地媒体";
        }

        Episode episode =
                episodes.get(0);

        StringBuilder builder =
                new StringBuilder();

        builder.append("本地文件");

        long size =
                episode.getFileSizeBytes();

        if (size > 0) {

            builder.append("  ·  ")
                    .append(
                            FormatUtils.formatFileSize(size)
                    );
        }

        long duration =
                episode.getDurationMs();

        if (duration > 0) {

            builder.append("  ·  ")
                    .append(
                            FormatUtils.formatDuration(duration)
                    );
        }

        return builder.toString();
    }

    private String buildPath(
            Video video
    ) {

        List<Episode> episodes =
                video.getEpisodes();

        if (episodes == null ||
                episodes.isEmpty()) {

            return "本地文件";
        }

        Episode episode =
                episodes.get(0);

        String url =
                safe(episode.getPlayUrl());

        if (url.isEmpty()) {
            return "本地文件";
        }

        return url;
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

    static class LocalMediaViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView type;
        final TextView info;
        final TextView path;

        LocalMediaViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView type,
                TextView info,
                TextView path
        ) {

            super(itemView);

            this.title = title;
            this.type = type;
            this.info = info;
            this.path = path;
        }
    }
}
