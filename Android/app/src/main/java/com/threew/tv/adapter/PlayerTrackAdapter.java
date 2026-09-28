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

public class PlayerTrackAdapter
        extends RecyclerView.Adapter<PlayerTrackAdapter.ViewHolder> {

    public static class TrackItem {

        public static final int TYPE_VIDEO = 0;
        public static final int TYPE_AUDIO = 1;
        public static final int TYPE_SUBTITLE = 2;

        private final String id;
        private final String name;
        private final String language;
        private final int type;
        private final boolean enabled;

        public TrackItem(
                String id,
                String name,
                String language,
                int type,
                boolean enabled
        ) {
            this.id = id;
            this.name = name;
            this.language = language;
            this.type = type;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLanguage() {
            return language;
        }

        public int getType() {
            return type;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public interface OnTrackClickListener {
        void onTrackClick(TrackItem item);
    }

    private final Context context;
    private final List<TrackItem> tracks =
            new ArrayList<>();

    private String selectedId;
    private OnTrackClickListener listener;

    public PlayerTrackAdapter(Context context) {
        this.context = context;
    }

    public void setOnTrackClickListener(
            OnTrackClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(
            List<TrackItem> values
    ) {
        tracks.clear();

        if (values != null) {
            tracks.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(
            TrackItem item
    ) {
        if (item == null) {
            return;
        }

        tracks.add(item);

        notifyItemInserted(
                tracks.size() - 1
        );
    }

    public void clear() {
        tracks.clear();
        selectedId = null;
        notifyDataSetChanged();
    }

    public void setSelectedId(
            String id
    ) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public TrackItem getItem(
            int position
    ) {
        if (position < 0 ||
                position >= tracks.size()) {
            return null;
        }

        return tracks.get(position);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        TextView view =
                new TextView(context);

        view.setTextSize(13);
        view.setGravity(
                Gravity.CENTER_VERTICAL
        );

        view.setSingleLine(true);

        view.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(5);

        view.setLayoutParams(params);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        TrackItem item =
                tracks.get(position);

        StringBuilder text =
                new StringBuilder();

        if (item.getName() != null &&
                !item.getName().trim().isEmpty()) {

            text.append(
                    item.getName()
            );

        } else {

            switch (item.getType()) {

                case TrackItem.TYPE_VIDEO:
                    text.append("视频轨道");
                    break;

                case TrackItem.TYPE_AUDIO:
                    text.append("音频轨道");
                    break;

                case TrackItem.TYPE_SUBTITLE:
                    text.append("字幕轨道");
                    break;

                default:
                    text.append("媒体轨道");
                    break;
            }
        }

        if (item.getLanguage() != null &&
                !item.getLanguage().trim().isEmpty()) {

            text.append(" · ");
            text.append(item.getLanguage());
        }

        holder.text.setText(
                text.toString()
        );

        boolean selected =
                item.getId() != null
                        && item.getId().equals(selectedId);

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(
                dp(10)
        );

        if (selected) {

            background.setColor(
                    Color.rgb(38, 111, 170)
            );

            holder.text.setTextColor(
                    Color.WHITE
            );

        } else if (!item.isEnabled()) {

            background.setColor(
                    Color.rgb(25, 28, 34)
            );

            holder.text.setTextColor(
                    Color.rgb(90, 96, 105)
            );

        } else {

            background.setColor(
                    Color.rgb(20, 23, 31)
            );

            holder.text.setTextColor(
                    Color.rgb(225, 228, 234)
            );
        }

        holder.itemView.setBackground(
                background
        );

        holder.itemView.setOnClickListener(v -> {

            if (!item.isEnabled()) {
                return;
            }

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            selectedId =
                    tracks.get(adapterPosition)
                            .getId();

            notifyDataSetChanged();

            if (listener != null) {
                listener.onTrackClick(
                        tracks.get(adapterPosition)
                );
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

        private final TextView text;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            text = (TextView) itemView;
        }
    }
}