package com.threew.tv.adapter;

import android.content.res.ColorStateList;
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

import com.threew.tv.model.Subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订阅管理适配器。
 *
 * 用于：
 * - URL 订阅
 * - 本地 TXT / JSON 订阅
 * - 启用 / 停用
 * - 内容状态
 * - 最后更新时间
 * - TV 遥控器操作
 */
public class SubscriptionManageAdapter
        extends RecyclerView.Adapter<SubscriptionManageAdapter.SubscriptionViewHolder> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(
                Subscription subscription,
                int position
        );
    }

    public interface OnEnabledChangeListener {
        void onEnabledChanged(
                Subscription subscription,
                boolean enabled,
                int position
        );
    }

    public interface OnLongClickListener {
        boolean onSubscriptionLongClick(
                Subscription subscription,
                int position
        );
    }

    private final List<Subscription> subscriptions =
            new ArrayList<>();

    private final OnSubscriptionClickListener clickListener;
    private final OnEnabledChangeListener enabledListener;
    private final OnLongClickListener longClickListener;

    public SubscriptionManageAdapter(
            OnSubscriptionClickListener clickListener,
            OnEnabledChangeListener enabledListener,
            OnLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.enabledListener = enabledListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Subscription> data
    ) {

        subscriptions.clear();

        if (data != null) {
            subscriptions.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void add(
            Subscription subscription
    ) {

        if (subscription == null) {
            return;
        }

        subscriptions.add(subscription);

        notifyItemInserted(
                subscriptions.size() - 1
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= subscriptions.size()) {

            return;
        }

        subscriptions.remove(position);

        notifyItemRemoved(position);
    }

    public void clear() {

        int count = subscriptions.size();

        if (count == 0) {
            return;
        }

        subscriptions.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public Subscription getItem(
            int position
    ) {

        if (position < 0 ||
                position >= subscriptions.size()) {

            return null;
        }

        return subscriptions.get(position);
    }

    public List<Subscription> getItems() {
        return new ArrayList<>(subscriptions);
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(
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
                        dp(parent, 92)
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(rootParams);

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setText("↻");

        icon.setTextSize(20);

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

        TextView type =
                new TextView(
                        parent.getContext()
                );

        type.setTextSize(8);

        type.setGravity(
                Gravity.CENTER
        );

        type.setTextColor(
                Color.rgb(
                        100,
                        205,
                        225
                )
        );

        type.setPadding(
                dp(parent, 6),
                dp(parent, 2),
                dp(parent, 6),
                dp(parent, 2)
        );

        titleRow.addView(
                type,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        dp(parent, 22)
                )
        );

        TextView source =
                new TextView(
                        parent.getContext()
                );

        source.setTextSize(8);

        source.setTextColor(
                Color.rgb(
                        105,
                        115,
                        128
                )
        );

        source.setGravity(
                Gravity.CENTER_VERTICAL
        );

        source.setSingleLine(true);

        source.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        content.addView(
                source,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 22)
                )
        );

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextSize(8);

        status.setTextColor(
                Color.rgb(
                        110,
                        190,
                        210
                )
        );

        status.setGravity(
                Gravity.CENTER_VERTICAL
        );

        status.setSingleLine(true);

        content.addView(
                status,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 18)
                )
        );

        CheckBox enabled =
                new CheckBox(
                        parent.getContext()
                );

        enabled.setText("启用");

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
                new ColorStateList(
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

        return new SubscriptionViewHolder(
                root,
                name,
                type,
                source,
                status,
                enabled
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubscriptionViewHolder holder,
            int position
    ) {

        Subscription subscription =
                subscriptions.get(position);

        String name =
                safe(
                        subscription.getName()
                );

        if (name.isEmpty()) {
            name = "未命名订阅";
        }

        holder.name.setText(name);

        if (subscription.isLocalFile()) {

            holder.type.setText("本地文件");

        } else {

            holder.type.setText("URL订阅");
        }

        String source;

        if (subscription.isLocalFile()) {

            source =
                    safe(
                            subscription.getLocalUri()
                    );

            if (source.isEmpty()) {
                source = "本地文件未设置";
            }

        } else {

            source =
                    safe(
                            subscription.getUrl()
                    );

            if (source.isEmpty()) {
                source = "订阅地址未设置";
            }
        }

        holder.source.setText(source);

        holder.status.setText(
                buildStatus(subscription)
        );

        holder.enabled.setOnCheckedChangeListener(
                null
        );

        holder.enabled.setChecked(
                subscription.isEnabled()
        );

        holder.enabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    Subscription current =
                            subscriptions.get(
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

                        clickListener.onSubscriptionClick(
                                subscriptions.get(
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
                                .onSubscriptionLongClick(
                                        subscriptions.get(
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
            Subscription subscription
    ) {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                subscription.isEnabled()
                        ? "已启用"
                        : "已停用"
        );

        builder.append("  ·  ");

        if (subscription.hasContent()) {

            builder.append("已有内容");

        } else {

            builder.append("暂无内容");
        }

        long updateTime =
                subscription.getLastUpdateTime();

        if (updateTime > 0) {

            builder.append("  ·  更新 ")
                    .append(
                            formatTime(updateTime)
                    );
        }

        return builder.toString();
    }

    private String formatTime(
            long timestamp
    ) {

        try {

            return new SimpleDateFormat(
                    "MM-dd HH:mm",
                    Locale.getDefault()
            ).format(
                    new Date(timestamp)
            );

        } catch (Exception e) {

            return "";
        }
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
                dpValue(view, 1),
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
                dpValue(view, 9)
        );

        view.setBackground(drawable);
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
        return subscriptions.size();
    }

    static class SubscriptionViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView type;
        final TextView source;
        final TextView status;
        final CheckBox enabled;

        SubscriptionViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView type,
                TextView source,
                TextView status,
                CheckBox enabled
        ) {

            super(itemView);

            this.name = name;
            this.type = type;
            this.source = source;
            this.status = status;
            this.enabled = enabled;
        }
    }
}
