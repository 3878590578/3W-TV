package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
 * 分类卡片适配器
 *
 * 用于首页、搜索页等位置显示：
 * 全部 / 电影 / 电视剧 / 动漫 / 综艺 / 短剧等分类。
 *
 * 特点：
 * 1. 横向滚动友好
 * 2. TV 遥控器焦点友好
 * 3. 支持选中状态
 * 4. 不依赖额外 drawable XML
 * 5. 支持动态添加分类
 */
public class CategoryCardAdapter extends RecyclerView.Adapter<CategoryCardAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    private final Context context;
    private final List<String> categories = new ArrayList<>();

    private int selectedPosition = 0;
    private OnCategoryClickListener listener;

    public CategoryCardAdapter(Context context) {
        this.context = context;

        categories.add("全部");
        categories.add("电影");
        categories.add("电视剧");
        categories.add("动漫");
        categories.add("综艺");
        categories.add("短剧");
    }

    public CategoryCardAdapter(Context context, List<String> data) {
        this.context = context;

        if (data != null) {
            categories.addAll(data);
        }

        if (categories.isEmpty()) {
            categories.add("全部");
        }
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<String> data) {
        categories.clear();

        if (data != null) {
            categories.addAll(data);
        }

        if (categories.isEmpty()) {
            categories.add("全部");
        }

        selectedPosition = 0;
        notifyDataSetChanged();
    }

    public void addCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return;
        }

        String value = category.trim();

        if (categories.contains(value)) {
            return;
        }

        categories.add(value);
        notifyItemInserted(categories.size() - 1);
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= categories.size()) {
            return;
        }

        int oldPosition = selectedPosition;
        selectedPosition = position;

        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedCategory() {
        if (selectedPosition < 0 || selectedPosition >= categories.size()) {
            return "";
        }

        return categories.get(selectedPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER);

        int horizontalPadding = dp(18);
        int verticalPadding = dp(10);

        root.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding
        );

        root.setFocusable(true);
        root.setClickable(true);

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setSingleLine(true);

        root.addView(
                textView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return new ViewHolder(root, textView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        String category = categories.get(position);

        holder.textView.setText(category);

        boolean selected = position == selectedPosition;

        if (selected) {
            holder.textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            holder.textView.setTextSize(15);
            holder.textView.setAlpha(1.0f);

            holder.itemView.setScaleX(1.04f);
            holder.itemView.setScaleY(1.04f);
        } else {
            holder.textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            holder.textView.setTextSize(14);
            holder.textView.setAlpha(0.72f);

            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int oldPosition = selectedPosition;
            selectedPosition = adapterPosition;

            if (oldPosition != selectedPosition) {
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
            }

            if (listener != null) {
                listener.onCategoryClick(
                        categories.get(adapterPosition),
                        adapterPosition
                );
            }
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(120)
                        .start();

                holder.textView.setAlpha(1.0f);
            } else {
                float scale = position == selectedPosition ? 1.04f : 1.0f;

                v.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(120)
                        .start();

                holder.textView.setAlpha(
                        position == selectedPosition ? 1.0f : 0.72f
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private int dp(int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textView;

        public ViewHolder(
                @NonNull View itemView,
                TextView textView
        ) {
            super(itemView);
            this.textView = textView;
        }
    }
}
