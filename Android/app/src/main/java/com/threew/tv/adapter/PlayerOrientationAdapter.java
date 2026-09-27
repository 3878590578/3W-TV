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
 * 播放器屏幕方向选择适配器。
 *
 * 支持：
 * - 跟随设备
 * - 自动横屏
 * - 竖屏
 * - 横屏
 */
public class PlayerOrientationAdapter
        extends RecyclerView.Adapter<PlayerOrientationAdapter.ViewHolder> {

    public interface OnOrientationClickListener {
        void onOrientationClick(String orientation);
    }

    private final Context context;

    private final List<String> orientations = new ArrayList<>(
            Arrays.asList(
                    "跟随设备",
                    "自动横屏",
                    "竖屏",
                    "横屏"
            )
    );

    private String selectedOrientation = "跟随设备";

    private OnOrientationClickListener listener;

    public PlayerOrientationAdapter(Context context) {
        this.context = context;
    }

    public void setOnOrientationClickListener(
            OnOrientationClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSelectedOrientation(String orientation) {
        if (orientation == null
                || orientation.trim().isEmpty()) {
            return;
        }

        selectedOrientation = orientation;
        notifyDataSetChanged();
    }

    public String getSelectedOrientation() {
        return selectedOrientation;
    }

    public void setOrientations(List<String> values) {
        orientations.clear();

        if (values != null) {
            for (String value : values) {
                if (value != null
                        && !value.trim().isEmpty()) {
                    orientations.add(value);
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<String> getOrientations() {
        return new ArrayList<>(orientations);
    }

    @Override
    public int getItemCount() {
        return orientations.size();
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
        String orientation = orientations.get(position);

        holder.bind(
                orientation,
                orientation.equals(selectedOrientation)
        );

        holder.itemView.setOnClickListener(v -> {
            selectedOrientation = orientation;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onOrientationClick(orientation);
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
                String orientation,
                boolean selected
        ) {
            textView.setText(orientation);

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
