package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.DownloadItem;
import com.threew.tv.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载列表适配器。
 *
 * 支持：
 * - 等待
 * - 下载中
 * - 暂停
 * - 已完成
 * - 失败
 * - 删除
 *
 * 点击：进入下载项操作
 * 长按：显示更多操作
 */
public class DownloadAdapter
        extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    public interface OnDownloadClickListener {
        void onDownloadClick(
                DownloadItem item,
                int position
        );
    }

    public interface OnDownloadLongClickListener {
        boolean onDownloadLongClick(
                DownloadItem item,
                int position
        );
    }

    private final List<DownloadItem> data =
            new ArrayList<>();

    private final OnDownloadClickListener clickListener;
    private final OnDownloadLongClickListener longClickListener;

    public DownloadAdapter(
            OnDownloadClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public DownloadAdapter(
            OnDownloadClickListener clickListener,
            OnDownloadLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<DownloadItem> items
    ) {

        data.clear();

        if (items != null) {
            data.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<DownloadItem> items
    ) {

        if (items == null ||
                items.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(items);

        notifyItemRangeInserted(
                start,
                items.size()
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

    public void updateItem(
            DownloadItem item
    ) {

        if (item == null) {
            return;
        }

        long id = item.getId();

        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getId() == id) {

                data.set(i, item);

                notifyItemChanged(i);

                return;
            }
        }
    }

    public DownloadItem getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public List<DownloadItem> getItems() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(
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
                dp(parent, 14),
                dp(parent, 12),
                dp(parent, 14),
                dp(parent, 12)
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
                0,
                0,
                0,
                dp(parent, 7)
        );

        root.setLayoutParams(rootParams);

        LinearLayout top =
                new LinearLayout(
                        parent.getContext()
                );

        top.setOrientation(
                LinearLayout.HORIZONTAL
        );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                top,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout titleBox =
                new LinearLayout(
                        parent.getContext()
                );

        titleBox.setOrientation(
                LinearLayout.VERTICAL
        );

        top.addView(
                titleBox,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
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

        titleBox.addView(title);

        TextView episode =
                new TextView(
                        parent.getContext()
                );

        episode.setTextColor(
                Color.rgb(135, 145, 160)
        );

        episode.setTextSize(11);

        episode.setMaxLines(1);

        episode.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams episodeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        episodeParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        titleBox.addView(
                episode,
                episodeParams
        );

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextSize(11);

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                dp(parent, 9),
                dp(parent, 4),
                dp(parent, 9),
                dp(parent, 4)
        );

        top.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(parent, 76),
                        dp(parent, 32)
                )
        );

        ProgressBar progress =
                new ProgressBar(
                        parent.getContext(),
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progress.setMax(100);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 5)
                );

        progressParams.setMargins(
                0,
                dp(parent, 12),
                0,
                0
        );

        root.addView(
                progress,
                progressParams
        );

        LinearLayout bottom =
                new LinearLayout(
                        parent.getContext()
                );

        bottom.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottom.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams bottomParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        bottomParams.setMargins(
                0,
                dp(parent, 7),
                0,
                0
        );

        root.addView(
                bottom,
                bottomParams
        );

        TextView size =
                new TextView(
                        parent.getContext()
                );

        size.setTextColor(
                Color.rgb(125, 135, 150)
        );

        size.setTextSize(10);

        bottom.addView(
                size,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView speed =
                new TextView(
                        parent.getContext()
                );

        speed.setTextColor(
                Color.rgb(75, 190, 255)
        );

        speed.setTextSize(10);

        speed.setGravity(
                Gravity.RIGHT
        );

        bottom.addView(
                speed,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView error =
                new TextView(
                        parent.getContext()
                );

        error.setTextColor(
                Color.rgb(255, 100, 100)
        );

        error.setTextSize(10);

        error.setMaxLines(2);

        error.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams errorParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        errorParams.setMargins(
                0,
                dp(parent, 5),
                0,
                0
        );

        root.addView(
                error,
                errorParams
        );

        return new DownloadViewHolder(
                root,
                title,
                episode,
                status,
                progress,
                size,
                speed,
                error
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull DownloadViewHolder holder,
            int position
    ) {

        DownloadItem item =
                data.get(position);

        String videoName =
                safe(item.getVideoName());

        if (videoName.isEmpty()) {
            videoName = "未命名视频";
        }

        holder.title.setText(videoName);

        String episodeName =
                safe(item.getEpisodeName());

        int episodeNumber =
                item.getEpisodeNumber();

        if (episodeName.isEmpty() &&
                episodeNumber > 0) {

            episodeName =
                    "第 " +
                    episodeNumber +
                    " 集";

        } else if (episodeName.isEmpty()) {

            episodeName = "单集";

        } else if (episodeNumber > 0) {

            episodeName =
                    "第 " +
                    episodeNumber +
                    " 集 · " +
                    episodeName;
        }

        holder.episode.setText(
                episodeName
        );

        int progress =
                clamp(
                        item.getProgress(),
                        0,
                        100
                );

        holder.progress.setProgress(
                progress
        );

        holder.status.setText(
                getStatusText(item)
        );

        applyStatusStyle(
                holder.status,
                item
        );

        long downloaded =
                item.getDownloadedBytes();

        long total =
                item.getTotalBytes();

        String sizeText;

        if (total > 0) {

            sizeText =
                    FormatUtils.formatFileSize(
                            downloaded
                    ) +
                    " / " +
                    FormatUtils.formatFileSize(
                            total
                    );

        } else if (downloaded > 0) {

            sizeText =
                    FormatUtils.formatFileSize(
                            downloaded
                    );

        } else {

            sizeText = "等待数据";
        }

        holder.size.setText(
                sizeText
        );

        holder.speed.setText(
                getSpeedText(item)
        );

        String error =
                safe(item.getErrorMessage());

        if (item.isFailed() &&
                !error.isEmpty()) {

            holder.error.setVisibility(
                    View.VISIBLE
            );

            holder.error.setText(
                    error
            );

        } else {

            holder.error.setVisibility(
                    View.GONE
            );

            holder.error.setText("");
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

                        clickListener.onDownloadClick(
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
                                .onDownloadLongClick(
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private String getStatusText(
            DownloadItem item
    ) {

        if (item.isCompleted()) {
            return "已完成";
        }

        if (item.isDownloading()) {
            return "下载中";
        }

        if (item.isPaused()) {
            return "已暂停";
        }

        if (item.isWaiting()) {
            return "等待中";
        }

        if (item.isFailed()) {
            return "失败";
        }

        if (item.isDeleted()) {
            return "已删除";
        }

        return "未知";
    }

    private void applyStatusStyle(
            TextView view,
            DownloadItem item
    ) {

        if (item.isCompleted()) {

            view.setTextColor(
                    Color.rgb(90, 220, 150)
            );

            view.setBackgroundColor(
                    Color.rgb(25, 65, 48)
            );

        } else if (item.isDownloading()) {

            view.setTextColor(
                    Color.rgb(70, 190, 255)
            );

            view.setBackgroundColor(
                    Color.rgb(25, 55, 75)
            );

        } else if (item.isFailed()) {

            view.setTextColor(
                    Color.rgb(255, 100, 100)
            );

            view.setBackgroundColor(
                    Color.rgb(75, 30, 35)
            );

        } else if (item.isPaused()) {

            view.setTextColor(
                    Color.rgb(255, 190, 70)
            );

            view.setBackgroundColor(
                    Color.rgb(70, 55, 25)
            );

        } else {

            view.setTextColor(
                    Color.rgb(155, 165, 180)
            );

            view.setBackgroundColor(
                    Color.rgb(42, 45, 54)
            );
        }
    }

    private String getSpeedText(
            DownloadItem item
    ) {

        if (!item.isDownloading()) {
            return "";
        }

        /*
         * DownloadItem 当前只保存下载量，
         * 不强制依赖额外 speed 字段。
         * 后续 DownloadManager 如果提供实时速度，
         * 可以直接扩展这里。
         */
        return "下载中 · " +
                item.getProgress() +
                "%";
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private int clamp(
            int value,
            int min,
            int max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
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

    static class DownloadViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView episode;
        final TextView status;
        final ProgressBar progress;
        final TextView size;
        final TextView speed;
        final TextView error;

        DownloadViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView episode,
                TextView status,
                ProgressBar progress,
                TextView size,
                TextView speed,
                TextView error
        ) {

            super(itemView);

            this.title = title;
            this.episode = episode;
            this.status = status;
            this.progress = progress;
            this.size = size;
            this.speed = speed;
            this.error = error;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
