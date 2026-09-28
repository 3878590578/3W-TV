package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter
        extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public static class SettingItem {

        private String key;
        private String title;
        private String summary;
        private String value;
        private int type;

        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_SWITCH = 1;
        public static final int TYPE_VALUE = 2;
        public static final int TYPE_SECTION = 3;

        public SettingItem(
                String key,
                String title,
                String summary,
                String value,
                int type
        ) {
            this.key = key;
            this.title = title;
            this.summary = summary;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getValue() {
            return value;
        }

        public int getType() {
            return type;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public interface OnSettingClickListener {
        void onSettingClick(
                SettingItem item,
                int position
        );
    }

    private final List<SettingItem> data =
            new ArrayList<>();

    private final OnSettingClickListener listener;

    public SettingsAdapter(
            OnSettingClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<SettingItem> items
    ) {
        data.clear();

        if (items != null) {
            data.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void addItem(
            SettingItem item
    ) {
        if (item == null) {
            return;
        }

        int position = data.size();

        data.add(item);

        notifyItemInserted(position);
    }

    public void clear() {
        int size = data.size();

        data.clear();

        if (size > 0) {
            notifyItemRangeRemoved(0, size);
        }
    }

    public SettingItem getItem(
            int position
    ) {
        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public void updateValue(
            String key,
            String value
    ) {
        if (key == null) {
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            SettingItem item = data.get(i);

            if (key.equals(item.getKey())) {
                item.setValue(value);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void updateSummary(
            String key,
            String summary
    ) {
        if (key == null) {
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            SettingItem item = data.get(i);

            if (key.equals(item.getKey())) {
                item.setSummary(summary);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root =
                new LinearLayout(parent.getContext());

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 16),
                dp(parent, 12),
                dp(parent, 16),
                dp(parent, 12)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(parent, 5);

        root.setLayoutParams(params);

        TextView title =
                new TextView(parent.getContext());

        title.setTextSize(15);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView summary =
                new TextView(parent.getContext());

        summary.setTextSize(12);
        summary.setAlpha(0.70f);

        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        summaryParams.topMargin = dp(parent, 4);

        root.addView(
                summary,
                summaryParams
        );

        TextView value =
                new TextView(parent.getContext());

        value.setTextSize(12);
        value.setGravity(Gravity.END);

        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        valueParams.topMargin = dp(parent, 4);

        root.addView(
                value,
                valueParams
        );

        return new SettingsViewHolder(
                root,
                title,
                summary,
                value
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SettingsViewHolder holder,
            int position
    ) {
        SettingItem item = data.get(position);

        holder.title.setText(
                item.getTitle() == null
                        ? ""
                        : item.getTitle()
        );

        holder.summary.setText(
                item.getSummary() == null
                        ? ""
                        : item.getSummary()
        );

        holder.value.setText(
                item.getValue() == null
                        ? ""
                        : item.getValue()
        );

        if (item.getType() == SettingItem.TYPE_SECTION) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(12, 15, 21)
            );

            holder.title.setTextColor(
                    Color.rgb(245, 247, 250)
            );

            holder.title.setTextSize(16);

            holder.summary.setVisibility(
                    View.GONE
            );

            holder.value.setVisibility(
                    View.GONE
            );

        } else {

            holder.itemView.setBackgroundColor(
                    Color.rgb(20, 23, 31)
            );

            holder.title.setTextColor(
                    Color.rgb(245, 247, 250)
            );

            holder.title.setTextSize(15);

            holder.summary.setVisibility(
                    View.VISIBLE
            );

            holder.value.setVisibility(
                    View.VISIBLE
            );

            holder.summary.setTextColor(
                    Color.rgb(165, 172, 183)
            );

            holder.value.setTextColor(
                    Color.rgb(105, 185, 245)
            );
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onSettingClick(
                        data.get(adapterPosition),
                        adapterPosition
                );
            }
        });
    }

    private int dp(
            ViewGroup parent,
            int value
    ) {
        return (int) (
                value
                        * parent.getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    public static class SettingsViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView summary;
        private final TextView value;

        public SettingsViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView summary,
                TextView value
        ) {
            super(itemView);

            this.title = title;
            this.summary = summary;
            this.value = value;
        }
    }
}