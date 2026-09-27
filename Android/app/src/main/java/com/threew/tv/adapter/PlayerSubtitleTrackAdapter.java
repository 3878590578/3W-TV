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
 * 播放器字幕轨道适配器。
 *
 * 用于显示当前媒体可用的字幕轨道。
 *
 * 支持：
 * - 关闭字幕
 * - 自动字幕
 * - 多语言字幕
 * - 外挂字幕
 * - 当前选中状态
 */
public class PlayerSubtitleTrackAdapter
        extends RecyclerView.Adapter<PlayerSubtitleTrackAdapter.ViewHolder> {

    public static class SubtitleTrackItem {

        private final String id;
        private final String label;
        private final String language;
        private final String mimeType;
        private boolean selected;
        private boolean enabled;

        public SubtitleTrackItem(
                String id,
                String label,
                String language
        ) {
            this(
                    id,
                    label,
                    language,
                    null,
                    false,
                    true
            );
        }

        public SubtitleTrackItem(
                String id,
                String label,
                String language,
                String mimeType,
                boolean selected,
                boolean enabled
        ) {
            this.id = id;
            this.label = label;
            this.language = language;
            this.mimeType = mimeType;
            this.selected = selected;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getLanguage() {
            return language;
        }

        public String getMimeType() {
            return mimeType;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public interface OnSubtitleTrackClickListener {
        void onSubtitleTrackClick(
                SubtitleTrackItem item
        );
    }

    private final Context context;
    private final List<SubtitleTrackItem> items =
            new ArrayList<>();

    private OnSubtitleTrackClickListener listener;

    public PlayerSubtitleTrackAdapter(Context context) {
        this.context = context;
    }

    public void setOnSubtitleTrackClickListener(
            OnSubtitleTrackClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(
            List<SubtitleTrackItem> values
    ) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(SubtitleTrackItem item) {
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

    public SubtitleTrackItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public void selectTrack(String id) {
        for (int i = 0; i < items.size(); i++) {
            SubtitleTrackItem item = items.get(i);

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
            SubtitleTrackItem item = items.get(i);

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
        SubtitleTrackItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            selectTrack(item.getId());

            if (listener != null) {
                listener.onSubtitleTrackClick(item);
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
                dp(15),
                dp(11),
                dp(15),
                dp(11)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(7);

        layout.setLayoutParams(params);

        TextView label = new TextView(context);
        label.setId(android.R.id.text1);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                labelParams =
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );

        layout.addView(label, labelParams);

        TextView language = new TextView(context);
        language.setId(android.R.id.text2);
        language.setTextSize(11);
        language.setGravity(Gravity.CENTER);
        language.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams
                languageParams =
                new android.widget.LinearLayout.LayoutParams(
                        dp(65),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        layout.addView(language, languageParams);

        TextView check = new TextView(context);
        check.setId(android.R.id.icon);
        check.setTextSize(16);
        check.setGravity(Gravity.CENTER);
        check.setSingleLine(true);

        layout.addView(
                check,
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

        private final TextView label;
        private final TextView language;
        private final TextView check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            label = itemView.findViewById(
                    android.R.id.text1
            );

            language = itemView.findViewById(
                    android.R.id.text2
            );

            check = itemView.findViewById(
                    android.R.id.icon
            );
        }

        public void bind(SubtitleTrackItem item) {
            String labelText = item.getLabel();

            if (labelText == null
                    || labelText.trim().isEmpty()) {
                labelText = "字幕";
            }

            label.setText(labelText);

            String languageText =
                    item.getLanguage();

            if (languageText == null
                    || languageText.trim().isEmpty()) {
                languageText = "";
            }

            language.setText(languageText);

            check.setText(
                    item.isSelected()
                            ? "✓"
                            : ""
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(dp(12));

            if (!item.isEnabled()) {
                background.setColor(
                        Color.rgb(29, 32, 37)
                );

                label.setTextColor(
                        Color.rgb(95, 101, 110)
                );

                language.setTextColor(
                        Color.rgb(82, 88, 96)
                );

                check.setTextColor(
                        Color.rgb(80, 86, 94)
                );

                itemView.setAlpha(0.6f);

            } else if (item.isSelected()) {
                background.setColor(
                        Color.rgb(36, 57, 76)
                );

                label.setTextColor(
                        Color.WHITE
                );

                language.setTextColor(
                        Color.rgb(155, 195, 235)
                );

                check.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                itemView.setAlpha(1.0f);

            } else {
                background.setColor(
                        Color.rgb(30, 35, 43)
                );

                label.setTextColor(
                        Color.rgb(205, 210, 218)
                );

                language.setTextColor(
                        Color.rgb(130, 140, 153)
                );

                check.setTextColor(
                        Color.rgb(105, 175, 230)
                );

                itemView.setAlpha(1.0f);
            }

            itemView.setBackground(background);
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
