package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
 * 本地视频卡片适配器。
 *
 * 用于本地视频页面。
 *
 * 本地视频不上传、不复制到应用缓存，
 * 直接使用 SAF 文件 Uri 播放。
 */
public class LocalVideoCardAdapter
        extends RecyclerView.Adapter<LocalVideoCardAdapter.ViewHolder> {

    public interface OnLocalVideoClickListener {
        void onLocalVideoClick(
                Video video,
                int position
        );
    }

    private final Context context;
    private final List<Video> videos =
            new ArrayList<>();

    private OnLocalVideoClickListener listener;

    public LocalVideoCardAdapter(Context context) {
        this.context = context;
    }

    public LocalVideoCardAdapter(
            Context context,
            List<Video> data
    ) {
        this.context = context;

        if (data != null) {
            videos.addAll(data);
        }
    }

    public void setOnLocalVideoClickListener(
            OnLocalVideoClickListener listener
    ) {
        this.listener = listener;
    }

    public void setVideos(List<Video> data) {
        videos.clear();

        if (data != null) {
            videos.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addVideos(List<Video> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = videos.size();

        videos.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void remove(int position) {
        if (position < 0 ||
                position >= videos.size()) {
            return;
        }

        videos.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        videos.clear();
        notifyDataSetChanged();
    }

    public Video getItem(int position) {
        if (position < 0 ||
                position >= videos.size()) {
            return null;
        }

        return videos.get(position);
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
                LinearLayout.HORIZONTAL
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
                        dp(116)
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        ImageView poster =
                new ImageView(context);

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        root.addView(
                poster,
                new LinearLayout.LayoutParams(
                        dp(82),
                        dp(106)
                )
        );

        LinearLayout content =
                new LinearLayout(context);

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

        contentParams.leftMargin = dp(13);

        root.addView(
                content,
                contentParams
        );

        TextView title =
                new TextView(context);

        title.setTextSize(15);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setMaxLines(2);
        title.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        content.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView fileInfo =
                new TextView(context);

        fileInfo.setTextSize(11);
        fileInfo.setAlpha(0.68f);
        fileInfo.setSingleLine(true);
        fileInfo.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams fileInfoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        fileInfoParams.topMargin = dp(7);

        content.addView(
                fileInfo,
                fileInfoParams
        );

        TextView progress =
                new TextView(context);

        progress.setTextSize(10);
        progress.setAlpha(0.58f);
        progress.setSingleLine(true);
        progress.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.topMargin = dp(5);

        content.addView(
                progress,
                progressParams
        );

        return new ViewHolder(
                root,
                poster,
                title,
                fileInfo,
                progress
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Video video =
                videos.get(position);

        String name =
                video.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "本地视频";
        }

        holder.title.setText(name);

        String poster =
                video.getPoster();

        if (poster != null &&
                !poster.trim().isEmpty()) {

            ImageLoader.load(
                    context,
                    poster,
                    holder.poster
            );

        } else {

            holder.poster.setImageDrawable(null);
        }

        StringBuilder fileInfo =
                new StringBuilder();

        if (video.getArea() != null &&
                !video.getArea().trim().isEmpty()) {

            fileInfo.append(
                    video.getArea().trim()
            );
        }

        if (video.getYear() != null &&
                !video.getYear().trim().isEmpty()) {

            if (fileInfo.length() > 0) {
                fileInfo.append(" · ");
            }

            fileInfo.append(
                    video.getYear().trim()
            );
        }

        if (fileInfo.length() == 0) {
            fileInfo.append("本地文件");
        }

        holder.fileInfo.setText(
                fileInfo.toString()
        );

        StringBuilder progressText =
                new StringBuilder();

        if (video.getEpisodes() != null &&
                !video.getEpisodes().isEmpty()) {

            com.threew.tv.model.Episode episode =
                    video.getEpisodes().get(0);

            long size =
                    episode.getFileSizeBytes();

            long duration =
                    episode.getDurationMs();

            if (size > 0) {
                progressText.append(
                        FormatUtils.formatFileSize(size)
                );
            }

            if (duration > 0) {

                if (progressText.length() > 0) {
                    progressText.append(" · ");
                }

                progressText.append(
                        FormatUtils.formatDuration(
                                duration
                        )
                );
            }

            if (episode.isWatched()) {

                if (progressText.length() > 0) {
                    progressText.append(" · ");
                }

                progressText.append("已看");
            }
        }

        if (progressText.length() == 0) {
            progressText.append("本地视频");
        }

        holder.progress.setText(
                progressText.toString()
        );

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onLocalVideoClick(
                        videos.get(adapterPosition),
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
        return videos.size();
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

        final ImageView poster;
        final TextView title;
        final TextView fileInfo;
        final TextView progress;

        public ViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView fileInfo,
                TextView progress
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.fileInfo = fileInfo;
            this.progress = progress;
        }
    }
        }
