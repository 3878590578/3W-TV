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

import com.threew.tv.model.History;

import java.util.ArrayList;
import java.util.List;

public class PlayerHistoryAdapter
        extends RecyclerView.Adapter<PlayerHistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(History history);
    }

    private final Context context;
    private final List<History> items = new ArrayList<>();
    private OnHistoryClickListener listener;

    public PlayerHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setOnHistoryClickListener(
            OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<History> list) {
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

    public History getItem(int position) {
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
            int viewType) {

        LinearLayout root =
                new LinearLayout(parent.getContext());

        root.setOrientation(
                LinearLayout.VERTICAL);

        root.setGravity(
                Gravity.CENTER_VERTICAL);

        root.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12));

        root.setBackgroundColor(
                Color.rgb(25, 30, 38));

        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        History item = items.get(position);

        holder.title.setText(
                safe(item.getVideoName(), "未命名视频"));

        String episode =
                item.getEpisodeName();

        if (episode == null ||
                episode.trim().isEmpty()) {

            episode = "继续播放";
        }

        holder.subtitle.setText(
                episode);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryClick(item);
            }
        });
    }

    private String safe(
            String value,
            String fallback) {

        if (value == null ||
                value.trim().isEmpty()) {

            return fallback;
        }

        return value;
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

        final TextView title;
        final TextView subtitle;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            LinearLayout root =
                    (LinearLayout) itemView;

            title = new TextView(
                    itemView.getContext());

            title.setTextColor(
                    Color.WHITE);

            title.setTextSize(15);

            subtitle = new TextView(
                    itemView.getContext());

            subtitle.setTextColor(
                    Color.rgb(135, 145, 160));

            subtitle.setTextSize(12);

            root.addView(
                    title,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2));

            root.addView(
                    subtitle,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2));
        }
    }
}