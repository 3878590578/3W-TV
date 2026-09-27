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
 * 播放清晰度 / 视频质量选择适配器。
 *
 * 用于播放器中的清晰度选择面板。
 *
 * 说明：
 * - 不负责实际切换清晰度。
 * - 只负责展示可用选项和当前选中状态。
 * - 实际清晰度切换由 PlayerController 处理。
 */
public class PlayerQualityAdapter
        extends RecyclerView.Adapter<PlayerQualityAdapter.ViewHolder> {

    public interface OnQualityClickListener {
        void onQualityClick(QualityItem item);
    }

    public static class QualityItem {

        private final String id;
        private final String name;
        private final String description;

        public QualityItem(
                String id,
                String name,
                String description
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Context context;
    private final List<QualityItem> items = new ArrayList<>();

    private String selectedId;
    private OnQualityClickListener listener;

    public PlayerQualityAdapter(Context context) {
        this.context = context;
    }

    public void setOnQualityClickListener(
            OnQualityClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<QualityItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(QualityItem item) {
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

    public void setSelectedId(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public QualityItem getItem(int position) {
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
        View view = createView(parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        QualityItem item = items.get(position);

        boolean selected =
                selectedId != null
                        && selectedId.equals(item.getId());

        holder.bind(item, selected);

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();

            if (listener != null) {
                listener.onQualityClick(item);
            }
        });
    }

    private View createView(ViewGroup parent) {
        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);

        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        layout.setGravity(Gravity.CENTER_VERTICAL);

        int horizontal = dp(16);
        int vertical = dp(11);

        layout.setPadding(
                horizontal,
                vertical,
                horizontal,
                vertical
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(8);

        layout.setLayoutParams(params);

        TextView name = new TextView(context);
        name.setId(android.R.id.text1);
        name.setTextSize(15);
        name.setSingleLine(true);

        TextView description = new TextView(context);
        description.setId(android.R.id.text2);
        description.setTextSize(11);
        description.setSingleLine(true);

        layout.addView(
                name,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        layout.addView(
                description,
                new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return layout;
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(android.R.id.text1);
            description = itemView.findViewById(android.R.id.text2);
        }

        public void bind(
                QualityItem item,
                boolean selected
        ) {
            name.setText(item.getName());

            String detail = item.getDescription();

            if (detail == null) {
                detail = "";
            }

            description.setText(detail);

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (selected) {
                background.setColor(
                        Color.rgb(48, 112, 235)
                );

                name.setTextColor(Color.WHITE);
                description.setTextColor(
                        Color.rgb(225, 235, 255)
                );
            } else {
                background.setColor(
                        Color.rgb(29, 34, 42)
                );

                name.setTextColor(
                        Color.rgb(235, 238, 244)
                );

                description.setTextColor(
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
