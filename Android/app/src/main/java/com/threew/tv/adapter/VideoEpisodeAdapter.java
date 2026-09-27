package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;
import com.threew.tv.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频剧集组合适配器。
 *
 * 用于视频详情页：
 * 标题/来源信息下方的剧集列表。
 *
 * 支持：
 * - 集数
 * - 剧集名称
 * - 当前播放集
 * - 已观看
 * - 已下载
 * - 观看进度
 * - TV 遥控器焦点
 */
public class VideoEpisodeAdapter
        extends RecyclerView.Adapter<VideoEpisodeAdapter.ViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(
                Video video,
                Episode episode,
                int position
        );
    }

    private final Context context;
    private final List<Episode> episodes = new ArrayList<>();

    private Video video;
    private OnEpisodeClickListener listener;

    private int selectedPosition = -1;

    public VideoEpisodeAdapter(Context context) {
        this.context = context;
    }

    public VideoEpisodeAdapter(
            Context context,
            Video video
    ) {
        this.context = context;
        setVideo(video);
    }

    public void setOnEpisodeClickListener(
            OnEpisodeClickListener listener
    ) {
        this.listener = listener;
    }

    public void setVideo(Video video) {
        this.video = video;

        episodes.clear();

        if (video != null &&
                video.getEpisodes() != null) {

            episodes.addAll(
                    video.getEpisodes()
            );
        }

        selectedPosition = -1;

        notifyDataSetChanged();
    }

    public void setEpisodes(List<Episode> data) {
        episodes.clear();

        if (data != null) {
            episodes.addAll(data);
        }

        selectedPosition = -1;

        notifyDataSetChanged();
    }

    public void setSelectedEpisode(int position) {
        if (position < 0 ||
                position >= episodes.size()) {
            return;
        }

        int oldPosition = selectedPosition;

        selectedPosition = position;

        if (oldPosition >= 0 &&
                oldPosition != selectedPosition) {

            notifyItemChanged(oldPosition);
        }

        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Episode getEpisode(int position) {
        if (position < 0 ||
                position >= episodes.size()) {
            return null;
        }

        return episodes.get(position);
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

        int margin = dp(4);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        TextView number =
                new TextView(context);

        number.setGravity(
                Gravity.CENTER
        );

        number.setTextSize(14);

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        dp(65),
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        root.addView(
                number,
                numberParams
        );

        LinearLayout middle =
                new LinearLayout(context);

        middle.setOrientation(
                LinearLayout.VERTICAL
        );

        middle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams middleParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );

        root.addView(
                middle,
                middleParams
        );

        TextView name =
                new TextView(context);

        name.setTextSize(14);
        name.setSingleLine(true);
        name.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        middle.addView(
                name,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView progress =
                new TextView(context);

        progress.setTextSize(10);
        progress.setAlpha(0.62f);
        progress.setSingleLine(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.topMargin = dp(2);

        middle.addView(
                progress,
                progressParams
        );

        TextView status =
                new TextView(context);

        status.setTextSize(10);
        status.setGravity(
                Gravity.CENTER
        );
        status.setSingleLine(true);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        dp(72),
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        root.addView(
                status,
                statusParams
        );

        return new ViewHolder(
                root,
                number,
                name,
                progress,
                status
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Episode episode =
                episodes.get(position);

        String number =
                episode.getNumber();

        if (number == null ||
                number.trim().isEmpty()) {

            number =
                    String.valueOf(position + 1);
        }

        holder.number.setText(
                "第 " + number + " 集"
        );

        String name =
                episode.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name =
                    "第 " + number + " 集";
        }

        holder.name.setText(name);

        long current =
                episode.getPositionMs();

        long duration =
                episode.getDurationMs();

        if (current > 0) {

            if (duration > 0) {

                holder.progress.setText(
                        FormatUtils.formatDuration(current)
                                + " / "
                                + FormatUtils.formatDuration(duration)
                );

            } else {

                holder.progress.setText(
                        FormatUtils.formatDuration(current)
                );
            }

        } else if (duration > 0) {

            holder.progress.setText(
                    FormatUtils.formatDuration(duration)
            );

        } else {

            holder.progress.setText("");
        }

        StringBuilder status =
                new StringBuilder();

        if (episode.isWatched()) {
            status.append("已看");
        }

        if (episode.isDownloaded()) {

            if (status.length() > 0) {
                status.append(" · ");
            }

            status.append("已下载");
        }

        holder.status.setText(
                status.toString()
        );

        boolean selected =
                position == selectedPosition;

        if (selected) {

            holder.number.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            holder.name.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            holder.itemView.setScaleX(1.02f);
            holder.itemView.setScaleY(1.02f);

        } else {

            holder.number.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            );

            holder.name.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            );

            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            int oldPosition =
                    selectedPosition;

            selectedPosition =
                    adapterPosition;

            if (oldPosition >= 0 &&
                    oldPosition != selectedPosition) {

                notifyItemChanged(oldPosition);
            }

            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onEpisodeClick(
                        video,
                        episodes.get(adapterPosition),
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

                        holder.name.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );

                    } else {

                        float scale =
                                position == selectedPosition
                                        ? 1.02f
                                        : 1.0f;

                        v.animate()
                                .scaleX(scale)
                                .scaleY(scale)
                                .setDuration(120)
                                .start();

                        holder.name.setTypeface(
                                Typeface.DEFAULT,
                                position == selectedPosition
                                        ? Typeface.BOLD
                                        : Typeface.NORMAL
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return episodes.size();
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

        final TextView number;
        final TextView name;
        final TextView progress;
        final TextView status;

        public ViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView name,
                TextView progress,
                TextView status
        ) {
            super(itemView);

            this.number = number;
            this.name = name;
            this.progress = progress;
            this.status = status;
        }
    }
        }
