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
import java.util.List;

/**
 * 播放器菜单适配器。
 *
 * 用于播放器右侧/底部快捷菜单。
 *
 * 支持：
 * - 选集
 * - 播放源
 * - 倍速
 * - 画面
 * - 方向
 * - 字幕
 * - 音轨
 * - 设置
 * - 锁定
 */
public class PlayerMenuAdapter
        extends RecyclerView.Adapter<PlayerMenuAdapter.ViewHolder> {

    public static class MenuItem {

        private final String id;
        private final String title;
        private final String subtitle;
        private final boolean enabled;

        public MenuItem(
                String id,
                String title,
                String subtitle,
                boolean enabled
        ) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public interface OnMenuClickListener {
        void onMenuClick(MenuItem item);
    }

    private final Context context;
    private final List<MenuItem> items = new ArrayList<>();

    private OnMenuClickListener listener;

    public PlayerMenuAdapter(Context context) {
        this.context = context;
    }

    public void setOnMenuClickListener(
            OnMenuClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<MenuItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(MenuItem item) {
        if (item == null) {
            return;
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public MenuItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        return new ViewHolder(createView());
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        MenuItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onMenuClick(item);
            }
        });
    }

    private View createView() {
        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);

        layout.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        layout.setGravity(Gravity.CENTER_VERTICAL);

        layout.setPadding(
                dp(16),
                dp(13),
                dp(16),
                dp(13)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(8);

        layout.setLayoutParams(params);

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(15);
        title.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

        layout.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setId(android.R.id.text2);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setSingleLine(true);

        layout.addView(
                subtitle,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return layout;
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

        private final TextView title;
        private final TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    android.R.id.text1
            );

            subtitle = itemView.findViewById(
                    android.R.id.text2
            );
        }

        public void bind(MenuItem item) {
            title.setText(item.getTitle());

            String sub = item.getSubtitle();

            if (sub == null) {
                sub = "";
            }

            subtitle.setText(sub);

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (item.isEnabled()) {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                title.setTextColor(
                        Color.rgb(235, 238, 244)
                );

                subtitle.setTextColor(
                        Color.rgb(120, 175, 245)
                );

                itemView.setAlpha(1.0f);
            } else {
                background.setColor(
                        Color.rgb(35, 35, 38)
                );

                title.setTextColor(
                        Color.rgb(120, 125, 132)
                );

                subtitle.setTextColor(
                        Color.rgb(90, 95, 102)
                );

                itemView.setAlpha(0.65f);
            }

            itemView.setBackground(background);
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
