package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
 * 下载任务卡片适配器。
 *
 * 用于下载管理页面。
 *
 * 支持：
 * - 等待
 * - 下载中
 * - 暂停
 * - 已完成
 * - 失败
 * - 进度
 * - 已下载大小
 * - 总大小
 * - 错误信息
 */
public class DownloadCardAdapter
        extends RecyclerView.Adapter<DownloadCardAdapter.ViewHolder> {

    public interface OnDownloadClickListener {
        void onDownloadClick(
                DownloadItem item,
                int position
        );
    }

    private final Context context;
    private final List<DownloadItem> downloads =
            new ArrayList<>();

    private OnDownloadClickListener listener;

    public DownloadCardAdapter(Context context) {
        this.context = context;
    }

    public DownloadCardAdapter(
            Context context,
            List<DownloadItem> data
    ) {
        this.context = context;

        if (data != null) {
            downloads.addAll(data);
        }
    }

    public void setOnDownloadClickListener(
            OnDownloadClickListener listener
    ) {
        this.listener = listener;
    }

    public void setDownloads(
            List<DownloadItem> data
    ) {
        downloads.clear();

        if (data != null) {
            downloads.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addDownloads(
            List<DownloadItem> data
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = downloads.size();

        downloads.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void updateItem(
            DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        long id = item.getId();

        for (int i = 0; i < downloads.size(); i++) {

            if (downloads.get(i).getId() == id) {
                downloads.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void remove(int position) {
        if (position < 0 ||
                position >= downloads.size()) {
            return;
        }

        downloads.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        downloads.clear();
        notifyDataSetChanged();
    }

    public DownloadItem getItem(int position) {
        if (position < 0 ||
                position >= downloads.size()) {
            return null;
        }

        return downloads.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setFocusable(true);
        root.setClickable(true);

        int margin = dp(5);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(132)
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        root.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        LinearLayout titleRow =
                new LinearLayout(context);

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
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView title =
                new TextView(context);

        title.setTextSize(15);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setSingleLine(true);
        title.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        titleRow.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                )
        );

        TextView status =
                new TextView(context);

        status.setTextSize(11);
        status.setGravity(
                Gravity.CENTER
        );
        status.setAlpha(0.75f);

        titleRow.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(90),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        ProgressBar progressBar =
                new ProgressBar(
                        context,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progressBar.setMax(100);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(6)
                );

        progressParams.topMargin = dp(10);

        root.addView(
                progressBar,
                progressParams
        );

        TextView info =
                new TextView(context);

        info.setTextSize(11);
        info.setAlpha(0.68f);
        info.setSingleLine(true);
        info.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        infoParams.topMargin = dp(7);

        root.addView(
                info,
                infoParams
        );

        TextView error =
                new TextView(context);

        error.setTextSize(10);
        error.setAlpha(0.7f);
        error.setMaxLines(2);
        error.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams errorParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        errorParams.topMargin = dp(4);

        root.addView(
                error,
                errorParams
        );

        return new ViewHolder(
                root,
                title,
                status,
                progressBar,
                info,
                error
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        DownloadItem item =
                downloads.get(position);

        String videoName =
                item.getVideoName();

        if (videoName == null ||
                videoName.trim().isEmpty()) {

            videoName = "未命名视频";
        }

        String episodeName =
                item.getEpisodeName();

        StringBuilder title =
                new StringBuilder(videoName);

        if (episodeName != null &&
                !episodeName.trim().isEmpty()) {

            title.append(" · ");
            title.append(episodeName);

        } else if (item.getEpisodeNumber() != null &&
                !item.getEpisodeNumber().trim().isEmpty()) {

            title.append(" · 第 ");
            title.append(item.getEpisodeNumber());
            title.append(" 集");
        }

        holder.title.setText(
                title.toString()
        );

        String statusText;

        if (item.isWaiting()) {
            statusText = "等待中";
        } else if (item.isDownloading()) {
            statusText = "下载中";
        } else if (item.isPaused()) {
            statusText = "已暂停";
        } else if (item.isCompleted()) {
            statusText = "已完成";
        } else if (item.isFailed()) {
            statusText = "失败";
        } else if (item.isDeleted()) {
            statusText = "已删除";
        } else {
            statusText = "未知";
        }

        holder.status.setText(
                statusText
        );

        int progress =
                item.getProgress();

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }

        holder.progressBar.setProgress(
                progress
        );

        long downloaded =
                item.getDownloadedBytes();

        long total =
                item.getTotalBytes();

        StringBuilder info =
                new StringBuilder();

        info.append(progress);
        info.append("%");

        if (downloaded > 0) {
            info.append(" · ");
            info.append(
                    FormatUtils.formatFileSize(
                            downloaded
                    )
            );
        }

        if (total > 0) {
            info.append(" / ");
            info.append(
                    FormatUtils.formatFileSize(
                            total
                    )
            );
        }

        holder.info.setText(
                info.toString()
        );

        String errorMessage =
                item.getErrorMessage();

        if (errorMessage != null &&
                !errorMessage.trim().isEmpty()) {

            holder.error.setVisibility(
                    View.VISIBLE
            );

            holder.error.setText(
                    "错误：" + errorMessage
            );

        } else {

            holder.error.setVisibility(
                    View.GONE
            );

            holder.error.setText("");
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onDownloadClick(
                        downloads.get(adapterPosition),
                        adapterPosition
                );
            }
        });

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.045f)
                                .scaleY(1.045f)
                                .setDuration(120)
                                .start();

                        holder.title.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );

                    } else {

                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(120)
                                .start();

                        holder.title.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    private int dp(int value) {
        float density =
                context.getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView status;
        final ProgressBar progressBar;
        final TextView info;
        final TextView error;

        public ViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView status,
                ProgressBar progressBar,
                TextView info,
                TextView error
        ) {
            super(itemView);

            this.title = title;
            this.status = status;
            this.progressBar = progressBar;
            this.info = info;
            this.error = error;
        }
    }
        }
