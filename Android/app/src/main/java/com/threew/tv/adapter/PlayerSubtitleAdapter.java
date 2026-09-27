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
 * 播放器字幕轨道选择适配器。
 *
 * 用于播放器字幕菜单。
 *
 * 支持：
 * - 关闭字幕
 * - 自动字幕
 * - 外部字幕
 * - 多字幕轨道选择
 *
 * 本类只负责字幕选项展示，
 * 实际字幕切换由 PlayerActivity / PlayerController 处理。
 */
public class PlayerSubtitleAdapter
        extends RecyclerView.Adapter<PlayerSubtitleAdapter.ViewHolder> {

    public interface OnSubtitleClickListener {
        void onSubtitleClick(SubtitleItem item);
    }

    public static class SubtitleItem {

        private final String id;
        private final String name;
        private final String language;
        private final boolean enabled;

        public SubtitleItem(
                String id,
                String name,
                String language,
                boolean enabled
        ) {
            this.id = id;
            this.name = name;
            this.language = language;
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

        public boolean isEnabled() {
            return enabled;
        }
    }

    private final Context context;
    private final List<SubtitleItem> subtitles = new ArrayList<>();

    private String selectedId = "off";

    private OnSubtitleClickListener listener;

    public PlayerSubtitleAdapter(Context context) {
        this.context = context;
    }

    public void setOnSubtitleClickListener(
            OnSubtitleClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(List<SubtitleItem> values) {
        subtitles.clear();

        if (values != null) {
            subtitles.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(SubtitleItem item) {
        if (item == null) {
            return;
        }

        subtitles.add(item);
        notifyItemInserted(subtitles.size() - 1);
    }

    public void clear() {
        subtitles.clear();
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        if (id == null || id.trim().isEmpty()) {
            selectedId = "off";
        } else {
            selectedId = id;
        }

        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public SubtitleItem getItem(int position) {
        if (position < 0 || position >= subtitles.size()) {
            return null;
        }

        return subtitles.get(position);
    }

    @Override
    public int getItemCount() {
        return subtitles.size();
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
        SubtitleItem item = subtitles.get(position);

        boolean selected =
                item.getId().equals(selectedId);

        holder.bind(item, selected);

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();

            if (listener != null) {
                listener.onSubtitleClick(item);
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
                SubtitleItem item,
                boolean selected
        ) {
            StringBuilder text =
                    new StringBuilder();

            String name = item.getName();

            if (name == null
                    || name.trim().isEmpty()) {
                name = "字幕";
            }

            text.append(name);

            String language = item.getLanguage();

            if (language != null
                    && !language.trim().isEmpty()) {
                text.append("  ·  ");
                text.append(language);
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
            } else if (!item.isEnabled()) {
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
