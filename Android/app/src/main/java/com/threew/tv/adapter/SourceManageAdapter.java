package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源管理适配器。
 *
 * 用于：
 * - 视频源管理
 * - 启用 / 停用
 * - 默认源
 * - 优先级
 * - 测试状态
 * - TV 遥控器操作
 */
public class SourceManageAdapter
        extends RecyclerView.Adapter<SourceManageAdapter.SourceViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(
                VideoSource source,
                int position
        );
    }

    public interface OnSourceEnabledChangeListener {
        void onEnabledChanged(
                VideoSource source,
                boolean enabled,
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
    private final OnSourceEnabledChangeListener enabledListener;
    private final OnSourceLongClickListener longClickListener;

    public SourceManageAdapter(
            OnSourceClickListener clickListener,
            OnSourceEnabledChangeListener enabledListener,
            OnSourceLongClickListener longClickListener
    ) {

        this.clickListener =
                clickListener;

        this.enabledListener =
                enabledListener;

        this.longClickListener =
                longClickListener;
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
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 12),
                dp(parent, 8),
                dp(parent, 10),
                dp(parent, 8)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 88)
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

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setText("◆");

        icon.setTextSize(15);

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(
                        100,
                        200,
                        225
                )
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 38),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        LinearLayout content =
                new LinearLayout(
                        parent.getContext()
                );

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );

        contentParams.setMargins(
                dp(parent, 8),
                0,
                dp(parent, 8),
                0
        );

        root.addView(
                content,
                contentParams
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

        content.addView(
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

        name.setTextSize(14);

        name.setTextColor(
                Color.rgb(
                        238,
                        242,
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

        TextView defaultLabel =
                new TextView(
                        parent.getContext()
                );

        defaultLabel.setText(
                "默认"
        );

        defaultLabel.setTextSize(8);

        defaultLabel.setGravity(
                Gravity.CENTER
        );

        defaultLabel.setTextColor(
                Color.rgb(
                        105,
                        215,
                        235
                )
        );

        defaultLabel.setPadding(
                dp(parent, 6),
                dp(parent, 2),
                dp(parent, 6),
                dp(parent, 2)
        );

        titleRow.addView(
                defaultLabel,
                new LinearLayout.LayoutParams(
                        dp(parent, 42),
                        dp(parent, 22)
                )
        );

        TextView api =
                new TextView(
                        parent.getContext()
                );

        api.setTextSize(8);

        api.setTextColor(
                Color.rgb(
                        92,
                        103,
                        115
                )
        );

        api.setGravity(
                Gravity.CENTER_VERTICAL
        );

        api.setSingleLine(true);

        api.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        content.addView(
                api,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 22)
                )
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setTextSize(8);

        state.setTextColor(
                Color.rgb(
                        110,
                        190,
                        210
                )
        );

        state.setGravity(
                Gravity.CENTER_VERTICAL
        );

        state.setSingleLine(true);

        content.addView(
                state,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        CheckBox enabled =
                new CheckBox(
                        parent.getContext()
                );

        enabled.setText(
                "启用"
        );

        enabled.setTextSize(9);

        enabled.setTextColor(
                Color.rgb(
                        160,
                        170,
                        180
                )
        );

        enabled.setGravity(
                Gravity.CENTER
        );

        enabled.setButtonTintList(
                new android.content.res.ColorStateList(
                        new int[][]{
                                new int[]{
                                        android.R.attr.state_checked
                                },
                                new int[]{}
                        },
                        new int[]{
                                Color.rgb(
                                        90,
                                        205,
                                        225
                                ),
                                Color.rgb(
                                        100,
                                        108,
                                        118
                                )
                        }
                )
        );

        root.addView(
                enabled,
                new LinearLayout.LayoutParams(
                        dp(parent, 62),
                        dp(parent, 42)
                )
        );

        return new SourceViewHolder(
                root,
                name,
                defaultLabel,
                api,
                state,
                enabled
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
                safe(
                        source.getName()
                );

        if (name.isEmpty()) {
            name = "未命名源";
        }

        holder.name.setText(
                name
        );

        boolean isDefault =
                source.isDefaultSource();

        holder.defaultLabel.setVisibility(
                isDefault
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        String api =
                safe(
                        source.getApiUrl()
                );

        if (api.isEmpty()) {
            api = "无 API 地址";
        }

        holder.api.setText(
                api
        );

        holder.state.setText(
                buildState(source)
        );

        holder.enabled.setOnCheckedChangeListener(
                null
        );

        holder.enabled.setChecked(
                source.isEnabled()
        );

        holder.enabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    VideoSource current =
                            sources.get(
                                    adapterPosition
                            );

                    current.setEnabled(
                            isChecked
                    );

                    if (enabledListener != null) {

                        enabledListener.onEnabledChanged(
                                current,
                                isChecked,
                                adapterPosition
                        );
                    }
                }
        );

        applyBackground(
                holder.itemView,
                false
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    applyBackground(
                            v,
                            hasFocus
                    );

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

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
                v -> {

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

    private String buildState(
            VideoSource source
    ) {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                source.isEnabled()
                        ? "已启用"
                        : "已停用"
        );

        builder.append("  ·  优先级 ")
                .append(
                        source.getPriority()
                );

        if (source.isCmsSource()) {

            builder.append(
                    "  ·  CMS"
            );
        }

        if (source.isAvailable()) {

            builder.append(
                    "  ·  可用"
            );

        } else {

            builder.append(
                    "  ·  未测试/不可用"
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
                                32,
                                45,
                                56
                        )
                        : Color.rgb(
                                20,
                                23,
                                30
                        )
        );

        drawable.setStroke(
                dpValue(
                        view,
                        1
                ),
                focused
                        ? Color.rgb(
                                75,
                                135,
                                155
                        )
                        : Color.rgb(
                                43,
                                51,
                                61
                        )
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
        final TextView defaultLabel;
        final TextView api;
        final TextView state;
        final CheckBox enabled;

        SourceViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView defaultLabel,
                TextView api,
                TextView state,
                CheckBox enabled
        ) {

            super(itemView);

            this.name = name;
            this.defaultLabel = defaultLabel;
            this.api = api;
            this.state = state;
            this.enabled = enabled;
        }
    }
}
