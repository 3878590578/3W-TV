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
 * 详情页视频来源选择适配器。
 *
 * 用途：
 * - 同一影片存在多个来源时显示来源列表
 * - 当前默认来源高亮
 * - 支持切换来源
 * - 支持 Android TV 遥控器焦点
 */
public class SourceChoiceAdapter
        extends RecyclerView.Adapter<SourceChoiceAdapter.SourceChoiceViewHolder> {

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

    public SourceChoiceAdapter(
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

        selectedPosition = findDefaultPosition();

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

        notifyItemChanged(selectedPosition);
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

        return data.isEmpty()
                ? -1
                : 0;
    }

    @NonNull
    @Override
    public SourceChoiceViewHolder onCreateViewHolder(
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
                dp(parent, 14),
                dp(parent, 8),
                dp(parent, 14),
                dp(parent, 8)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        dp(parent, 112),
                        dp(parent, 64)
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

        name.setTextColor(
                Color.rgb(225, 230, 236)
        );

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
                        dp(parent, 24)
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

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        return new SourceChoiceViewHolder(
                root,
                name,
                state
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SourceChoiceViewHolder holder,
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

        if (selected) {

            holder.state.setText(
                    "当前"
            );

            holder.state.setTextColor(
                    Color.rgb(90, 210, 250)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(25, 65, 84)
            );

            holder.name.setTextColor(
                    Color.rgb(245, 250, 255)
            );

        } else if (!enabled) {

            holder.state.setText(
                    "已停用"
            );

            holder.state.setTextColor(
                    Color.rgb(120, 125, 132)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(28, 30, 35)
            );

            holder.name.setTextColor(
                    Color.rgb(145, 150, 158)
            );

        } else {

            holder.state.setText(
                    "可用"
            );

            holder.state.setTextColor(
                    Color.rgb(125, 170, 190)
            );

            holder.itemView.setBackgroundColor(
                    Color.rgb(22, 25, 32)
            );

            holder.name.setTextColor(
                    Color.rgb(220, 225, 232)
            );
        }

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                selected
                                        ? Color.rgb(30, 75, 96)
                                        : Color.rgb(34, 42, 52)
                        );

                        v.animate()
                                .scaleX(1.04f)
                                .scaleY(1.04f)
                                .setDuration(100)
                                .start();

                    } else {

                        if (position == selectedPosition) {

                            v.setBackgroundColor(
                                    Color.rgb(25, 65, 84)
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

                        notifyItemChanged(oldPosition);
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

    static class SourceChoiceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView state;

        SourceChoiceViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView state
        ) {

            super(itemView);

            this.name = name;
            this.state = state;
        }
    }
}
