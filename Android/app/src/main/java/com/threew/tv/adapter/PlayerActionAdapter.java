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
 * 播放器操作项适配器。
 *
 * 用于播放器底部/侧边操作面板。
 *
 * 常见操作：
 * - 播放
 * - 暂停
 * - 上一集
 * - 下一集
 * - 快退
 * - 快进
 * - 选集
 * - 播放源
 * - 倍速
 * - 画面
 * - 字幕
 * - 音轨
 * - 设置
 * - 锁定
 */
public class PlayerActionAdapter
        extends RecyclerView.Adapter<PlayerActionAdapter.ViewHolder> {

    public static class ActionItem {

        private final String id;
        private final String title;
        private final String icon;
        private String value;
        private boolean enabled;
        private boolean selected;

        public ActionItem(
                String id,
                String title,
                String icon
        ) {
            this(
                    id,
                    title,
                    icon,
                    null,
                    true,
                    false
            );
        }

        public ActionItem(
                String id,
                String title,
                String icon,
                String value,
                boolean enabled,
                boolean selected
        ) {
            this.id = id;
            this.title = title;
            this.icon = icon;
            this.value = value;
            this.enabled = enabled;
            this.selected = selected;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public interface OnActionClickListener {
        void onActionClick(ActionItem item);
    }

    private final Context context;
    private final List<ActionItem> items = new ArrayList<>();

    private OnActionClickListener listener;

    public PlayerActionAdapter(Context context) {
        this.context = context;
    }

    public void setOnActionClickListener(
            OnActionClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<ActionItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(ActionItem item) {
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

    public ActionItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void updateValue(
            String id,
            String value
    ) {
        for (int i = 0; i < items.size(); i++) {
            ActionItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setValue(value);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void setSelected(
            String id,
            boolean selected
    ) {
        for (int i = 0; i < items.size(); i++) {
            ActionItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setSelected(selected);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void setEnabled(
            String id,
            boolean enabled
    ) {
        for (int i = 0; i < items.size(); i++) {
            ActionItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setEnabled(enabled);
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
        ActionItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onActionClick(item);
            }
        });
    }

    private View createView() {
        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);

        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.rightMargin = dp(6);
        params.bottomMargin = dp(6);

        layout.setLayoutParams(params);

        TextView icon = new TextView(context);
        icon.setId(android.R.id.icon);
        icon.setTextSize(19);
        icon.setGravity(Gravity.CENTER);
        icon.setSingleLine(true);

        layout.addView(
                icon,
                new android.widget.LinearLayout.LayoutParams(
                        dp(38),
                        dp(32)
                )
        );

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(10);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(3);

        layout.addView(title, titleParams);

        TextView value = new TextView(context);
        value.setId(android.R.id.text2);
        value.setTextSize(9);
        value.setGravity(Gravity.CENTER);
        value.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                valueParams =
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        valueParams.topMargin = dp(2);

        layout.addView(value, valueParams);

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

        private final TextView icon;
        private final TextView title;
        private final TextView value;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(
                    android.R.id.icon
            );

            title = itemView.findViewById(
                    android.R.id.text1
            );

            value = itemView.findViewById(
                    android.R.id.text2
            );
        }

        public void bind(ActionItem item) {
            String iconText = item.getIcon();

            if (iconText == null) {
                iconText = "";
            }

            icon.setText(iconText);
            title.setText(item.getTitle());

            String valueText = item.getValue();

            if (valueText == null
                    || valueText.trim().isEmpty()) {

                value.setVisibility(View.GONE);

            } else {
                value.setVisibility(View.VISIBLE);
                value.setText(valueText);
            }

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(29, 32, 37)
                );

                icon.setTextColor(
                        Color.rgb(88, 94, 103)
                );

                title.setTextColor(
                        Color.rgb(92, 98, 107)
                );

                value.setTextColor(
                        Color.rgb(78, 84, 93)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(35, 56, 76)
                );

                icon.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                title.setTextColor(
                        Color.rgb(180, 215, 245)
                );

                value.setTextColor(
                        Color.rgb(120, 180, 230)
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                icon.setTextColor(
                        Color.rgb(225, 230, 238)
                );

                title.setTextColor(
                        Color.rgb(185, 191, 202)
                );

                value.setTextColor(
                        Color.rgb(125, 140, 158)
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
