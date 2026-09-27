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
 * 播放器轨道选择适配器。
 *
 * 统一处理播放器中的媒体轨道选择：
 * - 视频轨道
 * - 音频轨道
 * - 字幕轨道
 *
 * 本类只负责展示轨道列表和当前选中状态。
 */
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
    private final List<TrackItem> tracks = new ArrayList<>();

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

    public void setItems(List<TrackItem> values) {
        tracks.clear();

        if (values != null) {
            tracks.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(TrackItem item) {
        if (item == null) {
            return;
        }

        tracks.add(item);
        notifyItemInserted(tracks.size() - 1);
    }

    public void clear() {
        tracks.clear();
        selectedId = null;
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public TrackItem getItem(int position) {
        if (position < 0 || position >= tracks.size()) {
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
    )
