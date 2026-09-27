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
 * 播放器控制项适配器。
 *
 * 用于播放器设置/控制面板：
 * - 播放速度
 * - 画面比例
 * - 屏幕方向
 * - 自动下一集
 * - 自动跳过片头
 * - 自动跳过片尾
 * - 重试播放
 * - 播放锁定
 * 等控制项。
 */
public class PlayerControlAdapter
        extends RecyclerView.Adapter<PlayerControlAdapter.ViewHolder> {

    public static class ControlItem {

        public static final int TYPE_ACTION = 0;
        public static final int TYPE_SWITCH = 1;
        public static final int TYPE_VALUE = 2;

        private final String id;
        private final String title;
        private String value;
        private boolean enabled;
        private final int type;

        public ControlItem(
                String id,
                String title,
                int type
        ) {
            this.id = id;
            this.title = title;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
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

        public int getType() {
            return type;
        }
    }

    public interface OnControlClickListener {
        void onControlClick(ControlItem item);
    }

    private final Context context;
    private final List<ControlItem> items = new ArrayList<>();

    private OnControlClickListener listener;

    public PlayerControlAdapter(Context context) {
        this.context = context;
    }

    public void setOnControlClickListener(
            OnControlClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<ControlItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(ControlItem item) {
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

    public ControlItem getItem(int position) {
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
            ControlItem item = items.get(i);

            if (item.getId().equals(id)) {
                item.setValue(value);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void updateEnabled(
            String id,
            boolean enabled
    ) {
        for (int i = 0; i < items.size(); i++) {
            ControlItem item = items.get(i);

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
        return new ViewHolder(createItemView());
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        ControlItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onControlClick(item);
            }
        });
    }

    private View createItemView() {
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

        TextView value = new TextView(context);
        value.setId(android.R.id.text2);
        value.setTextSize(13);
        value.setGravity(Gravity.CENTER_VERTICAL);
        value.setSingleLine(true);

        layout.addView(
                value,
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
        private final TextView value;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    android.R.id.text1
            );

            value = itemView.findViewById(
                    android.R.id.text2
            );
        }

        public void bind(ControlItem item) {
            title.setText(item.getTitle());

            String displayValue = item.getValue();

            if (displayValue == null
                    || displayValue.trim().isEmpty()) {

                if (item.getType()
                        == ControlItem.TYPE_SWITCH) {

                    displayValue = item.isEnabled()
                            ? "开启"
                            : "关闭";
                } else {
                    displayValue = "";
                }
            }

            value.setText(displayValue);

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (item.isEnabled()) {
                background.setColor(
                        Color.rgb(32, 39, 50)
                );

                title.setTextColor(Color.WHITE);
                value.setTextColor(
                        Color.rgb(100, 175, 255)
                );
            } else {
                background.setColor(
                        Color.rgb(28, 33, 40)
                );

                title.setTextColor(
                        Color.rgb(210, 214, 222)
                );

                value.setTextColor(
                        Color.rgb(145, 153, 165)
                );
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
