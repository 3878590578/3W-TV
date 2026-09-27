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

import com.threew.tv.model.History;
import com.threew.tv.utils.FormatUtils;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史卡片适配器。
 *
 * 用于“我的 / 播放历史”页面。
 *
 * 显示：
 * - 视频海报
 * - 视频名称
 * - 集数
 * - 播放进度
 * - 播放速度
 * - 最近观看时间
 * - 播放完成状态
 */
public class HistoryCardAdapter
        extends RecyclerView.Adapter<HistoryCardAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(
                History history,
                int position
        );
    }

    private final Context context;
    private final List<History> histories =
            new ArrayList<>();

    private OnHistoryClickListener listener;

    public HistoryCardAdapter(Context context) {
        this.context = context;
    }

    public HistoryCardAdapter(
            Context context,
            List<History> data
    ) {
        this.context = context;

        if (data != null) {
            histories.addAll(data);
        }
    }

    public void setOnHistoryClickListener(
            OnHistoryClickListener listener
    ) {
        this.listener = listener;
    }

    public void setHistories(
            List<History> data
    ) {
        histories.clear();

        if (data != null) {
            histories.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addHistories(
            List<History> data
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = histories.size();

        histories.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void remove(int position) {
        if (position < 0 ||
                position >= histories.size()) {
            return;
        }

        histories.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        histories.clear();
        notifyDataSetChanged();
    }

    public History getItem(int position) {
        if (position < 0 ||
                position >= histories.size()) {
            return null;
        }

        return histories.get(position);
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
                        dp(118)
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

        TextView episode =
                new TextView(context);

        episode.setTextSize(12);
        episode.setAlpha(0.72f);
        episode.setSingleLine(true);
        episode.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams episodeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        episodeParams.topMargin = dp(5);

        content.addView(
                episode,
                episodeParams
        );

        TextView progress =
                new TextView(context);

        progress.setTextSize(11);
        progress.setAlpha(0.65f);
        progress.setSingleLine(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.topMargin = dp(4);

        content.addView(
                progress,
                progressParams
        );

        TextView time =
                new TextView(context);

        time.setTextSize(10);
        time.setAlpha(0.55f);
        time.setSingleLine(true);

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        timeParams.topMargin = dp(4);

        content.addView(
                time,
                timeParams
        );

        return new ViewHolder(
                root,
                poster,
                title,
                episode,
                progress,
                time
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        History history =
                histories.get(position);

        String title =
                history.getVideoName();

        if (title == null ||
                title.trim().isEmpty()) {
            title = "未命名视频";
        }

        holder.title.setText(title);

        String poster =
                history.getPoster();

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

        String episodeName =
                history.getEpisodeName();

        String episodeNumber =
                history.getEpisodeNumber();

        StringBuilder episodeText =
                new StringBuilder();

        if (episodeNumber != null &&
                !episodeNumber.trim().isEmpty()) {

            episodeText.append(
                    "第 "
            ).append(
                    episodeNumber
            ).append(
                    " 集"
            );
        }

        if (episodeName != null &&
                !episodeName.trim().isEmpty()) {

            if (episodeText.length() > 0) {
                episodeText.append(" · ");
            }

            episodeText.append(
                    episodeName
            );
        }

        if (episodeText.length() == 0) {
            episodeText.append("继续观看");
        }

        holder.episode.setText(
                episodeText.toString()
        );

        long positionMs =
                history.getPositionMs();

        long durationMs =
                history.getDurationMs();

        StringBuilder progressText =
                new StringBuilder();

        if (positionMs > 0) {
            progressText.append(
                    FormatUtils.formatDuration(
                            positionMs
                    )
            );
        } else {
            progressText.append("未开始");
        }

        if (durationMs > 0) {
            progressText.append(" / ");
            progressText.append(
                    FormatUtils.formatDuration(
                            durationMs
                    )
            );
        }

        if (history.getSpeed() > 0 &&
                history.getSpeed() != 1.0f) {

            progressText.append(" · ");
            progressText.append(
                    formatSpeed(history.getSpeed())
            );
        }

        if (history.isCompleted()) {
            progressText.append(" · 已看完");
        }

        holder.progress.setText(
                progressText.toString()
        );

        long watchTime =
                history.getLastWatchTime();

        if (watchTime > 0) {
            holder.time.setText(
                    FormatUtils.formatTimeAgo(
                            watchTime
                    )
            );
        } else {
            holder.time.setText("");
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onHistoryClick(
                        histories.get(adapterPosition),
                        adapterPosition
                );
            }
        });
