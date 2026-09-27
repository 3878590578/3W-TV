package com.threew.tv.adapter;

import android.graphics.Color;
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
 * 订阅源适配器。
 *
 * 用于订阅管理页面：
 * - 显示订阅名称
 * - 显示订阅地址 / 本地文件
 * - 显示订阅类型
 * - 显示启用状态
 * - 显示最后更新时间
 * - 支持 Android TV 遥控器
 */
public class SubscriptionSourceAdapter
        extends RecyclerView.Adapter<SubscriptionSourceAdapter.SubscriptionViewHolder> {

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

    private final List<Subscription> data =
            new ArrayList<>();

    private final OnSubscriptionClickListener clickListener;
    private final OnSubscriptionLongClickListener longClickListener;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
            );

    public SubscriptionSourceAdapter(
            OnSubscriptionClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public SubscriptionSourceAdapter(
            OnSubscriptionClickListener clickListener,
            OnSubscriptionLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Subscription> subscriptions
    ) {

        data.clear();

        if (subscriptions != null) {
            data.addAll(subscriptions);
        }

        notifyDataSetChanged();
    }

    public void add(
            Subscription subscription
    ) {

        if (subscription == null) {
            return;
        }

        data.add(subscription);

        notifyItemInserted(
                data.size() - 1
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return;
        }

        data.remove(position);

        notifyItemRemoved(position);
    }

    public void update(
            int position,
            Subscription subscription
    ) {

        if (position < 0 ||
                position >= data.size() ||
                subscription == null) {

            return;
        }

        data.set(
                position,
                subscription
        );

        notifyItemChanged(position);
    }

    public Subscription getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return null;
        }

        return data.get(position);
    }

    public List<Subscription> getItems() {
        return new ArrayList<>(data);
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
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(parent, 14),
                dp(parent, 9),
                dp(parent, 14),
                dp(parent, 9)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 108)
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
                        dp(parent, 27)
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextSize(15);

        name.setTextColor(
                Color.rgb(238, 242, 247)
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setMaxLines(1);

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

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setTextSize(9);

        state.setGravity(
                Gravity.CENTER
        );

        titleRow.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 64),
                        dp(parent, 24)
                )
        );

        TextView address =
                new TextView(
                        parent.getContext()
                );

        address.setTextSize(9);

        address.setTextColor(
                Color.rgb(112, 122, 136)
        );

        address.setGravity(
                Gravity.CENTER_VERTICAL
        );

        address.setMaxLines(1);

        address.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        LinearLayout.LayoutParams addressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                );

        addressParams.topMargin =
                dp(parent, 2);

        root.addView(
                address,
                addressParams
        );

        LinearLayout infoRow =
                new LinearLayout(
                        parent.getContext()
                );

        infoRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        infoRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                infoRow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 27)
                )
        );

        TextView type =
                createInfoText(parent);

        infoRow.addView(
                type,
                new LinearLayout.LayoutParams(
                        dp(parent, 82),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView update =
                createInfoText(parent);

        infoRow.addView(
                update,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView contentState =
                createInfoText(parent);

        contentState.setGravity(
                Gravity.CENTER
        );

        infoRow.addView(
                contentState,
                new LinearLayout.LayoutParams(
                        dp(parent, 78),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new SubscriptionViewHolder(
                root,
                name,
                state,
                address,
                type,
                update,
                contentState
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubscriptionViewHolder holder,
            int position
    ) {

        Subscription subscription =
                data.get(position);

        String name =
                subscription.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "未命名订阅";
        }

        holder.name.setText(
                name
        );

        String address;

        if (subscription.isLocalFile()) {

            address =
                    subscription.getLocalUri();

            if (address == null ||
                    address.trim().isEmpty()) {

                address = "本地文件";
            }

        } else {

            address =
                    subscription.getUrl();

            if (address == null ||
                    address.trim().isEmpty()) {

                address = "未设置地址";
            }
        }

        holder.address.setText(
                address
        );

        String type =
                subscription.getType();

        if (type == null ||
                type.trim().isEmpty()) {

            type = subscription.isLocalFile()
                    ? "本地"
                    : "URL";
        }

        holder.type.setText(
                "类型：" + type
        );

        long updateTime =
                subscription.getLastUpdateTime();

        if (updateTime > 0) {

            holder.update.setText(
                    "更新：" +
                            dateFormat.format(
                                    new Date(updateTime)
                            )
            );

        } else {

            holder.update.setText(
                    "尚未更新"
            );
        }

        holder.contentState.setText(
                subscription.hasContent()
                        ? "已导入"
                        : "未导入"
        );

        if (subscription.hasContent()) {

            holder.contentState.setTextColor(
                    Color.rgb(90, 200, 235)
            );

        } else {

            holder.contentState.setTextColor(
                    Color.rgb(155, 145, 105)
            );
        }

        if (subscription.isEnabled()) {

            holder.state.setText(
                    "已启用"
            );

            holder.state.setTextColor(
                    Color.rgb(90, 210, 245)
            );

        } else {

            holder.state.setText(
                    "已停用"
            );

            holder.state.setTextColor(
                    Color.rgb(125, 130, 138)
            );
        }

        holder.itemView.setBackgroundColor(
                Color.rgb(20, 23, 30)
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                Color.rgb(32, 45, 56)
                        );

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackgroundColor(
                                Color.rgb(20, 23, 30)
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

                    if (clickListener != null) {

                        clickListener.onSubscriptionClick(
                                data.get(adapterPosition),
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
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private TextView createInfoText(
            ViewGroup parent
    ) {

        TextView text =
                new TextView(
                        parent.getContext()
                );

        text.setTextSize(9);

        text.setTextColor(
                Color.rgb(125, 135, 148)
        );

        text.setGravity(
                Gravity.CENTER_VERTICAL
        );

        text.setMaxLines(1);

        text.setEllipsize(
                TextUtils.TruncateAt.END
        );

        return text;
    }

    @Override
    public int getItemCount() {
        return data.size();
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

    static class SubscriptionViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView state;
        final TextView address;
        final TextView type;
        final TextView update;
        final TextView contentState;

        SubscriptionViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView state,
                TextView address,
                TextView type,
                TextView update,
                TextView contentState
        ) {

            super(itemView);

            this.name = name;
            this.state = state;
            this.address = address;
            this.type = type;
            this.update = update;
            this.contentState = contentState;
        }
    }
}
