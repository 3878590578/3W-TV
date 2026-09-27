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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源卡片网格适配器。
 *
 * 用于：
 * - 来源选择
 * - 来源管理
 * - 详情页来源切换
 *
 * 显示：
 * 来源名称、类型、优先级、默认状态、启用状态。
 */
public class SourceCardGridAdapter
        extends RecyclerView.Adapter<SourceCardGridAdapter.ViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(
                VideoSource source,
                int position
        );
    }

    private final Context context;
    private final List<VideoSource> sources =
            new ArrayList<>();

    private OnSourceClickListener listener;
    private int selectedPosition = -1;

    public SourceCardGridAdapter(Context context) {
        this.context = context;
    }

    public SourceCardGridAdapter(
            Context context,
            List<VideoSource> data
    ) {
        this.context = context;

        if (data != null) {
            sources.addAll(data);
        }
    }

    public void setOnSourceClickListener(
            OnSourceClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSources(List<VideoSource> data) {
        sources.clear();

        if (data != null) {
            sources.addAll(data);
        }

        selectedPosition = findDefaultPosition();

        notifyDataSetChanged();
    }

    public void addSources(List<VideoSource> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = sources.size();

        sources.addAll(data);

        if (selectedPosition < 0) {
            selectedPosition =
                    findDefaultPosition();
        }

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void clear() {
        sources.clear();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position < 0 ||
                position >= sources.size()) {
            return;
        }

        int oldPosition =
                selectedPosition;

        selectedPosition =
                position;

        if (oldPosition >= 0 &&
                oldPosition != selectedPosition) {

            notifyItemChanged(oldPosition);
        }

        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public VideoSource getItem(int position) {
        if (position < 0 ||
                position >= sources.size()) {
            return null;
        }

        return sources.get(position);
    }

    private int findDefaultPosition() {
        for (int i = 0; i < sources.size(); i++) {
            VideoSource source = sources.get(i);

            if (source != null &&
                    source.isDefaultSource()) {
                return i;
            }
        }

        return sources.isEmpty() ? -1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setFocusable(true);
        root.setClickable(true);

        int margin = dp(5);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(112)
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        root.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        TextView name =
                new TextView(context);

        name.setTextSize(16);
        name.setSingleLine(true);
        name.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        root.addView(
                name,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView type =
                new TextView(context);

        type.setTextSize(11);
        type.setAlpha(0.65f);
        type.setSingleLine(true);
        type.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        typeParams.topMargin = dp(6);

        root.addView(
                type,
                typeParams
        );

        TextView status =
                new TextView(context);

        status.setTextSize(10);
        status.setAlpha(0.7f);
        status.setSingleLine(true);
        status.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(4);

        root.addView(
                status,
                statusParams
        );

        return new ViewHolder(
                root,
                name,
                type,
                status
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        VideoSource source =
                sources.get(position);

        String name =
                source.getDisplayName();

        if (name == null ||
                name.trim().isEmpty()) {
            name = "未命名来源";
        }

        holder.name.setText(name);

        String type;

        if (source.isCmsSource()) {
            type = "CMS 来源";
        } else {
            type = "API 来源";
        }

        holder.type.setText(
                "类型：" + type
        );

        StringBuilder status =
                new StringBuilder();

        status.append(
                "优先级："
        ).append(
                source.getPriority()
        );

        if (source.isDefaultSource()) {
            status.append(" · 默认");
        }

        if (source.isEnabled()) {
            status.append(" · 已启用");
        } else {
            status.append(" · 已停用");
        }

        if (source.isAvailable()) {
            status.append(" · 可用");
        }

        holder.status.setText(
                status.toString()
        );

        boolean selected =
                position == selectedPosition;

        if (selected) {

            holder.name.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            holder.name.setTextSize(17);

            holder.itemView.setScaleX(1.025f);
            holder.itemView.setScaleY(1.025f);

        } else {

            holder.name.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            );

            holder.name.setTextSize(16);

            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            int oldPosition =
                    selectedPosition;

            selectedPosition =
                    adapterPosition;

            if (oldPosition >= 0 &&
                    oldPosition != selectedPosition) {

                notifyItemChanged(oldPosition);
            }

            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onSourceClick(
                        sources.get(adapterPosition),
                        adapterPosition
                );
            }
        });

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.05f)
                                .scaleY(1.05f)
                                .setDuration(120)
                                .start();

                        holder.name.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );

                    } else {

                        float scale =
                                position == selectedPosition
                                        ? 1.025f
                                        : 1.0f;

                        v.animate()
                                .scaleX(scale)
                                .scaleY(scale)
                                .setDuration(120)
                                .start();

                        holder.name.setTypeface(
                                Typeface.DEFAULT,
                                position == selectedPosition
                                        ? Typeface.BOLD
                                        : Typeface.NORMAL
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    private int dp(int value) {
        float density =
                context.getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView type;
        final TextView status;

        public ViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView type,
                TextView status
        ) {
            super(itemView);

            this.name = name;
            this.type = type;
            this.status = status;
        }
    }
        }
