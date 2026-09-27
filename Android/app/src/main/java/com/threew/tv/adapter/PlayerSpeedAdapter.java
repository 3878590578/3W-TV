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
 * 播放速度选择适配器
 *
 * 支持：
 * 1× / 1.5× / 2× / 2.5× / 3× / 5× / 8×
 */
public class PlayerSpeedAdapter
        extends RecyclerView.Adapter<PlayerSpeedAdapter.ViewHolder> {

    public interface OnSpeedClickListener {
        void onSpeedClick(float speed);
    }

    private final Context context;
    private final List<Float> speeds = new ArrayList<>(
            Arrays.asList(
                    1.0f,
                    1.5f,
                    2.0f,
                    2.5f,
                    3.0f,
                    5.0f,
                    8.0f
            )
    );

    private float selectedSpeed = 1.0f;
    private OnSpeedClickListener listener;

    public PlayerSpeedAdapter(Context context) {
        this.context = context;
    }

    public void setOnSpeedClickListener(
            OnSpeedClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSelectedSpeed(float speed) {
        selectedSpeed = speed;
        notifyDataSetChanged();
    }

    public float getSelectedSpeed() {
        return selectedSpeed;
    }

    public void setSpeeds(List<Float> values) {
        speeds.clear();

        if (values != null) {
            for (Float value : values) {
                if (value != null && value > 0) {
                    speeds.add(value);
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<Float> getSpeeds() {
        return new ArrayList<>(speeds);
    }

    @Override
    public int getItemCount() {
        return speeds.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        TextView textView = new TextView(context);

        int horizontal = dp(16);
        int vertical = dp(12);

        textView.setPadding(
                horizontal,
                vertical,
                horizontal,
                vertical
        );

        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setSingleLine(true);

        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        float speed = speeds.get(position);

        holder.bind(speed, isSelected(speed));

        holder.itemView.setOnClickListener(v -> {
            selectedSpeed = speed;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onSpeedClick(speed);
            }
        });
    }

    private boolean isSelected(float speed) {
        return Math.abs(speed - selectedSpeed) < 0.01f;
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(float speed, boolean selected) {
            textView.setText(formatSpeed(speed));

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(10));

            if (selected) {
                background.setColor(
                        Color.rgb(50, 120, 255)
                );

                textView.setTextColor(Color.WHITE);
                textView.setAlpha(1.0f);
            } else {
                background.setColor(
                        Color.rgb(31, 36, 44)
                );

                textView.setTextColor(
                        Color.rgb(205, 210, 218)
                );

                textView.setAlpha(0.9f);
            }

            textView.setBackground(background);
        }

        private String formatSpeed(float speed) {
            if (Math.abs(speed - Math.round(speed)) < 0.01f) {
                return Math.round(speed) + "×";
            }

            return String.format(
                    java.util.Locale.US,
                    "%.1f×",
                    speed
            );
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

    private int dp(int value) {
        return (int) (
                value
                        * context.getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }
        }
