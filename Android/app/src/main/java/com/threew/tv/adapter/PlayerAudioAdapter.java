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
 * 播放器音轨选择适配器。
 *
 * 用于播放器音频轨道菜单。
 *
 * 支持：
 * - 普通音轨
 * - 多语言音轨
 * - 多声道音轨
 * - 当前音轨高亮
 *
 * 实际音轨切换由播放器控制层负责。
 */
public class PlayerAudioAdapter
        extends RecyclerView.Adapter<PlayerAudioAdapter.ViewHolder> {

    public interface OnAudioClickListener {
        void onAudioClick(AudioItem item);
    }

    public static class AudioItem {

        private final String id;
        private final String name;
        private final String language;
        private final String channel;

        public AudioItem(
                String id,
                String name,
                String language,
                String channel
        ) {
            this.id = id;
            this.name = name;
            this.language = language;
            this.channel = channel;
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

        public String getChannel() {
            return channel;
        }
    }

    private final Context context;
    private final List<AudioItem> items = new ArrayList<>();

    private String selectedId;

    private OnAudioClickListener listener;

    public PlayerAudioAdapter(Context context) {
        this.context = context;
    }

    public void setOnAudioClickListener(
            OnAudioClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<AudioItem> values) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(AudioItem item) {
        if (item == null) {
            return;
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void clear() {
        items.clear();
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

    public AudioItem getItem(int position) {
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
        TextView textView = new TextView(context);

        textView.setGravity(Gravity.CENTER_VERTICAL);
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
        AudioItem item = items.get(position);

        boolean selected =
                item.getId() != null
                        && item.getId().equals(selectedId);

        holder.bind(item, selected);

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();

            if (listener != null) {
                listener.onAudioClick(item);
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
                AudioItem item,
                boolean selected
        ) {
            StringBuilder text =
                    new StringBuilder();

            String name = item.getName();

            if (name == null
                    || name.trim().isEmpty()) {
                name = "音轨";
            }

            text.append(name);

            String language = item.getLanguage();

            if (language != null
                    && !language.trim().isEmpty()) {
                text.append("  ·  ");
                text.append(language);
            }

            String channel = item.getChannel();

            if (channel != null
                    && !channel.trim().isEmpty()) {
                text.append("  ·  ");
                text.append(channel);
            }

            textView.setText(text.toString());

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(10));

            if (selected) {
                background.setColor(
                        Color.rgb(48, 112, 235)
                );

                textView.setTextColor(Color.WHITE);
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
