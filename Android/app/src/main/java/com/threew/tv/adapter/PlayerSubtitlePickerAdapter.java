package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
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
 * 播放器字幕选择器适配器。
 *
 * 用于播放器中统一显示：
 * - 关闭字幕
 * - 自动字幕
 * - 内置字幕
 * - 外挂字幕
 *
 * 与具体字幕加载逻辑解耦，只负责选项展示与选择。
 */
public class PlayerSubtitlePickerAdapter
        extends RecyclerView.Adapter<PlayerSubtitlePickerAdapter.ViewHolder> {

    public static final String TYPE_OFF = "off";
    public static final String TYPE_AUTO = "auto";
    public static final String TYPE_INTERNAL = "internal";
    public static final String TYPE_EXTERNAL = "external";

    public static class Item {

        private final String id;
        private final String title;
        private final String language;
        private final String type;

        private boolean selected;
        private boolean enabled;

        public Item(
                String id,
                String title,
                String language,
                String type
        ) {
            this(
                    id,
                    title,
                    language,
                    type,
                    false,
                    true
            );
        }

        public Item(
                String id,
                String title,
                String language,
                String type,
                boolean selected,
                boolean enabled
        ) {
            this.id = id;
            this.title = title;
            this.language = language;
            this.type = type;
            this.selected = selected;
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
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

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private final Context context;

    private final List<Item> items =
            new ArrayList<>();

    private OnItemClickListener listener;

    public PlayerSubtitlePickerAdapter(
            Context context
    ) {
        this.context = context;
    }

    public void setOnItemClickListener(
            OnItemClickListener listener
    ) {
        this.listener = listener;
    }

    public void setItems(
            List<Item> values
    ) {
        items.clear();

        if (values != null) {
            items.addAll(values);
        }

        notifyDataSetChanged();
    }

    public void addItem(Item item) {
        if (item == null) {
            return;
        }

        items.add(item);

        notifyItemInserted(
                items.size() - 1
        );
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        if (position < 0
                || position >= items.size()) {
            return null;
        }

        return items.get(position);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public Item getSelectedItem() {
        for (Item item : items) {
            if (item.isSelected()) {
                return item;
            }
        }

        return null;
    }

    public void select(String id) {
        for (int i = 0; i < items.size(); i++) {

            Item item = items.get(i);

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

            Item item = items.get(i);

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

            if (id.equals(
                    items.get(i).getId()
            )) {
                return i;
            }
        }

        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        return new ViewHolder(
                createView()
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Item item = items.get(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {

            if (!item.isEnabled()) {
                return;
            }

            select(item.getId());

            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private View createView() {

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
                dp(11),
                dp(12),
                dp(11)
        );

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rootParams.bottomMargin = dp(6);

        root.setLayoutParams(rootParams);

        TextView title =
                new TextView(context);

        title.setId(android.R.id.text1);
        title.setTextSize(14);
        title.setSingleLine(true);
        title.setEllipsize(
                TextUtils.TruncateAt.END
        );
        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        root.addView(
                title,
                titleParams
        );

        TextView language =
                new TextView(context);

        language.setId(android.R.id.text2);
        language.setTextSize(11);
        language.setSingleLine(true);
        language.setGravity(Gravity.CENTER);

        root.addView(
                language,
                new LinearLayout.LayoutParams(
                        dp(62),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView type =
                new TextView(context);

        type.setId(android.R.id.hint);
        type.setTextSize(10);
        type.setSingleLine(true);
        type.setGravity(Gravity.CENTER);

        root.addView(
                type,
                new LinearLayout.LayoutParams(
                        dp(52),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView check =
                new TextView(context);

        check.setId(android.R.id.icon);
        check.setTextSize(17);
        check.setGravity(Gravity.CENTER);

        root.addView(
                check,
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(28)
                )
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

        private final TextView title;
        private final TextView language;
        private final TextView type;
        private final TextView check;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            title = itemView.findViewById(
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

        public void bind(Item item) {

            String titleText =
                    item.getTitle();

            if (TextUtils.isEmpty(titleText)) {
                titleText = "字幕";
            }

            title.setText(titleText);

            String languageText =
                    item.getLanguage();

            if (languageText == null) {
                languageText = "";
            }

            language.setText(
                    languageText
            );

            String typeText =
                    item.getType();

            if (typeText == null) {
                typeText = "";
            }

            type.setText(
                    typeText
            );

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

                title.setTextColor(
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

                itemView.setAlpha(
                        0.55f
                );

            } else if (item.isSelected()) {

                background.setColor(
                        Color.rgb(35, 55, 74)
                );

                title.setTextColor(
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

                itemView.setAlpha(
                        1.0f
                );

            } else {

                background.setColor(
                        Color.rgb(30, 34, 41)
                );

                title.setTextColor(
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

                itemView.setAlpha(
                        1.0f
                );
            }

            itemView.setBackground(
                    background
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
