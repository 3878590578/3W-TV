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
import java.util.Locale;

public class PlayerBookmarkAdapter
        extends RecyclerView.Adapter<PlayerBookmarkAdapter.ViewHolder> {

    public static class BookmarkItem {

        private final String id;
        private final String title;
        private final long positionMs;
        private final long createTime;
        private boolean enabled;

        public BookmarkItem(
                String id,
                String title,
                long positionMs
        ) {
            this(
                    id,
                    title,
                    positionMs,
                    System.currentTimeMillis(),
                    true
            );
        }

        public BookmarkItem(
                String id,
                String title,
                long positionMs,
                long createTime,
                boolean enabled
        ) {
            this.id = id;
            this.title = title;
            this.positionMs = Math.max(0L, positionMs);
            this.createTime = createTime;
            this.enabled = enabled;
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

        public long getCreateTime() {
            return createTime;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public interface OnBookmarkClickListener {
        void onBookmarkClick(BookmarkItem item);
    }

    private final Context context;
    private final List<BookmarkItem> items = new ArrayList<>();

    private OnBookmarkClickListener listener;

    public PlayerBookmarkAdapter(Context context) {
        this.context = context;
    }

    public void setOnBookmarkClickListener(
            OnBookmarkClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<BookmarkItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(BookmarkItem item) {
        if (item == null) {
            return;
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(String id) {
        if (id == null) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            if (id.equals(items.get(i).getId())) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public BookmarkItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void setEnabled(
            String id,
            boolean enabled
    ) {
        for (int i = 0; i < items.size(); i++) {
            BookmarkItem item = items.get(i);

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
        BookmarkItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            if (listener != null) {
                listener.onBookmarkClick(item);
            }
        });
    }

    private View createView() {
        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);

        layout.setOrientation(
                android.widget.LinearLayout.HORIZONTAL
        );

        layout.setGravity(
                Gravity.CENTER_VERTICAL
        );

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
        time.setTextSize(12);
        time.setGravity(Gravity.CENTER);
        time.setSingleLine(true);

        layout.addView(
                time,
                new android.widget.LinearLayout.LayoutParams(
                        dp(62),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView title = new TextView(context);
        title.setId(android.R.id.text1);
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

        titleParams.leftMargin = dp(9);

        layout.addView(
                title,
                titleParams
        );

        TextView mark = new TextView(context);
        mark.setId(android.R.id.icon);
        mark.setTextSize(16);
        mark.setGravity(Gravity.CENTER);
        mark.setText("◆");

        layout.addView(
                mark,
                new android.widget.LinearLayout.LayoutParams(
                        dp(30),
                        dp(30)
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
        private final TextView mark;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            time = itemView.findViewById(
                    android.R.id.text2
            );

            title = itemView.findViewById(
                    android.R.id.text1
            );

            mark = itemView.findViewById(
                    android.R.id.icon
            );
        }

        public void bind(BookmarkItem item) {
            time.setText(
                    formatTime(item.getPositionMs())
            );

            String titleText = item.getTitle();

            if (titleText == null
                    || titleText.trim().isEmpty()) {
                titleText = "视频标记";
            }

            title.setText(titleText);

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

                mark.setTextColor(
                        Color.rgb(80, 86, 94)
                );

                itemView.setAlpha(0.6f);

            } else {

                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                time.setTextColor(
                        Color.rgb(105, 185, 245)
                );

                title.setTextColor(
                        Color.rgb(205, 210, 218)
                );

                mark.setTextColor(
                        Color.rgb(115, 180, 235)
                );

                itemView.setAlpha(1.0f);
            }

            itemView.setBackground(background);
        }

        private String formatTime(long milliseconds) {
            long totalSeconds =
                    Math.max(0L, milliseconds) / 1000L;

            long hours =
                    totalSeconds / 3600L;

            long minutes =
                    (totalSeconds % 3600L) / 60L;

            long seconds =
                    totalSeconds % 60L;

            if (hours > 0L) {
                return String.format(
                        Locale.getDefault(),
                        "%d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                );
            }

            return String.format(
                    Locale.getDefault(),
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