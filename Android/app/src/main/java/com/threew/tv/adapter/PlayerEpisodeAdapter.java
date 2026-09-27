package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
 * 播放器集数适配器。
 *
 * 用于播放器中的集数列表：
 * - 当前播放集高亮
 * - 已观看状态
 * - 已下载状态
 * - TV 遥控器焦点
 * - 手机触摸操作
 */
public class PlayerEpisodeAdapter
        extends RecyclerView.Adapter<PlayerEpisodeAdapter.EpisodeViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(
                Episode episode,
                int position
        );
    }

    private final List<Episode> episodes =
            new ArrayList<>();

    private final OnEpisodeClickListener listener;

    private int selectedPosition = 0;

    public PlayerEpisodeAdapter(
            OnEpisodeClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<Episode> data
    ) {

        episodes.clear();

        if (data != null) {
            episodes.addAll(data);
        }

        if (episodes.isEmpty()) {

            selectedPosition = -1;

        } else if (
                selectedPosition < 0 ||
                selectedPosition >= episodes.size()
        ) {

            selectedPosition = 0;
        }

        notifyDataSetChanged();
    }

    public void setSelectedPosition(
            int position
    ) {

        if (position < 0 ||
                position >= episodes.size()) {

            return;
        }

        int old =
                selectedPosition;

        selectedPosition =
                position;

        if (old >= 0 &&
                old < episodes.size()) {

            notifyItemChanged(old);
        }

        notifyItemChanged(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Episode getItem(
            int position
    ) {

        if (position < 0 ||
                position >= episodes.size()) {

            return null;
        }

        return episodes.get(position);
    }

    public List<Episode> getItems() {
        return new ArrayList<>(episodes);
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
                dp(parent, 7),
                dp(parent, 12),
                dp(parent, 7)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 54)
                );

        rootParams.setMargins(
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3),
                dp(parent, 3)
        );

        root.setLayoutParams(rootParams);

        TextView number =
                new TextView(
                        parent.getContext()
                );

        number.setTextSize(12);

        number.setGravity(
                Gravity.CENTER
        );

        number.setTextColor(
                Color.rgb(
                        130,
                        140,
                        152
                )
        );

        root.addView(
                number,
                new LinearLayout.LayoutParams(
                        dp(parent, 48),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextSize(13);

        name.setTextColor(
                Color.rgb(
                        230,
                        235,
                        241
                )
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setSingleLine(true);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                name,
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

        status.setTextSize(9);

        status.setGravity(
                Gravity.CENTER
        );

        status.setSingleLine(true);

        status.setTextColor(
                Color.rgb(
                        110,
                        195,
                        220
                )
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new EpisodeViewHolder(
                root,
                number,
                name,
                status
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull EpisodeViewHolder holder,
            int position
    ) {

        Episode episode =
                episodes.get(position);

        String number =
                buildEpisodeNumber(
                        episode,
                        position
                );

        holder.number.setText(
                number
        );

        String name =
                safe(
                        episode.getName()
                );

        if (name.isEmpty()) {

            name =
                    "第 " +
                    (position + 1) +
                    " 集";
        }

        holder.name.setText(
                name
        );

        holder.status.setText(
                buildStatus(
                        episode
                )
        );

        boolean selected =
                position ==
                        selectedPosition;

        applyBackground(
                holder.itemView,
                selected,
                false
        );

        holder.number.setTextColor(
                selected
                        ? Color.rgb(
                                100,
                                215,
                                235
                        )
                        : Color.rgb(
                                130,
                                140,
                                152
                        )
        );

        holder.name.setTextColor(
                selected
                        ? Color.rgb(
                                245,
                                250,
                                255
                        )
                        : Color.rgb(
                                225,
                                230,
                                236
                        )
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    boolean currentSelected =
                            adapterPosition ==
                                    selectedPosition;

                    applyBackground(
                            v,
                            currentSelected,
                            hasFocus
                    );

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.02f)
                                .scaleY(1.02f)
                                .setDuration(100)
                                .start();

                    } else {

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

                    setSelectedPosition(
                            adapterPosition
                    );

                    if (listener != null) {

                        listener.onEpisodeClick(
                                episodes.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );
    }

    private String buildEpisodeNumber(
            Episode episode,
            int position
    ) {

        int number =
                episode.getNumber();

        if (number <= 0) {
            number =
                    position + 1;
        }

        return String.format(
                "EP %02d",
                number
        );
    }

    private String buildStatus(
            Episode episode
    ) {

        if (episode.isDownloaded()) {

            return "已下载";
        }

        if (episode.isWatched()) {

            return "已看";
        }

        long position =
                episode.getPositionMs();

        if (position > 0) {

            return "继续";
        }

        return "";
    }

    private void applyBackground(
            View view,
            boolean selected,
            boolean focused
    ) {

        int fill;
        int stroke;

        if (selected) {

            fill =
                    focused
                            ? Color.rgb(
                                    38,
                                    78,
                                    92
                            )
                            : Color.rgb(
                                    28,
                                    61,
                                    74
                            );

            stroke =
                    Color.rgb(
                            75,
                            175,
                            205
                    );

        } else if (focused) {

            fill =
                    Color.rgb(
                            36,
                            44,
                            54
                    );

            stroke =
                    Color.rgb(
                            75,
                            105,
                            120
                    );

        } else {

            fill =
                    Color.rgb(
                            22,
                            26,
                            33
                    );

            stroke =
                    Color.rgb(
                            48,
                            57,
                            68
                    );
        }

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                fill
        );

        drawable.setStroke(
                dpValue(
                        view,
                        1
                ),
                stroke
        );

        drawable.setCornerRadius(
                dpValue(
                        view,
                        8
                )
        );

        view.setBackground(
                drawable
        );
    }

    private int dpValue(
            View view,
            int value
    ) {

        return Math.round(
                value *
                        view.getResources()
                                .getDisplayMetrics()
                                .density
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

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    static class EpisodeViewHolder
            extends RecyclerView.ViewHolder {

        final TextView number;
        final TextView name;
        final TextView status;

        EpisodeViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView name,
                TextView status
        ) {

            super(itemView);

            this.number = number;
            this.name = name;
            this.status = status;
        }
    }
}
