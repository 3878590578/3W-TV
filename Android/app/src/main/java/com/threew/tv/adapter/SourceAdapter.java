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

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源列表适配器。
 *
 * 用于：
 * - 视频源管理
 * - 启用 / 禁用状态
 * - 默认源显示
 * - 优先级显示
 * - 测试状态显示
 *
 * 支持手机、平板和 Android TV 遥控器焦点。
 */
public class SourceAdapter
        extends RecyclerView.Adapter<SourceAdapter.SourceViewHolder> {

    public interface OnSourceClickListener {
        void onSourceClick(VideoSource source, int position);
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

    public SourceAdapter(
            OnSourceClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public SourceAdapter(
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

    public void addData(
            List<VideoSource> sources
    ) {

        if (sources == null ||
                sources.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(sources);

        notifyItemRangeInserted(
                start,
                sources.size()
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
                dp(parent, 14),
                dp(parent, 10),
                dp(parent, 10),
                dp(parent, 10)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 84)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
        );

        root.setLayoutParams(params);

        TextView priority =
                new TextView(
                        parent.getContext()
                );

        priority.setGravity(
                Gravity.CENTER
        );

        priority.setTextColor(
                Color.rgb(55, 180, 255)
        );

        priority.setTextSize(13);

        priority.setTypeface(
                null,
                Typeface.BOLD
        );

        root.addView(
                priority,
                new LinearLayout.LayoutParams(
                        dp(parent, 38),
                        dp(parent, 50)
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

        TextView api =
                new TextView(
                        parent.getContext()
                );

        api.setTextColor(
                Color.rgb(115, 124, 140)
        );

        api.setTextSize(10);

        api.setMaxLines(1);

        api.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        LinearLayout.LayoutParams apiParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        apiParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        info.addView(
                api,
                apiParams
        );

        TextView remark =
                new TextView(
                        parent.getContext()
                );

        remark.setTextColor(
                Color.rgb(100, 110, 125)
        );

        remark.setTextSize(10);

        remark.setMaxLines(1);

        remark.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams remarkParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        remarkParams.setMargins(
                0,
                dp(parent, 3),
                0,
                0
        );

        info.addView(
                remark,
                remarkParams
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
                        dp(parent, 78),
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
                        dp(parent, 78),
                        dp(parent, 26)
                )
        );

        TextView defaultTag =
                new TextView(
                        parent.getContext()
                );

        defaultTag.setGravity(
                Gravity.CENTER
        );

        defaultTag.setTextSize(9);

        defaultTag.setTextColor(
                Color.rgb(55, 180, 255)
        );

        stateBox.addView(
                defaultTag,
                new LinearLayout.LayoutParams(
                        dp(parent, 78),
                        dp(parent, 24)
                )
        );

        return new SourceViewHolder(
                root,
                priority,
                name,
                api,
                remark,
                state,
                defaultTag
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SourceViewHolder holder,
            int position
    ) {

        VideoSource source =
                data.get(position);

        holder.priority.setText(
                String.valueOf(
                        source.getPriority()
                )
        );

        String name =
                safe(source.getName());

        if (name.isEmpty()) {
            name = "未命名来源";
        }

        holder.name.setText(name);

        holder.api.setText(
                safe(source.getApiUrl())
        );

        String remark =
                safe(source.getRemark());

        holder.remark.setText(
                remark
        );

        if (remark.isEmpty()) {
            holder.remark.setVisibility(
                    View.GONE
            );
        } else {
            holder.remark.setVisibility(
                    View.VISIBLE
            );
        }

        if (!source.isEnabled()) {

            holder.state.setText("已禁用");
            holder.state.setTextColor(
                    Color.rgb(120, 125, 135)
            );

        } else if (!source.isAvailable()) {

            holder.state.setText("不可用");
            holder.state.setTextColor(
                    Color.rgb(230, 150, 90)
            );

        } else {

            holder.state.setText("已启用");
            holder.state.setTextColor(
                    Color.rgb(65, 210, 145)
            );
        }

        if (source.isDefaultSource()) {

            holder.defaultTag.setText(
                    "默认"
            );

        } else {

            holder.defaultTag.setText(
                    ""
            );
        }

        boolean focused =
                holder.itemView.hasFocus();

        if (focused) {

            holder.itemView.setBackgroundColor(
                    Color.rgb(28, 58, 78)
            );

        } else {

            holder.itemView.setBackgroundColor(
                    Color.rgb(20, 23, 31)
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class SourceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView priority;
        final TextView name;
        final TextView api;
        final TextView remark;
        final TextView state;
        final TextView defaultTag;

        SourceViewHolder(
                @NonNull View itemView,
                TextView priority,
                TextView name,
                TextView api,
                TextView remark,
                TextView state,
                TextView defaultTag
        ) {
            super(itemView);

            this.priority = priority;
            this.name = name;
            this.api = api;
            this.remark = remark;
            this.state = state;
            this.defaultTag = defaultTag;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
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
