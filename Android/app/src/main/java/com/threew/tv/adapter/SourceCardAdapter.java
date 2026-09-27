package com.threew.tv.adapter;

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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源卡片适配器。
 *
 * 用于：
 * - 详情页来源选择
 * - 来源管理快捷列表
 * - 视频播放前来源选择
 * - 多源聚合后的来源展示
 *
 * 支持：
 * - 默认来源标识
 * - 启用/停用状态
 * - 测试状态
 * - 优先级
 * - TV 遥控器焦点
 */
public class SourceCardAdapter
        extends RecyclerView.Adapter<SourceCardAdapter.SourceViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(
                VideoSource source,
                int position
        );
    }

    public interface OnSourceLongClickListener {
        boolean onSourceLongClick(
                VideoSource source,
                int position
        );
    }

    private final List<VideoSource> sources =
            new ArrayList<>();

    private final OnSourceClickListener clickListener;
    private final OnSourceLongClickListener longClickListener;

    public SourceCardAdapter(
            OnSourceClickListener clickListener
    ) {

        this(
                clickListener,
                null
        );
    }

    public SourceCardAdapter(
            OnSourceClickListener clickListener,
            OnSourceLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<VideoSource> data
    ) {

        sources.clear();

        if (data != null) {
            sources.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void add(
            VideoSource source
    ) {

        if (source == null) {
            return;
        }

        sources.add(source);

        notifyItemInserted(
                sources.size() - 1
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= sources.size()) {

            return;
        }

        sources.remove(position);

        notifyItemRemoved(position);
    }

    public void clear() {

        int count =
                sources.size();

        if (count == 0) {
            return;
        }

        sources.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public VideoSource getItem(
            int position
    ) {

        if (position < 0 ||
                position >= sources.size()) {

            return null;
        }

        return sources.get(position);
    }

    public List<VideoSource> getItems() {
        return new ArrayList<>(sources);
    }

    @NonNull
    @Override
    public SourceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        LinearLayout root =
                new LinearLayout(
                        parent.getContext()
                );

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 10),
                dp(parent, 9),
                dp(parent, 10),
                dp(parent, 9)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        dp(parent, 168),
                        dp(parent, 112)
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(
                rootParams
        );

        LinearLayout titleRow =
                new LinearLayout(
                        parent.getContext()
                );

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                titleRow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 28)
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextSize(13);

        name.setTextColor(
                Color.rgb(
                        238,
                        243,
                        247
                )
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setSingleLine(true);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        titleRow.addView(
                name,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView defaultTag =
                new TextView(
                        parent.getContext()
                );

        defaultTag.setText(
                "默认"
        );

        defaultTag.setTextSize(7);

        defaultTag.setTextColor(
                Color.rgb(
                        100,
                        215,
                        230
                )
        );

        defaultTag.setGravity(
                Gravity.CENTER
        );

        defaultTag.setPadding(
                dp(parent, 5),
                0,
                dp(parent, 5),
                0
        );

        titleRow.addView(
                defaultTag,
                new LinearLayout.LayoutParams(
                        dp(parent, 34),
                        dp(parent, 19)
                )
        );

        TextView api =
                new TextView(
                        parent.getContext()
                );

        api.setTextSize(7);

        api.setTextColor(
                Color.rgb(
                        105,
                        118,
                        132
                )
        );

        api.setGravity(
                Gravity.CENTER_VERTICAL
        );

        api.setSingleLine(true);

        api.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        root.addView(
                api,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 25)
                )
        );

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextSize(8);

        status.setGravity(
                Gravity.CENTER_VERTICAL
        );

        status.setSingleLine(true);

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                )
        );

        TextView priority =
                new TextView(
                        parent.getContext()
                );

        priority.setTextSize(7);

        priority.setTextColor(
                Color.rgb(
                        105,
                        180,
                        200
                )
        );

        priority.setGravity(
                Gravity.CENTER_VERTICAL
        );

        priority.setSingleLine(true);

        root.addView(
                priority,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        return new SourceViewHolder(
                root,
                name,
                defaultTag,
                api,
                status,
                priority
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SourceViewHolder holder,
            int position
    ) {

        VideoSource source =
                sources.get(position);

        String name =
                safe(source.getName());

        if (name.isEmpty()) {
            name = "未命名来源";
        }

        holder.name.setText(
                name
        );

        holder.defaultTag.setVisibility(
                source.isDefaultSource()
                        ? View.VISIBLE
                        : View.GONE
        );

        String apiUrl =
                safe(source.getApiUrl());

        if (apiUrl.isEmpty()) {
            apiUrl = "未设置接口";
        }

        holder.api.setText(
                apiUrl
        );

        holder.status.setText(
                buildStatus(source)
        );

        holder.priority.setText(
                "优先级：" +
                        source.getPriority()
        );

        applyBackground(
                holder.itemView,
                false
        );

        holder.itemView.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    applyBackground(
                            view,
                            hasFocus
                    );

                    if (hasFocus) {

                        view.animate()
                                .scaleX(1.025f)
                                .scaleY(1.025f)
                                .setDuration(110)
                                .start();

                    } else {

                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(110)
                                .start();
                    }
                }
        );

        holder.itemView.setOnClickListener(
                view -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    if (clickListener != null) {

                        clickListener.onSourceClick(
                                sources.get(
                                        adapterPosition
                                ),
                                adapterPosition
                        );
                    }
                }
        );

        holder.itemView.setOnLongClickListener(
                view -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return false;
                    }

                    if (longClickListener != null) {

                        return longClickListener
                                .onSourceLongClick(
                                        sources.get(
                                                adapterPosition
                                        ),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private String buildStatus(
            VideoSource source
    ) {

        StringBuilder builder =
                new StringBuilder();

        if (source.isEnabled()) {

            builder.append(
                    "已启用"
            );

        } else {

            builder.append(
                    "已停用"
            );
        }

        builder.append(
                "  ·  "
        );

        if (source.isAvailable()) {

            builder.append(
                    "可用"
            );

        } else {

            builder.append(
                    "未检测"
            );
        }

        if (source.isCmsSource()) {

            builder.append(
                    "  ·  CMS"
            );
        }

        return builder.toString();
    }

    private void applyBackground(
            View view,
            boolean focused
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                focused
                        ? Color.rgb(
                                33,
                                47,
                                59
                        )
                        : Color.rgb(
                                20,
                                23,
                                30
                        )
        );

        drawable.setStroke(
                dpValue(view, 1),
                focused
                        ? Color.rgb(
                                75,
                                140,
                                160
                        )
                        : Color.rgb(
                                43,
                                51,
                                61
                        )
        );

        drawable.setCornerRadius(
                dpValue(view, 9)
        );

        view.setBackground(
                drawable
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

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    static class SourceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView defaultTag;
        final TextView api;
        final TextView status;
        final TextView priority;

        SourceViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView defaultTag,
                TextView api,
                TextView status,
                TextView priority
        ) {

            super(itemView);

            this.name = name;
            this.defaultTag = defaultTag;
            this.api = api;
            this.status = status;
            this.priority = priority;
        }
    }
}
