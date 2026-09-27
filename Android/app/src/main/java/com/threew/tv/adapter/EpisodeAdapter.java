package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Episode;

import java.util.ArrayList;
import java.util.List;

/**
 * 剧集列表适配器。
 *
 * 支持：
 * - 集数
 * - 集名
 * - 播放状态
 * - 下载状态
 * - 已观看标记
 * - 当前选中集
 *
 * 同时适配手机、平板和 Android TV 遥控器焦点。
 */
public class EpisodeAdapter
        extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode, int position);
    }

    private final List<Episode> data =
            new ArrayList<>();

    private final OnEpisodeClickListener listener;

    private int selectedPosition = -1;

    public EpisodeAdapter(
            OnEpisodeClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<Episode> episodes
    ) {

        data.clear();

        if (episodes != null) {
            data.addAll(episodes);
        }

        selectedPosition = -1;

        notifyDataSetChanged();
    }

    public void setSelectedPosition(
            int position
    ) {

        int old = selectedPosition;

        selectedPosition = position;

        if (old >= 0 &&
                old < data.size()) {
            notifyItemChanged(old);
        }

        if (position >= 0 &&
                position < data.size()) {
            notifyItemChanged(position);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Episode getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public List<Episode> getItems() {
        return new ArrayList<>(data);
    }

    public void clear() {

        int size = data.size();

        data.clear();

        selectedPosition = -1;

        if (size > 0) {
            notifyItemRangeRemoved(
                    0,
                    size
            );
        }
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(
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
                dp(parent, 12),
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
                        dp(parent, 58)
                );

        rootParams.setMargins(
                0,
                0,
                0,
                dp(parent, 4)
        );

        root.setLayoutParams(rootParams);

        TextView number =
                new TextView(
                        parent.getContext()
                );

        number.setGravity(
                Gravity.CENTER
        );

        number.setTextColor(
                Color.rgb(55, 180, 255)
        );

        number.setTextSize(13);

        number.setTypeface(
                null,
                Typeface.BOLD
        );

        root.addView(
                number,
                new LinearLayout.LayoutParams(
                        dp(parent, 48),
                        dp(parent, 40)
                )
        );

        LinearLayout textBox =
                new LinearLayout(
                        parent.getContext()
                );

        textBox.setOrientation(
                LinearLayout.VERTICAL
        );

        textBox.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                textBox,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextColor(
                Color.rgb(245, 247, 250)
        );

        name.setTextSize(14);

        name.setMaxLines(1);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        textBox.addView(name);

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextColor(
                Color.rgb(115, 124, 140)
        );

        status.setTextSize(10);

        status.setMaxLines(1);

        status.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.setMargins(
                0,
                dp(parent, 3),
                0,
                0
        );

        textBox.addView(
                status,
                statusParams
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setGravity(
                Gravity.CENTER
        );

        state.setTextSize(11);

        state.setTextColor(
                Color.rgb(120, 130, 145)
        );

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 56),
                        dp(parent, 40)
                )
        );

        return new EpisodeViewHolder(
                root,
                number,
                name,
                status,
                state
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull EpisodeViewHolder holder,
            int position
    ) {

        Episode episode =
                data.get(position);

        int number =
                episode.getNumber();

        if (number > 0) {

            holder.number.setText(
                    String.valueOf(number)
            );

        } else {

            holder.number.setText(
                    String.valueOf(
                            position + 1
                    )
            );
        }

        String episodeName =
                safe(episode.getName());

        if (episodeName.isEmpty()) {

            episodeName =
                    "第 " +
                            (number > 0
                                    ? number
                                    : position + 1) +
                            " 集";
        }

        holder.name.setText(
                episodeName
        );

        StringBuilder statusText =
                new StringBuilder();

        if (episode.getDurationMs() > 0) {

            statusText.append(
                    formatDuration(
                            episode.getDurationMs()
                    )
            );
        }

        if (episode.getPositionMs() > 0) {

            if (statusText.length() > 0) {
                statusText.append("  ·  ");
            }

            statusText.append("已播放");
        }

        if (episode.isWatched()) {

            if (statusText.length() > 0) {
                statusText.append("  ·  ");
            }

            statusText.append("已看完");
        }

        holder.status.setText(
                statusText.toString()
        );

        if (episode.isDownloaded()) {

            holder.state.setText("已下载");
            holder.state.setTextColor(
                    Color.rgb(65, 210, 145)
            );

        } else if (episode.isWatched()) {

            holder.state.setText("✓");
            holder.state.setTextColor(
                    Color.rgb(65, 210, 145)
            );

        } else if (episode.getPositionMs() > 0) {

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

        boolean selected =
                position == selectedPosition;

        if (selected) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(28, 58, 78)
            );

            holder.name.setTextColor(
                    Color.rgb(255, 255, 255)
            );

            holder.number.setTextColor(
                    Color.rgb(85, 195, 255)
            );

        } else {

            holder.itemView.setBackgroundColor(
                    Color.rgb(20, 23, 31)
            );

            holder.name.setTextColor(
                    Color.rgb(245, 247, 250)
            );

            holder.number.setTextColor(
                    Color.rgb(55, 180, 255)
            );
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);

        holder.itemView.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {
                        return;
                    }

                    setSelectedPosition(
                            adapterPosition
                    );

                    if (listener != null) {

                        listener.onEpisodeClick(
                                data.get(adapterPosition),
                                adapterPosition
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class EpisodeViewHolder
            extends RecyclerView.ViewHolder {

        final TextView number;
        final TextView name;
        final TextView status;
        final TextView state;

        EpisodeViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView name,
                TextView status,
                TextView state
        ) {
            super(itemView);

            this.number = number;
            this.name = name;
            this.status = status;
            this.state = state;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String formatDuration(
            long durationMs
    ) {

        if (durationMs <= 0) {
            return "";
        }

        long totalSeconds =
                durationMs / 1000L;

        long hours =
                totalSeconds / 3600L;

        long minutes =
                (totalSeconds % 3600L) / 60L;

        long seconds =
                totalSeconds % 60L;

        if (hours > 0) {

            return String.format(
                    "%d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
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
}
