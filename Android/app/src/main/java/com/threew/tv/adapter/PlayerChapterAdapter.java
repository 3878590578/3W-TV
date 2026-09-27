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
 * 播放器章节/时间点适配器。
 *
 * 用于显示视频章节、片头、片尾以及用户记录的时间点。
 */
public class PlayerChapterAdapter
        extends RecyclerView.Adapter<PlayerChapterAdapter.ViewHolder> {

    public static class ChapterItem {

        private final String id;
        private final String title;
        private final long positionMs;
        private final long durationMs;
        private boolean enabled;
        private boolean selected;

        public ChapterItem(
                String id,
                String title,
                long positionMs
        ) {
            this(
                    id,
                    title,
                    positionMs,
                    0L,
                    true,
                    false
            );
        }

        public ChapterItem(
                String id,
                String title,
                long positionMs,
                long durationMs,
                boolean enabled,
                boolean selected
        ) {
            this.id = id;
            this.title = title;
            this.positionMs = Math.max(0L, positionMs);
            this.durationMs = Math.max(0L, durationMs);
            this.enabled = enabled;
            this.selected = selected;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public long getPositionMs() {
            return positionMs;
        }

        public long getDurationMs() {
            return durationMs;
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

    public interface OnChapterClickListener {
        void onChapterClick(ChapterItem item);
    }

    private final Context context;
    private final List<ChapterItem> items = new ArrayList<>();

    private OnChapterClickListener listener;

    public PlayerChapterAdapter(Context context) {
        this.context = context;
    }

    public void setOnChapterClickListener(
            OnChapterClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<ChapterItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(ChapterItem item) {
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

    public ChapterItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void selectChapter(String id) {
        for (int i = 0; i < items.size(); i++) {
            ChapterItem item = items.get(i);

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
            ChapterItem item = items.get(i);

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
        ChapterItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            selectChapter(item.getId());

            if (listener != null) {
                listener.onChapterClick(item);
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
                dp(14),
                dp(11),
                dp(14),
                dp(11)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(7);

        layout.setLayoutParams(params);

        TextView time = new TextView(context);
        time.setId(android.R.id.text2);
        time.setTextSize(11);
        time.setGravity(Gravity.CENTER);
        time.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                timeParams =
                new android.widget.LinearLayout.LayoutParams(
                        dp(58),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        layout.addView(time, timeParams);

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

        titleParams.leftMargin = dp(8);

        layout.addView(title, titleParams);

        TextView duration = new TextView(context);
        duration.setId(android.R.id.icon);
        duration.setTextSize(10);
        duration.setGravity(Gravity.CENTER);
        duration.setSingleLine(true);

        layout.addView(
                duration,
                new android.widget.LinearLayout.LayoutParams(
                        dp(55),
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

        private final TextView time;
        private final TextView title;
        private final TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(
                    android.R.id.text2
            );

            title = itemView.findViewById(
                    android.R.id.text1
            );

            duration = itemView.findViewById(
                    android.R.id.icon
            );
        }

        public void bind(ChapterItem item) {
            time.setText(
                    formatTime(item.getPositionMs())
            );

            title.setText(item.getTitle());

            if (item.getDurationMs() > 0L) {
                duration.setText(
                        formatTime(item.getDurationMs())
                );
            } else {
                duration.setText("");
            }

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(29, 32, 37)
                );

                time.setTextColor(
                        Color.rgb(88, 94, 102)
                );

                title.setTextColor(
                        Color.rgb(95, 101, 110)
                );

                duration.setTextColor(
                        Color.rgb(82, 88, 96)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(36, 57, 76)
                );

                time.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                title.setTextColor(
                        Color.WHITE
                );

                duration.setTextColor(
                        Color.rgb(150, 195, 235)
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                time.setTextColor(
                        Color.rgb(125, 175, 220)
                );

                title.setTextColor(
                        Color.rgb(205, 210, 218)
                );

                duration.setTextColor(
                        Color.rgb(125, 135, 148)
                );

                itemView.setAlpha(1.0f);
            }

            itemView.setBackground(background);
        }

        private String formatTime(long milliseconds) {
            long totalSeconds =
                    Math.max(0L, milliseconds) / 1000L;

            long hours = totalSeconds / 3600L;
            long minutes =
                    (totalSeconds % 3600L) / 60L;
            long seconds =
                    totalSeconds % 60L;

            if (hours > 0L) {
                return String.format(
                        java.util.Locale.getDefault(),
                        "%d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                );
            }

            return String.format(
                    java.util.Locale.getDefault(),
                    "%02d:%02d",
                    minutes,
                    seconds
            );
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
