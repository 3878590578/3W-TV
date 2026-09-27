package com.threew.tv.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Video;
import com.threew.tv.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果适配器。
 *
 * 功能：
 * - 显示搜索结果
 * - 显示海报、名称、年份、地区、分类、来源
 * - 支持同名合并后的来源数量
 * - 支持手机 / 平板 / Android TV
 * - 支持遥控器焦点
 */
public class SearchResultAdapter
        extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(
                Video video,
                int position
        );
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(
                Video video,
                int position
        );
    }

    private final List<Video> data =
            new ArrayList<>();

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public SearchResultAdapter(
            OnItemClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public SearchResultAdapter(
            OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<Video> videos
    ) {

        data.clear();

        if (videos != null) {
            data.addAll(videos);
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<Video> videos
    ) {

        if (videos == null ||
                videos.isEmpty()) {
            return;
        }

        int start =
                data.size();

        data.addAll(videos);

        notifyItemRangeInserted(
                start,
                videos.size()
        );
    }

    public void clear() {

        int size =
                data.size();

        data.clear();

        if (size > 0) {

            notifyItemRangeRemoved(
                    0,
                    size
            );
        }
    }

    public Video getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {

            return null;
        }

        return data.get(position);
    }

    public List<Video> getItems() {
        return new ArrayList<>(data);
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(
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
                dp(parent, 8),
                dp(parent, 8),
                dp(parent, 8),
                dp(parent, 8)
        );

        root.setFocusable(true);
        root.setClickable(true);

        root.setBackgroundColor(
                Color.rgb(19, 22, 29)
        );

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 124)
                );

        rootParams.setMargins(
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4),
                dp(parent, 4)
        );

        root.setLayoutParams(rootParams);

        FrameLayout posterContainer =
                new FrameLayout(
                        parent.getContext()
                );

        posterContainer.setLayoutParams(
                new LinearLayout.LayoutParams(
                        dp(parent, 76),
                        dp(parent, 108)
                )
        );

        ImageView poster =
                new ImageView(
                        parent.getContext()
                );

        poster.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        poster.setBackgroundColor(
                Color.rgb(30, 34, 42)
        );

        posterContainer.addView(
                poster,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView sourceTag =
                new TextView(
                        parent.getContext()
                );

        sourceTag.setTextColor(Color.WHITE);
        sourceTag.setTextSize(8);
        sourceTag.setGravity(Gravity.CENTER);
        sourceTag.setMaxLines(1);
        sourceTag.setEllipsize(
                TextUtils.TruncateAt.END
        );
        sourceTag.setPadding(
                dp(parent, 4),
                0,
                dp(parent, 4),
                0
        );
        sourceTag.setBackgroundColor(
                Color.rgb(35, 115, 150)
        );

        FrameLayout.LayoutParams sourceParams =
                new FrameLayout.LayoutParams(
                        dp(parent, 56),
                        dp(parent, 20),
                        Gravity.BOTTOM | Gravity.START
                );

        sourceParams.setMargins(
                dp(parent, 4),
                0,
                0,
                dp(parent, 4)
        );

        posterContainer.addView(
                sourceTag,
                sourceParams
        );

        root.addView(
                posterContainer
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
                dp(parent, 12),
                dp(parent, 4),
                dp(parent, 6),
                dp(parent, 4)
        );

        root.addView(
                content,
                contentParams
        );

        TextView title =
                createText(
                        parent,
                        16,
                        Color.rgb(245, 247, 250)
                );

        title.setMaxLines(2);

        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView meta =
                createText(
                        parent,
                        11,
                        Color.rgb(150, 160, 173)
                );

        meta.setMaxLines(1);

        meta.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams metaParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        metaParams.topMargin =
                dp(parent, 7);

        content.addView(
                meta,
                metaParams
        );

        TextView category =
                createText(
                        parent,
                        10,
                        Color.rgb(105, 185, 220)
                );

        category.setMaxLines(1);

        category.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams categoryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        categoryParams.topMargin =
                dp(parent, 5);

        content.addView(
                category,
                categoryParams
        );

        TextView remarks =
                createText(
                        parent,
                        10,
                        Color.rgb(115, 123, 135)
                );

        remarks.setMaxLines(1);

        remarks.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams remarksParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        remarksParams.topMargin =
                dp(parent, 4);

        content.addView(
                remarks,
                remarksParams
        );

        TextView arrow =
                new TextView(
                        parent.getContext()
                );

        arrow.setText("›");
        arrow.setTextSize(24);
        arrow.setTextColor(
                Color.rgb(75, 88, 103)
        );
        arrow.setGravity(
                Gravity.CENTER
        );

        root.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(parent, 30),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new SearchResultViewHolder(
                root,
                poster,
                sourceTag,
                title,
                meta,
                category,
                remarks,
                arrow
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SearchResultViewHolder holder,
            int position
    ) {

        Video video =
                data.get(position);

        holder.title.setText(
                safe(video.getName())
        );

        String source =
                safe(video.getSourceName());

        if (source.isEmpty()) {
            source = "未知来源";
        }

        holder.sourceTag.setText(
                source
        );

        ImageLoader.load(
                holder.itemView.getContext(),
                video.getPoster(),
                holder.poster
        );

        StringBuilder meta =
                new StringBuilder();

        if (!safe(video.getYear()).isEmpty()) {

            meta.append(
                    safe(video.getYear())
            );
        }

        if (!safe(video.getArea()).isEmpty()) {

            if (meta.length() > 0) {
                meta.append("  ·  ");
            }

            meta.append(
                    safe(video.getArea())
            );
        }

        if (meta.length() == 0) {
            meta.append("暂无资料");
        }

        holder.meta.setText(
                meta.toString()
        );

        String category =
                safe(video.getCategory());

        if (category.isEmpty()) {
            category = "视频";
        }

        holder.category.setText(
                category
        );

        String remarks =
                safe(video.getRemarks());

        if (remarks.isEmpty()) {
            remarks = "点击查看详情";
        }

        holder.remarks.setText(
                remarks
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                Color.rgb(27, 48, 62)
                        );

                        holder.arrow.setTextColor(
                                Color.rgb(90, 205, 250)
                        );

                        v.animate()
                                .scaleX(1.015f)
                                .scaleY(1.015f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackgroundColor(
                                Color.rgb(19, 22, 29)
                        );

                        holder.arrow.setTextColor(
                                Color.rgb(75, 88, 103)
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

                        clickListener.onItemClick(
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
                                .onItemLongClick(
                                        data.get(adapterPosition),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private TextView createText(
            ViewGroup parent,
            float size,
            int color
    ) {

        TextView text =
                new TextView(
                        parent.getContext()
                );

        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(
                Gravity.CENTER_VERTICAL
        );

        return text;
    }

    private String safe(
            String value
    ) {

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

    static class SearchResultViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView sourceTag;
        final TextView title;
        final TextView meta;
        final TextView category;
        final TextView remarks;
        final TextView arrow;

        SearchResultViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView sourceTag,
                TextView title,
                TextView meta,
                TextView category,
                TextView remarks,
                TextView arrow
        ) {

            super(itemView);

            this.poster = poster;
            this.sourceTag = sourceTag;
            this.title = title;
            this.meta = meta;
            this.category = category;
            this.remarks = remarks;
            this.arrow = arrow;
        }
    }
}
