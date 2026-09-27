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
 * 播放器选项适配器。
 *
 * 用于：
 * - 倍速选择
 * - 画面比例选择
 * - 屏幕方向选择
 * - 清晰度选择
 * - 字幕选择
 * - 音轨选择
 * - 其他播放器选项
 */
public class PlayerOptionAdapter
        extends RecyclerView.Adapter<PlayerOptionAdapter.ViewHolder> {

    public static class OptionItem {

        private final String id;
        private final String title;
        private final String description;
        private boolean selected;
        private boolean enabled;

        public OptionItem(
                String id,
                String title
        ) {
            this(
                    id,
                    title,
                    null,
                    false,
                    true
            );
        }

        public OptionItem(
                String id,
                String title,
                String description,
                boolean selected,
                boolean enabled
        ) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.selected = selected;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
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
    }

    public interface OnOptionClickListener {
        void onOptionClick(OptionItem item);
    }

    private final Context context;
    private final List<OptionItem> items = new ArrayList<>();

    private OnOptionClickListener listener;

    public PlayerOptionAdapter(Context context) {
        this.context = context;
    }

    public void setOnOptionClickListener(
            OnOptionClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<OptionItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(OptionItem item) {
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

    public OptionItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void setSelected(String id) {
        for (int i = 0; i < items.size(); i++) {
            OptionItem item = items.get(i);

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
            OptionItem item = items.get(i);

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
        OptionItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onOptionClick(item);
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
                dp(12),
                dp(16),
                dp(12)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(7);

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

        TextView description = new TextView(context);
        description.setId(android.R.id.text2);
        description.setTextSize(11);
        description.setSingleLine(true);
        description.setGravity(Gravity.CENTER);

        layout.addView(
                description,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView check = new TextView(context);
        check.setId(android.R.id.icon);
        check.setTextSize(15);
        check.setGravity(Gravity.CENTER);
        check.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                checkParams =
                new android.widget.LinearLayout.LayoutParams(
                        dp(28),
                        dp(28)
                );

        checkParams.leftMargin = dp(8);

        layout.addView(check, checkParams);

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
        private final TextView description;
        private final TextView check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    android.R.id.text1
            );

            description = itemView.findViewById(
                    android.R.id.text2
            );

            check = itemView.findViewById(
                    android.R.id.icon
            );
        }

        public void bind(OptionItem item) {
            title.setText(item.getTitle());

            String desc = item.getDescription();

            if (desc == null || desc.trim().isEmpty()) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
                description.setText(desc);
            }

            if (item.isSelected()) {
                check.setText("✓");
            } else {
                check.setText("");
            }

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(32, 34, 38)
                );

                title.setTextColor(
                        Color.rgb(115, 120, 128)
                );

                description.setTextColor(
                        Color.rgb(85, 90, 98)
                );

                check.setTextColor(
                        Color.rgb(80, 85, 92)
                );

                itemView.setAlpha(0.65f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(35, 52, 70)
                );

                title.setTextColor(
                        Color.WHITE
                );

                description.setTextColor(
                        Color.rgb(150, 190, 235)
                );

                check.setTextColor(
                        Color.rgb(100, 185, 255)
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                title.setTextColor(
                        Color.rgb(225, 229, 236)
                );

                description.setTextColor(
                        Color.rgb(135, 143, 155)
                );

                check.setTextColor(
                        Color.rgb(100, 175, 245)
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
