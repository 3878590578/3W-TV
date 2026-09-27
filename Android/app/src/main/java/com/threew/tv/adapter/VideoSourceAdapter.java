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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源管理适配器。
 *
 * 用于来源管理页面：
 * - 来源名称
 * - API 地址
 * - 来源类型
 * - 启用 / 停用状态
 * - 默认来源
 * - 可用状态
 * - 优先级
 * - Android TV 焦点操作
 */
public class VideoSourceAdapter
        extends RecyclerView.Adapter<VideoSourceAdapter.VideoSourceViewHolder> {

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

    private final List<VideoSource> data =
            new ArrayList<>();

    private final OnSourceClickListener clickListener;
    private final OnSourceLongClickListener longClickListener;

    public VideoSourceAdapter(
            OnSourceClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public VideoSourceAdapter(
            OnSourceClickListener clickListener,
            OnSourceLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<VideoSource> sources
    ) {

        data.clear();

        if (sources != null) {
            data.addAll(sources);
        }

        notifyDataSetChanged();
    }

    public void add(
            VideoSource source
    ) {

        if (source == null) {
            return;
        }

        data.add(source);

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
            VideoSource source
    ) {

        if (position < 0 ||
                position >= data.size() ||
                source == null) {
            return;
        }

        data.set(
                position,
                source
        );

        notifyItemChanged(position);
    }

    public void move(
            int fromPosition,
            int toPosition
    ) {

        if (fromPosition < 0 ||
                toPosition < 0 ||
                fromPosition >= data.size() ||
                toPosition >= data.size() ||
                fromPosition == toPosition) {

            return;
        }

        VideoSource source =
                data.remove(fromPosition);

        data.add(
                toPosition,
                source
        );

        notifyItemMoved(
                fromPosition,
                toPosition
        );
    }

    public VideoSource getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return null;
        }

        return data.get(position);
    }

    public List<VideoSource> getItems() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public VideoSourceViewHolder onCreateViewHolder(
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
                        dp(parent, 104)
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

        TextView api =
                new TextView(
                        parent.getContext()
                );

        api.setTextSize(9);

        api.setTextColor(
                Color.rgb(115, 125, 138)
        );

        api.setGravity(
                Gravity.CENTER_VERTICAL
        );

        api.setMaxLines(1);

        api.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        LinearLayout.LayoutParams apiParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 23)
                );

        apiParams.topMargin =
                dp(parent, 2);

        root.addView(
                api,
                apiParams
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

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 25)
                );

        root.addView(
                infoRow,
                infoParams
        );

        TextView type =
                createInfoText(parent);

        infoRow.addView(
                type,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                )
        );

        TextView priority =
                createInfoText(parent);

        priority.setGravity(
                Gravity.CENTER
        );

        infoRow.addView(
                priority,
                new LinearLayout.LayoutParams(
                        dp(parent, 70),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView defaultState =
                createInfoText(parent);

        defaultState.setGravity(
                Gravity.CENTER
        );

        infoRow.addView(
                defaultState,
                new LinearLayout.LayoutParams(
                        dp(parent, 74),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new VideoSourceViewHolder(
                root,
                name,
                state,
                api,
                type,
                priority,
                defaultState
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull VideoSourceViewHolder holder,
            int position
    ) {

        VideoSource source =
                data.get(position);

        String name =
                source.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "未命名来源";
        }

        holder.name.setText(
                name
        );

        String api =
                source.getApiUrl();

        if (api == null) {
            api = "";
        }

        holder.api.setText(
                api
        );

        String type =
                source.getType();

        if (type == null ||
                type.trim().isEmpty()) {

            type = "API";
        }

        if (source.isCmsSource()) {
            type = "CMS";
        }

        holder.type.setText(
                "类型：" + type
        );

        holder.priority.setText(
                "优先级：" +
                        source.getPriority()
        );

        holder.defaultState.setText(
                source.isDefaultSource()
                        ? "★ 默认"
                        : "非默认"
        );

        if (source.isDefaultSource()) {

            holder.defaultState.setTextColor(
                    Color.rgb(250, 190, 80)
            );

        } else {

            holder.defaultState.setTextColor(
                    Color.rgb(105, 115, 128)
            );
        }

        if (!source.isEnabled()) {

            holder.state.setText(
                    "已停用"
            );

            holder.state.setTextColor(
                    Color.rgb(125, 130, 138)
            );

        } else if (source.isAvailable()) {

            holder.state.setText(
                    "可用"
            );

            holder.state.setTextColor(
                    Color.rgb(90, 210, 245)
            );

        } else {

            holder.state.setText(
                    "待检测"
            );

            holder.state.setTextColor(
                    Color.rgb(205, 170, 90)
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

                        clickListener.onSourceClick(
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
                                .onSourceLongClick(
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

    static class VideoSourceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView state;
        final TextView api;
        final TextView type;
        final TextView priority;
        final TextView defaultState;

        VideoSourceViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView state,
                TextView api,
                TextView type,
                TextView priority,
                TextView defaultState
        ) {

            super(itemView);

            this.name = name;
            this.state = state;
            this.api = api;
            this.type = type;
            this.priority = priority;
            this.defaultState = defaultState;
        }
    }
}
