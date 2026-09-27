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
 * 分类适配器。
 *
 * 用于：
 * - 首页分类
 * - 搜索分类
 * - 视频分类筛选
 * - TV 遥控器操作
 */
public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(
                String category,
                int position
        );
    }

    private final List<String> categories =
            new ArrayList<>();

    private final OnCategoryClickListener listener;

    private int selectedPosition = 0;

    public CategoryAdapter(
            OnCategoryClickListener listener
    ) {
        this.listener = listener;
    }

    public void setData(
            List<String> data
    ) {

        categories.clear();

        if (data != null) {
            categories.addAll(data);
        }

        if (categories.isEmpty()) {
            selectedPosition = -1;
        } else if (selectedPosition < 0 ||
                selectedPosition >= categories.size()) {
            selectedPosition = 0;
        }

        notifyDataSetChanged();
    }

    public void setSelectedPosition(
            int position
    ) {

        if (position < 0 ||
                position >= categories.size()) {

            return;
        }

        int old =
                selectedPosition;

        selectedPosition =
                position;

        if (old >= 0 &&
                old < categories.size()) {

            notifyItemChanged(old);
        }

        notifyItemChanged(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedCategory() {

        if (selectedPosition < 0 ||
                selectedPosition >= categories.size()) {

            return "";
        }

        return categories.get(
                selectedPosition
        );
    }

    public List<String> getItems() {
        return new ArrayList<>(categories);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
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

        text.setTextSize(13);

        text.setSingleLine(true);

        text.setPadding(
                dp(parent, 18),
                dp(parent, 8),
                dp(parent, 18),
                dp(parent, 8)
        );

        text.setFocusable(true);
        text.setClickable(true);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(parent, 42)
                );

        params.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        text.setLayoutParams(params);

        return new CategoryViewHolder(
                text
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position
    ) {

        String category =
                categories.get(position);

        holder.text.setText(
                category
        );

        boolean selected =
                position == selectedPosition;

        applyBackground(
                holder.text,
                selected
        );

        holder.text.setTextColor(
                selected
                        ? Color.rgb(
                                235,
                                250,
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

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.04f)
                                .scaleY(1.04f)
                                .setDuration(100)
                                .start();

                        if (position != selectedPosition) {

                            v.setBackground(
                                    createBackground(
                                            Color.rgb(
                                                    38,
                                                    47,
                                                    58
                                            ),
                                            Color.rgb(
                                                    75,
                                                    105,
                                                    120
                                            )
                                    )
                            );
                        }

                    } else {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        applyBackground(
                                (TextView) v,
                                position == selectedPosition
                        );
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

                    setSelectedPosition(
                            adapterPosition
                    );

                    if (listener != null) {

                        listener.onCategoryClick(
                                categories.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );
    }

    private void applyBackground(
            TextView view,
            boolean selected
    ) {

        if (selected) {

            view.setBackground(
                    createBackground(
                            Color.rgb(
                                    30,
                                    70,
                                    84
                            ),
                            Color.rgb(
                                    75,
                                    175,
                                    205
                            )
                    )
            );

        } else {

            view.setBackground(
                    createBackground(
                            Color.rgb(
                                    25,
                                    29,
                                    36
                            ),
                            Color.rgb(
                                    55,
                                    63,
                                    73
                            )
                    )
            );
        }
    }

    private GradientDrawable createBackground(
            int fillColor,
            int strokeColor
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                fillColor
        );

        drawable.setStroke(
                dpValue(1),
                strokeColor
        );

        drawable.setCornerRadius(
                dpValue(9)
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
        return categories.size();
    }

    static class CategoryViewHolder
            extends RecyclerView.ViewHolder {

        final TextView text;

        CategoryViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            text =
                    (TextView) itemView;
        }
    }
}
