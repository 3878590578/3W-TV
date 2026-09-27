package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放器字幕列表适配器。
 *
 * 用于播放器字幕菜单中的字幕选项。
 *
 * 支持：
 * - 关闭字幕
 * - 自动字幕
 * - 内置字幕
 * - 外挂字幕
 * - 多语言字幕
 * - 当前字幕高亮
 */
public class PlayerSubtitleListAdapter
        extends RecyclerView.Adapter<PlayerSubtitleListAdapter.ViewHolder> {

    public static final String ID_OFF = "subtitle_off";
    public static final String ID_AUTO = "subtitle_auto";

    public static class SubtitleItem {

        private final String id;
        private final String name;
        private final String language;
        private final String type;
        private boolean selected;
        private boolean enabled;

        public SubtitleItem(
                String id,
                String name,
                String language,
                String type
        ) {
            this(
                    id,
                    name,
                    language,
                    type,
                    false,
                    true
            );
        }

        public SubtitleItem(
                String id,
                String name,
                String language,
                String type,
                boolean selected,
                boolean enabled
        ) {
            this.id = id;
            this.name = name;
            this.language = language;
            this.type = type;
            this.selected = selected;
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

        public String getType() {
            return type;
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

    public interface OnSubtitleClickListener {
        void onSubtitleClick(SubtitleItem item);
    }

    private final Context context;

    private final List<SubtitleItem> items =
            new ArrayList<>();

    private OnSubtitleClickListener listener;

    public PlayerSubtitleListAdapter(Context context) {
        this.context = context;
    }

    public void setOnSubtitleClickListener(
            OnSubtitleClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(
            List<SubtitleItem> list
    ) {
        items.clear();

        if (list != null) {
            items.addAll(list);
        }

        notifyDataSetChanged();
    }

    public void addItem(SubtitleItem item) {
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

    public SubtitleItem getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public SubtitleItem getSelectedItem() {
        for (SubtitleItem item : items) {
            if (item.isSelected()) {
                return item;
            }
        }

        return null;
    }

    public void select(String id) {
        for (int i = 0; i < items.size(); i++) {
            SubtitleItem item = items.get(i);

            boolean selected =
                    id != null
                            && id.equals(item.getId());

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
        if (id == null) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            SubtitleItem item = items.get(i);

            if (id.equals(item.getId())) {
                item.setEnabled(enabled);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public int findPosition(String id) {
        if (id == null) {
            return -1;
        }

        for (int i = 0; i < items.size(); i++) {
            if (id.equals(items.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    public List<SubtitleItem> getItems() {
        return new ArrayList<>(items);
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
        return new ViewHolder(createItemView());
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        SubtitleItem item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (!item.isEnabled()) {
                return;
            }

            select(item.getId());

            if (listener != null) {
                listener.onSubtitleClick(item);
            }
        });
    }

    private View createItemView() {
        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(14),
                dp(10),
                dp(12),
                dp(10)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(6);

        root.setLayoutParams(params);

        TextView name =
                new TextView(context);

        name.setId(android.R.id.text1);
        name.setTextSize(14);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setSingleLine(true);

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        root.addView(name, nameParams);

        TextView language =
                new TextView(context);

        language.setId(android.R.id.text2);
        language.setTextSize(11);
        language.setGravity(Gravity.CENTER);
        language.setSingleLine(true);

        LinearLayout.LayoutParams languageParams =
                new LinearLayout.LayoutParams(
                        dp(62),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        root.addView(
                language,
                languageParams
        );

        TextView type =
                new TextView(context);

        type.setId(android.R.id.hint);
        type.setTextSize(10);
        type.setGravity(Gravity.CENTER);
        type.setSingleLine(true);

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(
                        dp(52),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        root.addView(
                type,
                typeParams
        );

        TextView check =
                new TextView(context);

        check.setId(android.R.id.icon);
        check.setTextSize(17);
        check.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams checkParams =
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(28)
                );

        root.addView(
                check,
                checkParams
        );

        return root;
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

        private final TextView name;
        private final TextView language;
        private final TextView type;
        private final TextView check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(
                    android.R.id.text1
            );

            language = itemView.findViewById(
                    android.R.id.text2
            );

            type = itemView.findViewById(
                    android.R.id.hint
            );

            check = itemView.findViewById(
                    android.R.id.icon
            );
        }

        public void bind(SubtitleItem item) {
            String nameText = item.getName();

            if (nameText == null
                    || nameText.trim().isEmpty()) {

                nameText = "字幕";
            }

            name.setText(nameText);

            String languageText =
                    item.getLanguage();

            if (languageText == null) {
                languageText = "";
            }

            language.setText(languageText);

            String typeText =
                    item.getType();

            if (typeText == null) {
                typeText = "";
            }

            type.setText(typeText);

            check.setText(
                    item.isSelected()
                            ? "✓"
                            : ""
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setCornerRadius(
                    dp(11)
            );

            if (!item.isEnabled()) {

                background.setColor(
                        Color.rgb(27, 30, 35)
                );

                name.setTextColor(
                        Color.rgb(92, 98, 106)
                );

                language.setTextColor(
                        Color.rgb(76, 82, 90)
                );

                type.setTextColor(
                        Color.rgb(72, 78, 86)
                );

                check.setTextColor(
                        Color.rgb(70, 76, 84)
                );

                itemView.setAlpha(0.55f);

            } else if (item.isSelected()) {

                background.setColor(
                        Color.rgb(35, 55, 74)
                );

                name.setTextColor(
                        Color.WHITE
                );

                language.setTextColor(
                        Color.rgb(155, 194, 232)
                );

                type.setTextColor(
                        Color.rgb(130, 177, 218)
                );

                check.setTextColor(
                        Color.rgb(105, 190, 255)
                );

                itemView.setAlpha(1.0f);

            } else {

                background.setColor(
                        Color.rgb(30, 34, 41)
                );

                name.setTextColor(
                        Color.rgb(210, 214, 220)
                );

                language.setTextColor(
                        Color.rgb(132, 141, 152)
                );

                type.setTextColor(
                        Color.rgb(112, 121, 132)
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
