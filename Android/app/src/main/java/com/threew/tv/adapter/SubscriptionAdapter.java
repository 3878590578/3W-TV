package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
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
 * 订阅列表适配器。
 *
 * 支持：
 * - 网络订阅
 * - 本地文件
 * - 启用 / 禁用状态
 * - 最后更新时间
 * - 内容是否已经导入
 * - 长按操作
 */
public class SubscriptionAdapter
        extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

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

    public SubscriptionAdapter(
            OnSubscriptionClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public SubscriptionAdapter(
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

    public void addData(
            List<Subscription> subscriptions
    ) {

        if (subscriptions == null ||
                subscriptions.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(subscriptions);

        notifyItemRangeInserted(
                start,
                subscriptions.size()
        );
    }

    public void clear() {

        int size = data.size();

        data.clear();

        if (size > 0) {
            notifyItemRangeRemoved(
                    0,
                    size
            );
        }
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
                LinearLayout.HORIZONTAL
        );

        root.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.setPadding(
                dp(parent, 14),
                dp(parent, 10),
                dp(parent, 10),
                dp(parent, 10)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 88)
                );

        rootParams.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
        );

        root.setLayoutParams(rootParams);

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextSize(22);

        icon.setTextColor(
                Color.rgb(55, 180, 255)
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 46),
                        dp(parent, 54)
                )
        );

        LinearLayout info =
                new LinearLayout(
                        parent.getContext()
                );

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        info.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextColor(
                Color.rgb(245, 247, 250)
        );

        name.setTextSize(15);

        name.setTypeface(
                null,
                Typeface.BOLD
        );

        name.setMaxLines(1);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        info.addView(name);

        TextView url =
                new TextView(
                        parent.getContext()
                );

        url.setTextColor(
                Color.rgb(115, 124, 140)
        );

        url.setTextSize(10);

        url.setMaxLines(1);

        url.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        LinearLayout.LayoutParams urlParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        urlParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        info.addView(
                url,
                urlParams
        );

        TextView update =
                new TextView(
                        parent.getContext()
                );

        update.setTextColor(
                Color.rgb(100, 110, 125)
        );

        update.setTextSize(10);

        update.setMaxLines(1);

        update.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams updateParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        updateParams.setMargins(
                0,
                dp(parent, 3),
                0,
                0
        );

        info.addView(
                update,
                updateParams
        );

        LinearLayout stateBox =
                new LinearLayout(
                        parent.getContext()
                );

        stateBox.setOrientation(
                LinearLayout.VERTICAL
        );

        stateBox.setGravity(
                Gravity.CENTER
        );

        root.addView(
                stateBox,
                new LinearLayout.LayoutParams(
                        dp(parent, 68),
                        dp(parent, 60)
                )
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setGravity(
                Gravity.CENTER
        );

        state.setTextSize(10);

        stateBox.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 68),
                        dp(parent, 28)
                )
        );

        TextView contentState =
                new TextView(
                        parent.getContext()
                );

        contentState.setGravity(
                Gravity.CENTER
        );

        contentState.setTextSize(9);

        contentState.setTextColor(
                Color.rgb(120, 130, 145)
        );

        stateBox.addView(
                contentState,
                new LinearLayout.LayoutParams(
                        dp(parent, 68),
                        dp(parent, 24)
                )
        );

        return new SubscriptionViewHolder(
                root,
                icon,
                name,
                url,
                update,
                state,
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

        if (subscription.isLocalFile()) {

            holder.icon.setText("▣");

        } else {

            holder.icon.setText("↻");
        }

        String name =
                safe(subscription.getName());

        if (name.isEmpty()) {
            name = "未命名订阅";
        }

        holder.name.setText(name);

        String url =
                safe(subscription.getUrl());

        if (subscription.isLocalFile()) {

            String localUri =
                    safe(subscription.getLocalUri());

            holder.url.setText(
                    localUri.isEmpty()
                            ? "本地文件"
                            : localUri
            );

        } else {

            holder.url.setText(
                    url.isEmpty()
                            ? "无订阅地址"
                            : url
            );
        }

        long updateTime =
                subscription.getLastUpdateTime();

        if (updateTime > 0) {

            holder.update.setText(
                    "更新：" +
                            formatTime(updateTime)
            );

        } else {

            holder.update.setText(
                    "尚未更新"
            );
        }

        if (subscription.isEnabled()) {

            holder.state.setText("已启用");
            holder.state.setTextColor(
                    Color.rgb(65, 210, 145)
            );

        } else {

            holder.state.setText("已禁用");
            holder.state.setTextColor(
                    Color.rgb(120, 125, 135)
            );
        }

        if (subscription.hasContent()) {

            holder.contentState.setText(
                    "已导入"
            );

            holder.contentState.setTextColor(
                    Color.rgb(55, 180, 255)
            );

        } else {

            holder.contentState.setText(
                    "无内容"
            );

            holder.contentState.setTextColor(
                    Color.rgb(120, 130, 145)
            );
        }

        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    v.setBackgroundColor(
                            hasFocus
                                    ? Color.rgb(28, 58, 78)
                                    : Color.rgb(20, 23, 31)
                    );
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class SubscriptionViewHolder
            extends RecyclerView.ViewHolder {

        final TextView icon;
        final TextView name;
        final TextView url;
        final TextView update;
        final TextView state;
        final TextView contentState;

        SubscriptionViewHolder(
                @NonNull View itemView,
                TextView icon,
                TextView name,
                TextView url,
                TextView update,
                TextView state,
                TextView contentState
        ) {
            super(itemView);

            this.icon = icon;
            this.name = name;
            this.url = url;
            this.update = update;
            this.state = state;
            this.contentState = contentState;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String formatTime(
            long timestamp
    ) {

        try {

            return new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
            ).format(
                    new Date(timestamp)
            );

        } catch (Exception e) {

            return "";
        }
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
}
