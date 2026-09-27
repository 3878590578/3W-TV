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

import com.threew.tv.model.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅源卡片网格适配器。
 *
 * 用于订阅管理页面显示：
 * - 订阅名称
 * - 本地/网络类型
 * - 内容状态
 * - 启用状态
 * - 更新时间
 */
public class SubscriptionCardGridAdapter
        extends RecyclerView.Adapter<SubscriptionCardGridAdapter.ViewHolder> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(
                Subscription subscription,
                int position
        );
    }

    private final Context context;
    private final List<Subscription> subscriptions =
            new ArrayList<>();

    private OnSubscriptionClickListener listener;
    private int selectedPosition = -1;

    public SubscriptionCardGridAdapter(Context context) {
        this.context = context;
    }

    public SubscriptionCardGridAdapter(
            Context context,
            List<Subscription> data
    ) {
        this.context = context;

        if (data != null) {
            subscriptions.addAll(data);
        }
    }

    public void setOnSubscriptionClickListener(
            OnSubscriptionClickListener listener
    ) {
        this.listener = listener;
    }

    public void setSubscriptions(
            List<Subscription> data
    ) {
        subscriptions.clear();

        if (data != null) {
            subscriptions.addAll(data);
        }

        selectedPosition = -1;

        notifyDataSetChanged();
    }

    public void addSubscriptions(
            List<Subscription> data
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = subscriptions.size();

        subscriptions.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void clear() {
        subscriptions.clear();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position < 0 ||
                position >= subscriptions.size()) {
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

    public Subscription getItem(int position) {
        if (position < 0 ||
                position >= subscriptions.size()) {
            return null;
        }

        return subscriptions.get(position);
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
        Subscription subscription =
                subscriptions.get(position);

        String name =
                subscription.getDisplayName();

        if (name == null ||
                name.trim().isEmpty()) {
            name = "未命名订阅";
        }

        holder.name.setText(name);

        String type;

        if (subscription.isLocalFile()) {
            type = "本地文件";
        } else {
            String value =
                    subscription.getType();

            if (value == null ||
                    value.trim().isEmpty()) {
                type = "网络订阅";
            } else {
                type = value;
            }
        }

        holder.type.setText(
                "类型：" + type
        );

        StringBuilder status =
                new StringBuilder();

        if (subscription.hasContent()) {
            status.append("已有内容");
        } else {
            status.append("暂无内容");
        }

        if (subscription.isEnabled()) {
            status.append(" · 已启用");
        } else {
            status.append(" · 已停用");
        }

        long updateTime =
                subscription.getLastUpdateTime();

        if (updateTime > 0) {
            status.append(" · 已更新");
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
                listener.onSubscriptionClick(
                        subscriptions.get(adapterPosition),
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
        return subscriptions.size();
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
