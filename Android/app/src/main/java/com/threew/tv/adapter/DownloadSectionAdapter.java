package com.threew.tv.adapter;

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
 * 下载分组适配器。
 *
 * 用于下载页面顶部：
 * - 全部
 * - 下载中
 * - 等待中
 * - 已暂停
 * - 已完成
 * - 失败
 *
 * 支持手机、平板以及 Android TV 遥控器焦点操作。
 */
public class DownloadSectionAdapter
        extends RecyclerView.Adapter<DownloadSectionAdapter.SectionViewHolder> {

    public interface OnSectionClickListener {
        void onSectionClick(
                int section,
                String title
        );
    }

    public static class Section {

        private final int id;
        private final String title;
        private int count;

        public Section(
                int id,
                String title
        ) {
            this.id = id;
            this.title = title;
        }

        public Section(
                int id,
                String title,
                int count
        ) {
            this.id = id;
            this.title = title;
            this.count = count;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public int getCount() {
            return count;
        }

        public void setCount(
                int count
        ) {
            this.count = Math.max(
                    0,
                    count
            );
        }
    }

    private final List<Section> sections =
            new ArrayList<>();

    private final OnSectionClickListener listener;

    private int selectedSection = 0;

    public DownloadSectionAdapter(
            OnSectionClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<Section> data
    ) {

        sections.clear();

        if (data != null) {
            sections.addAll(data);
        }

        if (sections.isEmpty()) {

            selectedSection = -1;

        } else {

            boolean exists = false;

            for (Section section : sections) {

                if (section.getId() ==
                        selectedSection) {

                    exists = true;
                    break;
                }
            }

            if (!exists) {
                selectedSection =
                        sections.get(0).getId();
            }
        }

        notifyDataSetChanged();
    }

    public void updateCount(
            int sectionId,
            int count
    ) {

        for (int i = 0;
             i < sections.size();
             i++) {

            Section section =
                    sections.get(i);

            if (section.getId() ==
                    sectionId) {

                section.setCount(count);

                notifyItemChanged(i);

                return;
            }
        }
    }

    public void setSelectedSection(
            int sectionId
    ) {

        if (selectedSection ==
                sectionId) {

            return;
        }

        int oldPosition =
                findPosition(
                        selectedSection
                );

        int newPosition =
                findPosition(
                        sectionId
                );

        selectedSection =
                sectionId;

        if (oldPosition >= 0) {

            notifyItemChanged(
                    oldPosition
            );
        }

        if (newPosition >= 0) {

            notifyItemChanged(
                    newPosition
            );
        }
    }

    public int getSelectedSection() {
        return selectedSection;
    }

    public Section getItem(
            int position
    ) {

        if (position < 0 ||
                position >= sections.size()) {

            return null;
        }

        return sections.get(position);
    }

    public List<Section> getItems() {
        return new ArrayList<>(sections);
    }

    private int findPosition(
            int sectionId
    ) {

        for (int i = 0;
             i < sections.size();
             i++) {

            if (sections.get(i).getId() ==
                    sectionId) {

                return i;
            }
        }

        return -1;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        TextView text =
                new TextView(
                        parent.getContext()
                );

        text.setGravity(
                Gravity.CENTER
        );

        text.setTextSize(12);

        text.setSingleLine(true);

        text.setPadding(
                dp(parent, 14),
                dp(parent, 7),
                dp(parent, 14),
                dp(parent, 7)
        );

        text.setFocusable(true);
        text.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(parent, 40)
                );

        params.setMargins(
                dp(parent, 3),
                dp(parent, 4),
                dp(parent, 3),
                dp(parent, 4)
        );

        text.setLayoutParams(params);

        return new SectionViewHolder(
                text
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SectionViewHolder holder,
            int position
    ) {

        Section section =
                sections.get(position);

        String title =
                section.getTitle();

        if (section.getCount() > 0) {

            title +=
                    "  " +
                    section.getCount();
        }

        holder.text.setText(
                title
        );

        boolean selected =
                section.getId() ==
                        selectedSection;

        applyBackground(
                holder.text,
                selected,
                false
        );

        holder.text.setTextColor(
                selected
                        ? Color.rgb(
                                235,
                                249,
                                255
                        )
                        : Color.rgb(
                                155,
                                165,
                                178
                        )
        );

        holder.text.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    int currentPosition =
                            holder.getBindingAdapterPosition();

                    if (currentPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    Section current =
                            sections.get(
                                    currentPosition
                            );

                    if (hasFocus) {

                        applyBackground(
                                holder.text,
                                current.getId() ==
                                        selectedSection,
                                true
                        );

                        holder.text.animate()
                                .scaleX(1.04f)
                                .scaleY(1.04f)
                                .setDuration(100)
                                .start();

                    } else {

                        applyBackground(
                                holder.text,
                                current.getId() ==
                                        selectedSection,
                                false
                        );

                        holder.text.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    }
                }
        );

        holder.text.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    Section clicked =
                            sections.get(
                                    adapterPosition
                            );

                    selectedSection =
                            clicked.getId();

                    notifyDataSetChanged();

                    if (listener != null) {

                        listener.onSectionClick(
                                clicked.getId(),
                                clicked.getTitle()
                        );
                    }
                }
        );
    }

    private void applyBackground(
            TextView view,
            boolean selected,
            boolean focused
    ) {

        int fill;
        int stroke;

        if (selected) {

            fill =
                    focused
                            ? Color.rgb(
                                    40,
                                    82,
                                    96
                            )
                            : Color.rgb(
                                    30,
                                    68,
                                    82
                            );

            stroke =
                    Color.rgb(
                            75,
                            175,
                            205
                    );

        } else if (focused) {

            fill =
                    Color.rgb(
                            38,
                            47,
                            58
                    );

            stroke =
                    Color.rgb(
                            75,
                            105,
                            120
                    );

        } else {

            fill =
                    Color.rgb(
                            25,
                            29,
                            36
                    );

            stroke =
                    Color.rgb(
                            55,
                            63,
                            73
                    );
        }

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(fill);

        drawable.setStroke(
                dpValue(
                        view,
                        1
                ),
                stroke
        );

        drawable.setCornerRadius(
                dpValue(
                        view,
                        9
                )
        );

        view.setBackground(
                drawable
        );
    }

    private int dpValue(
            View view,
            int value
    ) {

        return Math.round(
                value *
                        view.getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private int dp(
            ViewGroup parent,
            int value
    ) {

        return Math.round(
                value *
                        parent.getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class SectionViewHolder
            extends RecyclerView.ViewHolder {

        final TextView text;

        SectionViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            text =
                    (TextView) itemView;
        }
    }
}
