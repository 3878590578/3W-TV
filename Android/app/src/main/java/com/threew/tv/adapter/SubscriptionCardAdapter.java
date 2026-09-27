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

import com.threew.tv.model.Subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订阅卡片适配器。
 *
 * 用于：
 * - 订阅管理页快捷卡片
 * - 首页订阅状态
 * - 本地订阅 / URL 订阅展示
 * - TV 横向卡片布局
 */
public class SubscriptionCardAdapter
        extends RecyclerView.Adapter<SubscriptionCardAdapter.SubscriptionCardViewHolder> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(
                Subscription subscription,
                int position
        );
    }

    public interface OnSubscriptionLongClickListener {
        boolean onSubscriptionLongClick(
                Subscription subscription,
                int position
        );
    }

    private final List<Subscription> subscriptions =
            new ArrayList<>();

    private final OnSubscriptionClickListener clickListener;
    private final OnSubscriptionLongClickListener longClickListener;

    private int cardWidthDp = 210;
    private int cardHeightDp = 132;

    public SubscriptionCardAdapter(
            OnSubscriptionClickListener clickListener
    ) {

        this(
                clickListener,
                null
        );
    }

    public SubscriptionCardAdapter(
            OnSubscriptionClickListener clickListener,
            OnSubscriptionLongClickListener longClickListener
    ) {

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setCardSize(
            int widthDp,
            int heightDp
    ) {

        if (widthDp > 0) {
            cardWidthDp = widthDp;
        }

        if (heightDp > 0) {
            cardHeightDp = heightDp;
        }

        notifyDataSetChanged();
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

        int count =
                subscriptions.size();

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
    public SubscriptionCardViewHolder onCreateViewHolder(
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
                Gravity.TOP
        );

        root.setPadding(
                dp(parent, 12),
                dp(parent, 10),
                dp(parent, 12),
                dp(parent, 10)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        dp(parent, cardWidthDp),
                        dp(parent, cardHeightDp)
                );

        rootParams.setMargins(
                dp(parent, 5),
                dp(parent, 5),
                dp(parent, 5),
                dp(parent, 5)
        );

        root.setLayoutParams(rootParams);

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
                        dp(parent, 30)
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextSize(13);

        name.setTextColor(
                Color.rgb(
                        240,
                        244,
                        248
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

        type.setTextSize(7);

        type.setTextColor(
                Color.rgb(
                        100,
                        210,
                        230
                )
        );

        type.setGravity(
                Gravity.CENTER
        );

        type.setPadding(
                dp(parent, 5),
                0,
                dp(parent, 5),
                0
        );

        titleRow.addView(
                type,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        dp(parent, 21)
                )
        );

        TextView address =
                new TextView(
                        parent.getContext()
                );

        address.setTextSize(8);

        address.setTextColor(
                Color.rgb(
                        110,
                        122,
                        135
                )
        );

        address.setGravity(
                Gravity.CENTER_VERTICAL
        );

        address.setSingleLine(true);

        address.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        root.addView(
                address,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 28)
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
                        dp(parent, 24)
                )
        );

        TextView update =
                new TextView(
                        parent.getContext()
                );

        update.setTextSize(7);

        update.setTextColor(
                Color.rgb(
                        100,
                        170,
                        190
                )
        );

        update.setGravity(
                Gravity.CENTER_VERTICAL
        );

        update.setSingleLine(true);

        root.addView(
                update,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        TextView arrow =
                new TextView(
                        parent.getContext()
                );

        arrow.setText("›");

        arrow.setTextSize(22);

        arrow.setTextColor(
                Color.rgb(
                        85,
                        105,
                        120
                )
        );

        arrow.setGravity(
                Gravity.CENTER
        );

        root.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        return new SubscriptionCardViewHolder(
                root,
                name,
                type,
                address,
                status,
                update,
                arrow
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubscriptionCardViewHolder holder,
            int position
    ) {

        Subscription subscription =
                subscriptions.get(position);

        String name =
                safe(subscription.getName());

        if (name.isEmpty()) {
            name = "未命名订阅";
        }

        holder.name.setText(name);

        if (subscription.isLocalFile()) {

            holder.type.setText("本地");

        } else {

            holder.type.setText("URL");
        }

        String address;

        if (subscription.isLocalFile()) {

            address =
                    safe(subscription.getLocalUri());

            if (address.isEmpty()) {
                address = "本地文件";
            }

        } else {

            address =
                    safe(subscription.getUrl());

            if (address.isEmpty()) {
                address = "未设置订阅地址";
            }
        }

        holder.address.setText(
                address
        );

        boolean enabled =
                subscription.isEnabled();

        boolean hasContent =
                subscription.hasContent();

        if (enabled && hasContent) {

            holder.status.setText(
                    "● 已启用  ·  已加载"
            );

            holder.status.setTextColor(
                    Color.rgb(
                            100,
                            205,
                            175
                    )
            );

        } else if (enabled) {

            holder.status.setText(
                    "● 已启用  ·  暂无内容"
            );

            holder.status.setTextColor(
                    Color.rgb(
                            210,
                            175,
                            100
                    )
            );

        } else {

            holder.status.setText(
                    "● 已停用"
            );

            holder.status.setTextColor(
                    Color.rgb(
                            120,
                            130,
                            140
                    )
            );
        }

        long updateTime =
                subscription.getLastUpdateTime();

        if (updateTime > 0) {

            holder.update.setText(
                    "最后更新：" +
                            formatTime(updateTime)
            );

        } else {

            holder.update.setText(
                    "最后更新：暂无"
            );
        }

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

                    holder.arrow.setTextColor(
                            hasFocus
                                    ? Color.rgb(
                                            100,
                                            215,
                                            235
                                    )
                                    : Color.rgb(
                                            85,
                                            105,
                                            120
                                    )
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
                view -> {

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
        return subscriptions.size();
    }

    static class SubscriptionCardViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView type;
        final TextView address;
        final TextView status;
        final TextView update;
        final TextView arrow;

        SubscriptionCardViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView type,
                TextView address,
                TextView status,
                TextView update,
                TextView arrow
        ) {

            super(itemView);

            this.name = name;
            this.type = type;
            this.address = address;
            this.status = status;
            this.update = update;
            this.arrow = arrow;
        }
    }
}
