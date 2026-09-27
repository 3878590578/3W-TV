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
 * 播放器顶部/面板标签适配器。
 *
 * 用于：
 * - 选集
 * - 播放源
 * - 播放设置
 * - 字幕
 * - 音轨
 * - 更多
 */
public class PlayerTabAdapter
        extends RecyclerView.Adapter<PlayerTabAdapter.ViewHolder> {

    public static class TabItem {

        private final String id;
        private final String title;
        private boolean selected;
        private boolean enabled;
        private int badgeCount;

        public TabItem(
                String id,
                String title
        ) {
            this(
                    id,
                    title,
                    false,
                    true,
                    0
            );
        }

        public TabItem(
                String id,
                String title,
                boolean selected,
                boolean enabled,
                int badgeCount
        ) {
            this.id = id;
            this.title = title;
            this.selected = selected;
            this.enabled = enabled;
            this.badgeCount = badgeCount;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getBadgeCount() {
            return badgeCount;
        }

        public void setBadgeCount(int badgeCount) {
            this.badgeCount = Math.max(0, badgeCount);
        }
    }

    public interface OnTabClickListener {
        void onTabClick(TabItem item);
    }

    private final Context context;
    private final List<TabItem> items = new ArrayList<>();

    private OnTabClickListener listener;

    public PlayerTabAdapter(Context context) {
        this.context = context;
    }

    public void setOnTabClickListener(
            OnTabClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<TabItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(TabItem item) {
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

    public TabItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void selectTab(String id) {
        for (int i = 0; i < items.size(); i++) {
            TabItem item = items.get(i);

            boolean selected =
                    item.getId().equals(id);

            if (item.isSelected() != selected) {
                item.setSelected(selected);
                notifyItemChanged(i);
            }
        }
    }

    public void setEnabled(
            String id,
            boolean enabled
    ) {
        for (int i = 0; i < items.size(); i++) {
            TabItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setEnabled(enabled);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void setBadgeCount(
            String id,
            int count
    ) {
        for (int i = 0; i < items.size(); i++) {
            TabItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setBadgeCount(count);
                notifyItemChanged(i);
                return;
            }
        }
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
        TabItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            selectTab(item.getId());

            if (listener != null) {
                listener.onTabClick(item);
            }
        });
    }

    private View createView() {
        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);

        layout.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setPadding(
                dp(14),
                dp(9),
                dp(14),
                dp(9)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.rightMargin = dp(6);

        layout.setLayoutParams(params);

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);

        layout.addView(
                title,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView badge = new TextView(context);
        badge.setId(android.R.id.text2);
        badge.setTextSize(9);
        badge.setGravity(Gravity.CENTER);
        badge.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                badgeParams =
                new android.widget.LinearLayout.LayoutParams(
                        dp(18),
                        dp(18)
                );

        badgeParams.leftMargin = dp(5);

        layout.addView(badge, badgeParams);

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
        private final TextView badge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    android.R.id.text1
            );

            badge = itemView.findViewById(
                    android.R.id.text2
            );
        }

        public void bind(TabItem item) {
            title.setText(item.getTitle());

            if (item.getBadgeCount() > 0) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(
                        item.getBadgeCount() > 99
                                ? "99+"
                                : String.valueOf(
                                        item.getBadgeCount()
                                )
                );
            } else {
                badge.setVisibility(View.GONE);
            }

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(20));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(29, 32, 37)
                );

                title.setTextColor(
                        Color.rgb(95, 100, 108)
                );

                badge.setTextColor(
                        Color.rgb(85, 90, 98)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(36, 57, 76)
                );

                title.setTextColor(
                        Color.rgb(110, 190, 255)
                );

                badge.setTextColor(
                        Color.WHITE
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                title.setTextColor(
                        Color.rgb(195, 200, 210)
                );

                badge.setTextColor(
                        Color.rgb(120, 180, 235)
                );

                itemView.setAlpha(1.0f);
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
