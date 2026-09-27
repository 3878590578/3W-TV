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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放器播放源选择适配器。
 *
 * 用于同一影片存在多个播放源时，
 * 在播放器中快速切换播放源。
 */
public class PlayerSourceAdapter
        extends RecyclerView.Adapter<PlayerSourceAdapter.ViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(VideoSource source);
    }

    private final Context context;
    private final List<VideoSource> sources = new ArrayList<>();

    private long selectedSourceId = -1L;

    private OnSourceClickListener listener;

    public PlayerSourceAdapter(Context context) {
        this.context = context;
    }

    public void setOnSourceClickListener(
            OnSourceClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSources(List<VideoSource> values) {
        sources.clear();

        if (values != null) {
            sources.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void setSelectedSourceId(long sourceId) {
        selectedSourceId = sourceId;
        notifyDataSetChanged();
    }

    public long getSelectedSourceId() {
        return selectedSourceId;
    }

    public VideoSource getItem(int position) {
        if (position < 0 || position >= sources.size()) {
            return null;
        }

        return sources.get(position);
    }

    public void clear() {
        sources.clear();
        selectedSourceId = -1L;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        TextView textView = new TextView(context);

        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setSingleLine(true);

        textView.setPadding(
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

        params.bottomMargin = dp(8);

        textView.setLayoutParams(params);

        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        VideoSource source = sources.get(position);

        boolean selected =
                source.getId() == selectedSourceId;

        holder.bind(source, selected);

        holder.itemView.setOnClickListener(v -> {
            selectedSourceId = source.getId();
            notifyDataSetChanged();

            if (listener != null) {
                listener.onSourceClick(source);
            }
        });
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

        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(
                VideoSource source,
                boolean selected
        ) {
            String name = source.getDisplayName();

            if (name == null || name.trim().isEmpty()) {
                name = "未知播放源";
            }

            textView.setText(name);

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(10));

            if (selected) {
                background.setColor(
                        Color.rgb(48, 112, 235)
                );

                textView.setTextColor(Color.WHITE);
            } else if (!source.isEnabled()) {
                background.setColor(
                        Color.rgb(35, 35, 38)
                );

                textView.setTextColor(
                        Color.rgb(120, 125, 132)
                );
            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                textView.setTextColor(
                        Color.rgb(210, 214, 222)
                );
            }

            textView.setBackground(background);
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
