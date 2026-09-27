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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 剧集来源适配器。
 *
 * 用于详情页中：
 * - 同一影片多个视频源之间切换
 * - 显示来源名称
 * - 显示当前选中的来源
 * - 显示来源状态
 * - 支持 Android TV 遥控器焦点
 */
public class EpisodeSourceAdapter
        extends RecyclerView.Adapter<EpisodeSourceAdapter.EpisodeSourceViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(
                VideoSource source,
                int position
        );
    }

    private final List<VideoSource> data =
            new ArrayList<>();

    private final OnSourceClickListener listener;

    private int selectedPosition = -1;

    public EpisodeSourceAdapter(
            OnSourceClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<VideoSource> sources
    ) {

        data.clear();

        if (sources != null) {
            data.addAll(sources);
        }

        selectedPosition =
                findDefaultPosition();

        notifyDataSetChanged();
    }

    public void setSelectedPosition(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return;
        }

        int oldPosition =
                selectedPosition;

        selectedPosition =
                position;

        if (oldPosition >= 0 &&
                oldPosition < data.size()) {

            notifyItemChanged(oldPosition);
        }

        notifyItemChanged(
                selectedPosition
        );
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public VideoSource getSelectedSource() {

        if (selectedPosition < 0 ||
                selectedPosition >= data.size()) {

            return null;
        }

        return data.get(selectedPosition);
    }

    public VideoSource getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return null;
        }

        return data.get(position);
    }

    public List<VideoSource> getItems() {
        return new ArrayList<>(data);
    }

    private int findDefaultPosition() {

        for (int i = 0; i < data.size(); i++) {

            VideoSource source =
                    data.get(i);

            if (source != null &&
                    source.isDefaultSource()) {

                return i;
            }
        }

        for (int i = 0; i < data.size(); i++) {

            VideoSource source =
                    data.get(i);

            if (source != null &&
                    source.isEnabled()) {

                return i;
            }
        }

        return data.isEmpty()
                ? -1
                : 0;
    }

    @NonNull
    @Override
    public EpisodeSourceViewHolder onCreateViewHolder(
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
                dp(parent, 12),
                dp(parent, 7),
                dp(parent, 12),
                dp(parent, 7)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        dp(parent, 118),
                        dp(parent, 68)
                );

        params.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(params);

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setGravity(
                Gravity.CENTER
        );

        name.setTextSize(13);

        name.setTypeface(
                null,
                Typeface.BOLD
        );

        name.setMaxLines(1);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        root.addView(
                name,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 27)
                )
        );

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setGravity(
                Gravity.CENTER
        );

        status.setTextSize(9);

        status.setMaxLines(1);

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        TextView line =
                new TextView(
                        parent.getContext()
                );

        line.setBackgroundColor(
                Color.rgb(60, 75, 88)
        );

        LinearLayout.LayoutParams lineParams =
                new LinearLayout.LayoutParams(
                        dp(parent, 32),
                        dp(parent, 2)
                );

        lineParams.topMargin =
                dp(parent, 2);

        root.addView(
                line,
                lineParams
        );

        return new EpisodeSourceViewHolder(
                root,
                name,
                status,
                line
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull EpisodeSourceViewHolder holder,
            int position
    ) {

        VideoSource source =
                data.get(position);

        String name =
                source == null
                        ? "未知来源"
                        : source.getDisplayName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "未知来源";
        }

        holder.name.setText(
                name
        );

        boolean selected =
                position == selectedPosition;

        boolean enabled =
                source != null &&
                        source.isEnabled();

        boolean available =
                source != null &&
                        source.isAvailable();

        if (selected) {

            holder.status.setText(
                    "当前播放"
            );

            holder.status.setTextColor(
                    Color.rgb(92, 215, 255)
            );

            holder.name.setTextColor(
                    Color.WHITE
            );

            holder.line.setBackgroundColor(
                    Color.rgb(85, 205, 250)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(25, 67, 87)
            );

        } else if (!enabled) {

            holder.status.setText(
                    "已停用"
            );

            holder.status.setTextColor(
                    Color.rgb(120, 125, 132)
            );

            holder.name.setTextColor(
                    Color.rgb(145, 150, 158)
            );

            holder.line.setBackgroundColor(
                    Color.rgb(55, 58, 64)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(28, 30, 35)
            );

        } else if (!available) {

            holder.status.setText(
                    "待检测"
            );

            holder.status.setTextColor(
                    Color.rgb(175, 155, 105)
            );

            holder.name.setTextColor(
                    Color.rgb(205, 208, 214)
            );

            holder.line.setBackgroundColor(
                    Color.rgb(110, 95, 65)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(25, 27, 32)
            );

        } else {

            holder.status.setText(
                    "可用"
            );

            holder.status.setTextColor(
                    Color.rgb(120, 185, 205)
            );

            holder.name.setTextColor(
                    Color.rgb(220, 225, 232)
            );

            holder.line.setBackgroundColor(
                    Color.rgb(55, 105, 125)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(22, 25, 32)
            );
        }

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                selected
                                        ? Color.rgb(31, 78, 100)
                                        : Color.rgb(35, 44, 54)
                        );

                        v.animate()
                                .scaleX(1.04f)
                                .scaleY(1.04f)
                                .setDuration(100)
                                .start();

                    } else {

                        if (position == selectedPosition) {

                            v.setBackgroundColor(
                                    Color.rgb(25, 67, 87)
                            );

                        } else if (
                                source == null ||
                                !source.isEnabled()
                        ) {

                            v.setBackgroundColor(
                                    Color.rgb(28, 30, 35)
                            );

                        } else {

                            v.setBackgroundColor(
                                    Color.rgb(22, 25, 32)
                            );
                        }

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

                    VideoSource clicked =
                            data.get(adapterPosition);

                    if (clicked == null ||
                            !clicked.isEnabled()) {
                        return;
                    }

                    int oldPosition =
                            selectedPosition;

                    selectedPosition =
                            adapterPosition;

                    if (oldPosition >= 0 &&
                            oldPosition < data.size()) {

                        notifyItemChanged(
                                oldPosition
                        );
                    }

                    notifyItemChanged(
                            selectedPosition
                    );

                    if (listener != null) {

                        listener.onSourceClick(
                                clicked,
                                selectedPosition
                        );
                    }
                }
        );
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

    static class EpisodeSourceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView status;
        final TextView line;

        EpisodeSourceViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView status,
                TextView line
        ) {

            super(itemView);

            this.name = name;
            this.status = status;
            this.line = line;
        }
    }
        }
