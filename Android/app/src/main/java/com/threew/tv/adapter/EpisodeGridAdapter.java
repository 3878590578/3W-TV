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
import com.threew.tv.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 集数网格适配器。
 *
 * 适合详情页：
 * - 大量集数
 * - 手机/平板网格
 * - Android TV 遥控器焦点操作
 *
 * 状态：
 * - 普通
 * - 当前选中
 * - 已观看
 * - 有播放进度
 * - 已下载
 */
public class EpisodeGridAdapter
        extends RecyclerView.Adapter<EpisodeGridAdapter.EpisodeGridViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(
                Episode episode,
                int position
        );
    }

    public interface OnEpisodeLongClickListener {
        boolean onEpisodeLongClick(
                Episode episode,
                int position
        );
    }

    private final List<Episode> data =
            new ArrayList<>();

    private final OnEpisodeClickListener clickListener;
    private final OnEpisodeLongClickListener longClickListener;

    private int selectedPosition = -1;

    public EpisodeGridAdapter(
            OnEpisodeClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public EpisodeGridAdapter(
            OnEpisodeClickListener clickListener,
            OnEpisodeLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Episode> episodes
    ) {

        data.clear();

        if (episodes != null) {
            data.addAll(episodes);
        }

        if (selectedPosition >= data.size()) {
            selectedPosition = -1;
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<Episode> episodes
    ) {

        if (episodes == null ||
                episodes.isEmpty()) {
            return;
        }

        int start =
                data.size();

        data.addAll(episodes);

        notifyItemRangeInserted(
                start,
                episodes.size()
        );
    }

    public void clear() {

        int size =
                data.size();

        data.clear();

        selectedPosition = -1;

        if (size > 0) {

            notifyItemRangeRemoved(
                    0,
                    size
            );
        }
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

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            selectedPosition = -1;

            notifyDataSetChanged();

            return;
        }

        int old =
                selectedPosition;

        selectedPosition =
                position;

        if (old >= 0 &&
                old < data.size()) {

            notifyItemChanged(old);
        }

        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public EpisodeGridViewHolder onCreateViewHolder(
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
                Gravity.CENTER
        );

        root.setPadding(
                dp(parent, 5),
                dp(parent, 7),
                dp(parent, 5),
                dp(parent, 7)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 68)
                );

        params.setMargins(
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3)
        );

        root.setLayoutParams(params);

        TextView number =
                new TextView(
                        parent.getContext()
                );

        number.setGravity(
                Gravity.CENTER
        );

        number.setTextColor(
                Color.rgb(240, 243, 247)
        );

        number.setTextSize(13);

        number.setTypeface(
                null,
                Typeface.BOLD
        );

        number.setMaxLines(1);

        number.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                number,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 28)
                )
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setGravity(
                Gravity.CENTER
        );

        state.setTextSize(9);

        state.setMaxLines(1);

        state.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        return new EpisodeGridViewHolder(
                root,
                number,
                state
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull EpisodeGridViewHolder holder,
            int position
    ) {

        Episode episode =
                data.get(position);

        int number =
                episode.getNumber();

        String name =
                safe(episode.getName());

        if (number > 0) {

            holder.number.setText(
                    "第 " +
                            number +
                            " 集"
            );

        } else if (!name.isEmpty()) {

            holder.number.setText(
                    name
            );

        } else {

            holder.number.setText(
                    "第 " +
                            (position + 1) +
                            " 集"
            );
        }

        StringBuilder stateText =
                new StringBuilder();

        if (episode.isDownloaded()) {

            stateText.append(
                    "已下载"
            );

        } else if (episode.isWatched()) {

            stateText.append(
                    "已看"
            );

        } else {

            long positionMs =
                    episode.getPositionMs();

            long durationMs =
                    episode.getDurationMs();

            if (positionMs > 0 &&
                    durationMs > 0 &&
                    positionMs < durationMs) {

                int percent =
                        (int) (
                                positionMs *
                                        100L /
                                        durationMs
                        );

                stateText.append(
                        percent
                );

                stateText.append("%");

            } else if (episode.getDurationMs() > 0) {

                stateText.append(
                        FormatUtils.formatDuration(
                                episode.getDurationMs()
                        )
                );

            } else {

                stateText.append(
                        "未播放"
                );
            }
        }

        holder.state.setText(
                stateText.toString()
        );

        boolean selected =
                position == selectedPosition;

        if (selected) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(35, 105, 140)
            );

            holder.number.setTextColor(
                    Color.WHITE
            );

            holder.state.setTextColor(
                    Color.rgb(215, 240, 255)
            );

        } else if (episode.isDownloaded()) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(24, 65, 53)
            );

            holder.number.setTextColor(
                    Color.rgb(225, 250, 238)
            );

            holder.state.setTextColor(
                    Color.rgb(80, 215, 160)
            );

        } else if (episode.isWatched()) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(45, 48, 56)
            );

            holder.number.setTextColor(
                    Color.rgb(175, 182, 192)
            );

            holder.state.setTextColor(
                    Color.rgb(125, 135, 150)
            );

        } else if (
                episode.getPositionMs() > 0
        ) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(35, 50, 61)
            );

            holder.number.setTextColor(
                    Color.rgb(235, 242, 248)
            );

            holder.state.setTextColor(
                    Color.rgb(70, 190, 245)
            );

        } else {

            holder.itemView.setBackgroundColor(
                    Color.rgb(27, 30, 39)
            );

            holder.number.setTextColor(
                    Color.rgb(235, 238, 243)
            );

            holder.state.setTextColor(
                    Color.rgb(110, 120, 135)
            );
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setScaleX(1.04f);
                        v.setScaleY(1.04f);

                        if (position != selectedPosition) {

                            v.setBackgroundColor(
                                    Color.rgb(28, 58, 78)
                            );
                        }

                    } else {

                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);

                        applyBackground(
                                holder,
                                episode,
                                position
                        );
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

                    setSelectedPosition(
                            adapterPosition
                    );

                    if (clickListener != null) {

                        clickListener.onEpisodeClick(
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
                                .onEpisodeLongClick(
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private void applyBackground(
            EpisodeGridViewHolder holder,
            Episode episode,
            int position
    ) {

        if (position == selectedPosition) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(35, 105, 140)
            );

            return;
        }

        if (episode.isDownloaded()) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(24, 65, 53)
            );

        } else if (episode.isWatched()) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(45, 48, 56)
            );

        } else if (episode.getPositionMs() > 0) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(35, 50, 61)
            );

        } else {

            holder.itemView.setBackgroundColor(
                    Color.rgb(27, 30, 39)
            );
        }
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

    static class EpisodeGridViewHolder
            extends RecyclerView.ViewHolder {

        final TextView number;
        final TextView state;

        EpisodeGridViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView state
        ) {

            super(itemView);

            this.number = number;
            this.state = state;
        }
    }
}
