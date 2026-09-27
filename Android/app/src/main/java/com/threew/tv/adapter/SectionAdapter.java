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
 * 首页区块标题适配器。
 *
 * 用于展示：
 * - 最近观看
 * - 热门视频
 * - 最新更新
 * - 本地视频
 * - 收藏
 * - 各来源推荐
 *
 * 每个区块可以带一个“更多”按钮。
 */
public class SectionAdapter
        extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    public interface OnSectionClickListener {
        void onSectionClick(
                String title,
                int position
        );
    }

    public interface OnMoreClickListener {
        void onMoreClick(
                String title,
                int position
        );
    }

    private final List<String> sections =
            new ArrayList<>();

    private final OnSectionClickListener sectionListener;
    private final OnMoreClickListener moreListener;

    public SectionAdapter(
            OnSectionClickListener sectionListener,
            OnMoreClickListener moreListener
    ) {

        this.sectionListener =
                sectionListener;

        this.moreListener =
                moreListener;
    }

    public void setData(
            List<String> data
    ) {

        sections.clear();

        if (data != null) {
            sections.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void add(
            String title
    ) {

        if (title == null ||
                title.trim().isEmpty()) {

            return;
        }

        sections.add(
                title.trim()
        );

        notifyItemInserted(
                sections.size() - 1
        );
    }

    public void clear() {

        int count =
                sections.size();

        if (count == 0) {
            return;
        }

        sections.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public String getItem(
            int position
    ) {

        if (position < 0 ||
                position >= sections.size()) {

            return "";
        }

        return sections.get(position);
    }

    public List<String> getItems() {
        return new ArrayList<>(sections);
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LinearLayout root =
                new LinearLayout(
                        parent.getContext()
                );

        root.setOrientation(
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 4),
                dp(parent, 2),
                dp(parent, 4),
                dp(parent, 2)
        );

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 48)
                );

        root.setLayoutParams(
                rootParams
        );

        View indicator =
                new View(
                        parent.getContext()
                );

        indicator.setBackground(
                createIndicator()
        );

        root.addView(
                indicator,
                new LinearLayout.LayoutParams(
                        dp(parent, 3),
                        dp(parent, 25)
                )
        );

        TextView title =
                new TextView(
                        parent.getContext()
                );

        title.setTextSize(17);

        title.setTextColor(
                Color.rgb(
                        235,
                        240,
                        246
                )
        );

        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        title.setSingleLine(true);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );

        titleParams.setMargins(
                dp(parent, 10),
                0,
                dp(parent, 8),
                0
        );

        root.addView(
                title,
                titleParams
        );

        TextView more =
                new TextView(
                        parent.getContext()
                );

        more.setText(
                "更多  ›"
        );

        more.setTextSize(11);

        more.setGravity(
                Gravity.CENTER
        );

        more.setTextColor(
                Color.rgb(
                        125,
                        185,
                        205
                )
        );

        more.setPadding(
                dp(parent, 8),
                dp(parent, 4),
                dp(parent, 8),
                dp(parent, 4)
        );

        more.setFocusable(true);
        more.setClickable(true);

        root.addView(
                more,
                new LinearLayout.LayoutParams(
                        dp(parent, 66),
                        dp(parent, 34)
                )
        );

        return new SectionViewHolder(
                root,
                title,
                more
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SectionViewHolder holder,
            int position
    ) {

        String title =
                sections.get(position);

        holder.title.setText(
                title
        );

        holder.more.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    if (moreListener != null) {

                        moreListener.onMoreClick(
                                sections.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );

        holder.more.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackground(
                                createMoreBackground(
                                        true
                                )
                        );

                        v.animate()
                                .scaleX(1.04f)
                                .scaleY(1.04f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackground(
                                createMoreBackground(
                                        false
                                )
                        );

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    }
                }
        );

        holder.itemView.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    if (sectionListener != null) {

                        sectionListener.onSectionClick(
                                sections.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );
    }

    private GradientDrawable createIndicator() {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                Color.rgb(
                        80,
                        190,
                        220
                )
        );

        drawable.setCornerRadius(
                3f
        );

        return drawable;
    }

    private GradientDrawable createMoreBackground(
            boolean focused
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                focused
                        ? Color.rgb(
                                35,
                                52,
                                62
                        )
                        : Color.TRANSPARENT
        );

        drawable.setStroke(
                dpValue(1),
                focused
                        ? Color.rgb(
                                80,
                                150,
                                175
                        )
                        : Color.rgb(
                                55,
                                65,
                                75
                        )
        );

        drawable.setCornerRadius(
                dpValue(8)
        );

        return drawable;
    }

    private int dpValue(
            int value
    ) {

        return Math.round(
                value *
                        1f
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

        final TextView title;
        final TextView more;

        SectionViewHolder(
                @NonNull View itemView,
                TextView title,
                TextView more
        ) {

            super(itemView);

            this.title = title;
            this.more = more;
        }
    }
}
