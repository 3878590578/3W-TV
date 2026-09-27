package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 播放器显示模式适配器。
 *
 * 用于播放器设置中的画面显示模式选择。
 *
 * 支持：
 * - 显示全部画面
 * - 铺满屏幕
 * - 裁剪
 * - 原始比例
 * - 16:9
 * - 4:3
 *
 * 本类只负责 UI 选择，
 * 实际播放器缩放由 PlayerController / PlayerActivity 处理。
 */
public class PlayerDisplayAdapter
        extends RecyclerView.Adapter<PlayerDisplayAdapter.ViewHolder> {

    public interface OnDisplayClickListener {
        void onDisplayClick(String mode);
    }

    private final Context context;

    private final List<String> modes = new ArrayList<>(
            Arrays.asList(
                    "显示全部画面",
                    "铺满屏幕",
                    "裁剪",
                    "原始比例",
                    "16:9",
                    "4:3"
            )
    );

    private String selectedMode = "显示全部画面";

    private OnDisplayClickListener listener;

    public PlayerDisplayAdapter(Context context) {
        this.context = context;
    }

    public void setOnDisplayClickListener(
            OnDisplayClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSelectedMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return;
        }

        selectedMode = mode;
        notifyDataSetChanged();
    }

    public String getSelectedMode() {
        return selectedMode;
    }

    public void setModes(List<String> values) {
        modes.clear();

        if (values != null) {
            for (String value : values) {
                if (value != null
                        && !value.trim().isEmpty()) {
                    modes.add(value);
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<String> getModes() {
        return new ArrayList<>(modes);
    }

    @Override
    public int getItemCount() {
        return modes.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        TextView textView = new TextView(context);

        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setSingleLine(true);

        textView.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(8);

        textView.setLayoutParams(params);

        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        String mode = modes.get(position);

        holder.bind(
                mode,
                mode.equals(selectedMode)
        );

        holder.itemView.setOnClickListener(v -> {
            selectedMode = mode;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onDisplayClick(mode);
            }
        });
    }

    private int dp(int value) {
        return (int) (
                value
                        * context.getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(
                String mode,
                boolean selected
        ) {
            textView.setText(mode);

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(10));

            if (selected) {
                background.setColor(
                        Color.rgb(48, 112, 235)
                );

                textView.setTextColor(Color.WHITE);
            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                textView.setTextColor(
                        Color.rgb(210, 214, 222)
                );
            }

            textView.setBackground(background);
        }

        private int dp(int value) {
            return (int) (
                    value
                            * itemView.getResources()
                            .getDisplayMetrics()
                            .density
                            + 0.5f
            );
        }
    }
        }
