package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlayerSubtitleFileAdapter
        extends RecyclerView.Adapter<PlayerSubtitleFileAdapter.ViewHolder> {

    public static class SubtitleFileItem {

        private final String id;
        private final String name;
        private final String uri;
        private boolean selected;

        public SubtitleFileItem(
                String id,
                String name,
                String uri) {

            this.id = id;
            this.name = name;
            this.uri = uri;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUri() {
            return uri;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(
                boolean selected) {

            this.selected = selected;
        }
    }

    public interface OnSubtitleFileClickListener {
        void onSubtitleFileClick(
                SubtitleFileItem item);
    }

    private final Context context;
    private final List<SubtitleFileItem> items =
            new ArrayList<>();

    private OnSubtitleFileClickListener listener;

    public PlayerSubtitleFileAdapter(
            Context context) {

        this.context = context;
    }

    public void setListener(
            OnSubtitleFileClickListener listener) {

        this.listener = listener;
    }

    public void setOnSubtitleFileClickListener(
            OnSubtitleFileClickListener listener) {

        this.listener = listener;
    }

    public void setItems(
            List<SubtitleFileItem> list) {

        items.clear();

        if (list != null) {
            items.addAll(list);
        }

        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return items.size();
    }

    public SubtitleFileItem getItem(
            int position) {

        if (position < 0 ||
                position >= items.size()) {

            return null;
        }

        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        LinearLayout root =
                new LinearLayout(parent.getContext());

        root.setGravity(
                Gravity.CENTER_VERTICAL);

        root.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12));

        root.setBackgroundColor(
                Color.rgb(28, 33, 42));

        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        SubtitleFileItem item =
                items.get(position);

        holder.name.setText(
                item.getName() == null
                        ? "字幕文件"
                        : item.getName());

        holder.check.setText(
                item.isSelected()
                        ? "✓"
                        : "");

        holder.itemView.setOnClickListener(v -> {

            for (SubtitleFileItem value : items) {
                value.setSelected(
                        value == item);
            }

            notifyDataSetChanged();

            if (listener != null) {
                listener.onSubtitleFileClick(item);
            }
        });
    }

    private int dp(int value) {
        return (int) (
                value *
                        context.getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView check;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            LinearLayout root =
                    (LinearLayout) itemView;

            name = new TextView(
                    itemView.getContext());

            name.setTextColor(
                    Color.WHITE);

            name.setTextSize(14);

            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
                            1f);

            root.addView(
                    name,
                    nameParams);

            check = new TextView(
                    itemView.getContext());

            check.setTextColor(
                    Color.rgb(105, 190, 255));

            check.setTextSize(17);

            check.setGravity(
                    Gravity.CENTER);

            root.addView(
                    check,
                    new LinearLayout.LayoutParams(
                            dpStatic(itemView, 30),
                            dpStatic(itemView, 30)));
        }

        private static int dpStatic(
                View view,
                int value) {

            return (int) (
                    value *
                            view.getResources()
                                    .getDisplayMetrics()
                                    .density
                            + 0.5f
            );
        }
    }
}