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
 * 播放器快捷按钮适配器。
 *
 * 用于播放器控制面板中的快捷操作：
 * - 播放/暂停
 * - 上一集
 * - 下一集
 * - 选集
 * - 倍速
 * - 清晰度
 * - 画面比例
 * - 字幕
 * - 音轨
 * - 锁定
 * - 更多设置
 */
public class PlayerButtonAdapter
        extends RecyclerView.Adapter<PlayerButtonAdapter.ViewHolder> {

    public static class ButtonItem {

        private final String id;
        private final String title;
        private final String icon;
        private boolean enabled;
        private boolean selected;

        public ButtonItem(
                String id,
                String title,
                String icon
        ) {
            this(
                    id,
                    title,
                    icon,
                    true,
                    false
            );
        }

        public ButtonItem(
                String id,
                String title,
                String icon,
                boolean enabled,
                boolean selected
        ) {
            this.id = id;
            this.title = title;
            this.icon = icon;
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

    public interface OnButtonClickListener {
        void onButtonClick(ButtonItem item);
    }

    private final Context context;
    private final List<ButtonItem> items = new ArrayList<>();

    private OnButtonClickListener listener;

    public PlayerButtonAdapter(Context context) {
        this.context = context;
    }

    public void setOnButtonClickListener(
            OnButtonClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<ButtonItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(ButtonItem item) {
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

    public ButtonItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void setSelected(
            String id,
            boolean selected
    ) {
        for (int i = 0; i < items.size(); i++) {
            ButtonItem item = items.get(i);

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
            ButtonItem item = items.get(i);

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
        ButtonItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onButtonClick(item);
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
                dp(9),
                dp(10),
                dp(9)
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
        icon.setTextSize(20);
        icon.setGravity(Gravity.CENTER);
        icon.setSingleLine(true);

        layout.addView(
                icon,
                new android.widget.LinearLayout.LayoutParams(
                        dp(38),
                        dp(34)
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(
                    android.R.id.icon
            );

            title = itemView.findViewById(
                    android.R.id.text1
            );
        }

        public void bind(ButtonItem item) {
            String iconText = item.getIcon();

            if (iconText == null) {
                iconText = "";
            }

            icon.setText(iconText);
            title.setText(item.getTitle());

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(30, 33, 38)
                );

                icon.setTextColor(
                        Color.rgb(90, 96, 105)
                );

                title.setTextColor(
                        Color.rgb(95, 101, 110)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(36, 58, 78)
                );

                icon.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                title.setTextColor(
                        Color.rgb(165, 205, 240)
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
