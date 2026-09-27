package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
 * 视频卡片网格适配器
 *
 * 适用于：
 * 首页推荐、分类列表、搜索结果、收藏等网格页面。
 *
 * 特点：
 * 1. 海报 + 标题 + 来源
 * 2. TV 遥控器焦点放大
 * 3. 手机触控正常
 * 4. 支持点击回调
 * 5. 不依赖额外 drawable XML
 */
public class VideoCardGridAdapter
        extends RecyclerView.Adapter<VideoCardGridAdapter.ViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(Video video, int position);
    }

    private final Context context;
    private final List<Video> videos = new ArrayList<>();

    private OnVideoClickListener listener;

    public VideoCardGridAdapter(Context context) {
        this.context = context;
    }

    public VideoCardGridAdapter(
            Context context,
            List<Video> data
    ) {
        this.context = context;

        if (data != null) {
            videos.addAll(data);
        }
    }

    public void setOnVideoClickListener(
            OnVideoClickListener listener
    ) {
        this.listener = listener;
    }

    public void setVideos(List<Video> data) {
        videos.clear();

        if (data != null) {
            videos.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void addVideos(List<Video> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = videos.size();
        videos.addAll(data);
        notifyItemRangeInserted(start, data.size());
    }

    public void clear() {
        videos.clear();
        notifyDataSetChanged();
    }

    public Video getItem(int position) {
        if (position < 0 || position >= videos.size()) {
            return null;
        }

        return videos.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        FrameLayout root = new FrameLayout(context);

        root.setFocusable(true);
        root.setClickable(true);

        int margin = dp(5);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rootParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );

        root.setLayoutParams(rootParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        root.addView(
                content,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        ImageView poster = new ImageView(context);

        poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        poster.setAdjustViewBounds(true);

        content.addView(
                poster,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(205)
                )
        );

        TextView title = new TextView(context);

        title.setTextSize(14);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setMaxLines(2);
        title.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(8);

        content.addView(title, titleParams);

        TextView source = new TextView(context);

        source.setTextSize(11);
        source.setSingleLine(true);
        source.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );
        source.setAlpha(0.58f);

        LinearLayout.LayoutParams sourceParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        sourceParams.topMargin = dp(3);

        content.addView(source, sourceParams);

        return new ViewHolder(
                root,
                poster,
                title,
                source
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Video video = videos.get(position);

        String name = video.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "未命名视频";
        }

        holder.title.setText(name);

        String sourceName = video.getSourceName();

        if (sourceName == null || sourceName.trim().isEmpty()) {
            sourceName = "未知来源";
        }

        holder.source.setText("来源：" + sourceName);

        String poster = video.getPoster();

        if (poster != null && !poster.trim().isEmpty()) {
            ImageLoader.load(
                    context,
                    poster,
                    holder.poster
            );
        } else {
            holder.poster.setImageDrawable(null);
        }

        holder.itemView.setScaleX(1.0f);
        holder.itemView.setScaleY(1.0f);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (listener != null) {
                listener.onVideoClick(
                        videos.get(adapterPosition),
                        adapterPosition
                );
            }
        });

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {
                        holder.title.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.BOLD
                        );

                        v.animate()
                                .scaleX(1.045f)
                                .scaleY(1.045f)
                                .setDuration(130)
                                .start();

                    } else {

                        holder.title.setTypeface(
                                Typeface.DEFAULT,
                                Typeface.NORMAL
                        );

                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(130)
                                .start();
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    private int dp(int value) {
        float density =
                context.getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(value * density);
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView poster;
        final TextView title;
        final TextView source;

        public ViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                TextView source
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.source = source;
        }
    }
        }
