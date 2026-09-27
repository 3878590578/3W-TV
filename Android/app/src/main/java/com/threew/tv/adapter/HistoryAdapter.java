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

import com.threew.tv.model.History;
import com.threew.tv.utils.FormatUtils;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史适配器。
 *
 * 显示：
 * - 视频封面
 * - 视频名称
 * - 集数
 * - 播放进度
 * - 播放速度
 * - 最近观看时间
 * - 完成状态
 */
public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(
                History history,
                int position
        );
    }

    public interface OnHistoryLongClickListener {
        boolean onHistoryLongClick(
                History history,
                int position
        );
    }

    private final List<History> data =
            new ArrayList<>();

    private final OnHistoryClickListener clickListener;
    private final OnHistoryLongClickListener longClickListener;

    public HistoryAdapter(
            OnHistoryClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public HistoryAdapter(
            OnHistoryClickListener clickListener,
            OnHistoryLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<History> histories
    ) {

        data.clear();

        if (histories != null) {
            data.addAll(histories);
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<History> histories
    ) {

        if (histories == null ||
                histories.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(histories);

        notifyItemRangeInserted(
                start,
                histories.size()
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

    public History getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public List<History> getItems() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
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

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 104)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
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
                        dp(parent, 70),
                        dp(parent, 88)
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

        title.setTextSize(15);

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        title.setMaxLines(1);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        info.addView(title);

        TextView episode =
                new TextView(
                        parent.getContext()
                );

        episode.setTextColor(
                Color.rgb(160, 168, 182)
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
                dp(parent, 5),
                0,
                0
        );

        info.addView(
                episode,
                episodeParams
        );

        TextView progress =
                new TextView(
                        parent.getContext()
                );

        progress.setTextColor(
                Color.rgb(55, 180, 255)
        );

        progress.setTextSize(10);

        progress.setMaxLines(1);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        info.addView(
                progress,
                progressParams
        );

        TextView time =
                new TextView(
                        parent.getContext()
                );

        time.setTextColor(
                Color.rgb(105, 115, 130)
        );

        time.setTextSize(9);

        time.setMaxLines(1);

        time.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        timeParams.setMargins(
                0,
                dp(parent, 3),
                0,
                0
        );

        info.addView(
                time,
                timeParams
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setGravity(
                Gravity.CENTER
        );

        state.setTextSize(10);

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        dp(parent, 48)
                )
        );

        return new HistoryViewHolder(
                root,
                poster,
                title,
                episode,
                progress,
                time,
                state
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistoryViewHolder holder,
            int position
    ) {

        History history =
                data.get(position);

        String title =
                safe(history.getVideoName());

        if (title.isEmpty()) {
            title = "未命名视频";
        }

        holder.title.setText(title);

        String episodeName =
                safe(history.getEpisodeName());

        if (history.getEpisodeNumber() > 0) {

            if (episodeName.isEmpty()) {

                episodeName =
                        "第 " +
                                history.getEpisodeNumber() +
                                " 集";

            } else {

                episodeName =
                        "第 " +
                                history.getEpisodeNumber() +
                                " 集  ·  " +
                                episodeName;
            }
        }

        if (episodeName.isEmpty()) {
            episodeName = "继续观看";
        }

        holder.episode.setText(
                episodeName
        );

        long positionMs =
                Math.max(
                        0,
                        history.getPositionMs()
                );

        long durationMs =
                Math.max(
                        0,
                        history.getDurationMs()
                );

        if (durationMs > 0) {

            int percent =
                    (int) Math.min(
                            100,
                            Math.round(
                                    positionMs * 100f /
                                            durationMs
                            )
                    );

            holder.progress.setText(
                    formatTime(positionMs) +
                            " / " +
                            formatTime(durationMs) +
                            "  ·  " +
                            percent +
                            "%"
            );

        } else {

            holder.progress.setText(
                    formatTime(positionMs)
            );
        }

        StringBuilder timeText =
                new StringBuilder();

        if (history.getSpeed() > 0) {

            timeText.append(
                    formatSpeed(
                            history.getSpeed()
                    )
            );
        }

        if (history.getLastWatchTime() > 0) {

            if (timeText.length() > 0) {
                timeText.append("  ·  ");
            }

            timeText.append(
                    FormatUtils.formatTimeAgo(
                            history.getLastWatchTime()
                    )
            );
        }

        holder.time.setText(
                timeText.toString()
        );

        if (history.isCompleted()) {

            holder.state.setText("已看完");

            holder.state.setTextColor(
                    Color.rgb(65, 210, 145)
            );

        } else if (positionMs > 0) {

            holder.state.setText("继续");

            holder.state.setTextColor(
                    Color.rgb(55, 180, 255)
            );

        } else {

            holder.state.setText("播放");

            holder.state.setTextColor(
                    Color.rgb(120, 130, 145)
            );
        }

        holder.poster.setImageDrawable(
                null
        );

        String poster =
                safe(history.getPoster());

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

                        clickListener.onHistoryClick(
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
                                .onHistoryLongClick(
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

    static class HistoryViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView episode;
        final TextView progress;
        final TextView time;
        final TextView state;

        HistoryViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView episode,
                TextView progress,
                TextView time,
                TextView state
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.episode = episode;
            this.progress = progress;
            this.time = time;
            this.state = state;
        }
    }

    private String formatTime(
            long milliseconds
    ) {

        if (milliseconds <= 0) {
            return "00:00";
        }

        long totalSeconds =
                milliseconds / 1000L;

        long hours =
                totalSeconds / 3600L;

        long minutes =
                (totalSeconds % 3600L) / 60L;

        long seconds =
                totalSeconds % 60L;

        if (hours > 0) {

            return String.format(
                    java.util.Locale.getDefault(),
                    "%d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    private String formatSpeed(
            float speed
    ) {

        if (speed == 1f) {
            return "1×";
        }

        if (speed == 1.5f) {
            return "1.5×";
        }

        if (speed == 2f) {
            return "2×";
        }

        if (speed == 2.5f) {
            return "2.5×";
        }

        if (speed == 3f) {
            return "3×";
        }

        if (speed == 5f) {
            return "5×";
        }

        if (speed == 8f) {
            return "8×";
        }

        return String.valueOf(speed) + "×";
    }

    private String safe(String value) {

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
}
