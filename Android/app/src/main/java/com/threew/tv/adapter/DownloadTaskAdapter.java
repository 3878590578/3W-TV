package com.threew.tv.adapter;

import android.graphics.Color;
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
 * 下载任务适配器。
 *
 * 功能：
 * - 显示下载任务
 * - 显示剧集名称
 * - 显示下载进度
 * - 显示已下载 / 总大小
 * - 显示下载状态
 * - 显示错误信息
 * - 支持 Android TV 遥控器
 */
public class DownloadTaskAdapter
        extends RecyclerView.Adapter<DownloadTaskAdapter.DownloadTaskViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(
                DownloadItem item,
                int position
        );
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(
                DownloadItem item,
                int position
        );
    }

    private final List<DownloadItem> data =
            new ArrayList<>();

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public DownloadTaskAdapter(
            OnItemClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public DownloadTaskAdapter(
            OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener
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

    public void add(
            DownloadItem item
    ) {

        if (item == null) {
            return;
        }

        data.add(item);

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

    public void update(
            int position,
            DownloadItem item
    ) {

        if (position < 0 ||
                position >= data.size() ||
                item == null) {

            return;
        }

        data.set(
                position,
                item
        );

        notifyItemChanged(position);
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
    public DownloadTaskViewHolder onCreateViewHolder(
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
                dp(parent, 10),
                dp(parent, 14),
                dp(parent, 10)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 116)
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
                        dp(parent, 27)
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(14);

        title.setTextColor(
                Color.rgb(238, 242, 247)
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

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextSize(10);

        status.setGravity(
                Gravity.CENTER
        );

        titleRow.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(parent, 76),
                        dp(parent, 24)
                )
        );

        TextView episode =
                new TextView(
                        parent.getContext()
                );

        episode.setTextSize(10);

        episode.setTextColor(
                Color.rgb(135, 145, 158)
        );

        episode.setGravity(
                Gravity.CENTER_VERTICAL
        );

        episode.setMaxLines(1);

        episode.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                episode,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 22)
                )
        );

        ProgressBar progress =
                new ProgressBar(
                        parent.getContext(),
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progress.setMax(100);

        progress.setProgress(0);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 5)
                );

        progressParams.topMargin =
                dp(parent, 5);

        root.addView(
                progress,
                progressParams
        );

        LinearLayout bottomRow =
                new LinearLayout(
                        parent.getContext()
                );

        bottomRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottomRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams bottomParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 24)
                );

        bottomParams.topMargin =
                dp(parent, 3);

        root.addView(
                bottomRow,
                bottomParams
        );

        TextView size =
                createInfoText(parent);

        bottomRow.addView(
                size,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView speed =
                createInfoText(parent);

        speed.setGravity(
                Gravity.CENTER
        );

        bottomRow.addView(
                speed,
                new LinearLayout.LayoutParams(
                        dp(parent, 100),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView error =
                createInfoText(parent);

        error.setTextColor(
                Color.rgb(205, 105, 105)
        );

        error.setGravity(
                Gravity.CENTER_VERTICAL
        );

        error.setMaxLines(1);

        error.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                error,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        return new DownloadTaskViewHolder(
                root,
                title,
                status,
                episode,
                progress,
                size,
                speed,
                error
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull DownloadTaskViewHolder holder,
            int position
    ) {

        DownloadItem item =
                data.get(position);

        String videoName =
                safe(item.getVideoName());

        String episodeName =
                safe(item.getEpisodeName());

        if (videoName.isEmpty()) {
            videoName = "下载任务";
        }

        holder.title.setText(
                videoName
        );

        if (episodeName.isEmpty()) {

            String number =
                    safe(item.getEpisodeNumber());

            if (!number.isEmpty()) {

                episodeName =
                        "第 " + number + " 集";
            } else {

                episodeName =
                        "未知剧集";
            }
        }

        holder.episode.setText(
                episodeName
        );

        int progress =
                Math.max(
                        0,
                        Math.min(
                                100,
                                item.getProgress()
                        )
                );

        holder.progress.setProgress(
                progress
        );

        String downloaded =
                FormatUtils.formatFileSize(
                        Math.max(
                                0L,
                                item.getDownloadedBytes()
                        )
                );

        String total =
                FormatUtils.formatFileSize(
                        Math.max(
                                0L,
                                item.getTotalBytes()
                        )
                );

        if (item.getTotalBytes() > 0) {

            holder.size.setText(
                    downloaded +
                            " / " +
                            total +
                            "  " +
                            progress +
                            "%"
            );

        } else {

            holder.size.setText(
                    downloaded +
                            "  " +
                            progress +
                            "%"
            );
        }

        holder.speed.setText(
                buildSpeedText(item)
        );

        String error =
                safe(item.getErrorMessage());

        holder.error.setText(
                error
        );

        holder.status.setText(
                getStatusText(item)
        );

        holder.status.setTextColor(
                getStatusColor(item)
        );

        holder.error.setVisibility(
                error.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );

        holder.itemView.setBackgroundColor(
                Color.rgb(20, 23, 30)
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                Color.rgb(32, 45, 56)
                        );

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackgroundColor(
                                Color.rgb(20, 23, 30)
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

    private String buildSpeedText(
            DownloadItem item
    ) {

        if (item.isDownloading()) {
            return "下载中";
        }

        if (item.isCompleted()) {
            return "已完成";
        }

        if (item.isPaused()) {
            return "已暂停";
        }

        if (item.isWaiting()) {
            return "等待中";
        }

        if (item.isFailed()) {
            return "下载失败";
        }

        if (item.isDeleted()) {
            return "已删除";
        }

        return "";
    }

    private String getStatusText(
            DownloadItem item
    ) {

        if (item.isDownloading()) {
            return "下载中";
        }

        if (item.isCompleted()) {
            return "完成";
        }

        if (item.isPaused()) {
            return "暂停";
        }

        if (item.isWaiting()) {
            return "等待";
        }

        if (item.isFailed()) {
            return "失败";
        }

        if (item.isDeleted()) {
            return "已删除";
        }

        return "未知";
    }

    private int getStatusColor(
            DownloadItem item
    ) {

        if (item.isDownloading()) {

            return Color.rgb(
                    90,
                    210,
                    245
            );
        }

        if (item.isCompleted()) {

            return Color.rgb(
                    100,
                    205,
                    135
            );
        }

        if (item.isPaused()) {

            return Color.rgb(
                    205,
                    175,
                    95
            );
        }

        if (item.isFailed()) {

            return Color.rgb(
                    215,
                    105,
                    105
            );
        }

        if (item.isDeleted()) {

            return Color.rgb(
                    120,
                    125,
                    132
            );
        }

        return Color.rgb(
                145,
                155,
                168
        );
    }

    private TextView createInfoText(
            ViewGroup parent
    ) {

        TextView text =
                new TextView(
                        parent.getContext()
                );

        text.setTextSize(9);

        text.setTextColor(
                Color.rgb(125, 135, 148)
        );

        text.setGravity(
                Gravity.CENTER_VERTICAL
        );

        text.setMaxLines(1);

        text.setEllipsize(
                TextUtils.TruncateAt.END
        );

        return text;
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
        return data.size();
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

    static class DownloadTaskViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView status;
        final TextView episode;
        final ProgressBar progress;
        final TextView size;
        final TextView speed;
        final TextView error;

        DownloadTaskViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView status,
                TextView episode,
                ProgressBar progress,
                TextView size,
                TextView speed,
                TextView error
        ) {

            super(itemView);

            this.title = title;
            this.status = status;
            this.episode = episode;
            this.progress = progress;
            this.size = size;
            this.speed = speed;
            this.error = error;
        }
    }
}
