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
 * 播放器快进/快退选项适配器。
 *
 * 默认提供：
 * - 快退 5 秒
 * - 快退 10 秒
 * - 快退 15 秒
 * - 快退 30 秒
 * - 快进 5 秒
 * - 快进 10 秒
 * - 快进 15 秒
 * - 快进 30 秒
 *
 * 可由播放器设置面板动态调整。
 */
public class PlayerSeekAdapter
        extends RecyclerView.Adapter<PlayerSeekAdapter.ViewHolder> {

    public static class SeekItem {

        public static final int TYPE_BACKWARD = 0;
        public static final int TYPE_FORWARD = 1;

        private final String id;
        private final String title;
        private final int seconds;
        private final int type;
        private boolean enabled;
        private boolean selected;

        public SeekItem(
                String id,
                String title,
                int seconds,
                int type
        ) {
            this(
                    id,
                    title,
                    seconds,
                    type,
                    true,
                    false
            );
        }

        public SeekItem(
                String id,
                String title,
                int seconds,
                int type,
                boolean enabled,
                boolean selected
        ) {
            this.id = id;
            this.title = title;
            this.seconds = Math.max(0, seconds);
            this.type = type;
            this.enabled = enabled;
            this.selected = selected;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getType() {
            return type;
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

        public boolean isBackward() {
            return type == TYPE_BACKWARD;
        }

        public boolean isForward() {
            return type == TYPE_FORWARD;
        }
    }

    public interface OnSeekClickListener {
        void onSeekClick(SeekItem item);
    }

    private final Context context;
    private final List<SeekItem> items = new ArrayList<>();

    private OnSeekClickListener listener;

    public PlayerSeekAdapter(Context context) {
        this.context = context;
    }

    public void setOnSeekClickListener(
            OnSeekClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<SeekItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(SeekItem item) {
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

    public SeekItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void setSelected(String id) {
        for (int i = 0; i < items.size(); i++) {
            SeekItem item = items.get(i);

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
            SeekItem item = items.get(i);

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
        SeekItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onSeekClick(item);
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
                dp(10),
                dp(14),
                dp(10)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.rightMargin = dp(7);
        params.bottomMargin = dp(7);

        layout.setLayoutParams(params);

        TextView icon = new TextView(context);
        icon.setId(android.R.id.icon);
        icon.setTextSize(17);
        icon.setGravity(Gravity.CENTER);
        icon.setSingleLine(true);

        layout.addView(
                icon,
                new android.widget.LinearLayout.LayoutParams(
                        dp(30),
                        dp(30)
                )
        );

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(12);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.leftMargin = dp(5);

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

        public void bind(SeekItem item) {
            icon.setText(
                    item.isBackward()
                            ? "↶"
                            : "↷"
            );

            title.setText(item.getTitle());

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(14));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(29, 32, 37)
                );

                icon.setTextColor(
                        Color.rgb(88, 94, 102)
                );

                title.setTextColor(
                        Color.rgb(92, 98, 106)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(36, 57, 76)
                );

                icon.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                title.setTextColor(
                        Color.rgb(175, 215, 245)
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                icon.setTextColor(
                        Color.rgb(220, 226, 234)
                );

                title.setTextColor(
                        Color.rgb(190, 196, 207)
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
