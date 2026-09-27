package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
 * 视频详情信息适配器。
 *
 * 用于详情页展示：
 * 海报、标题、年份、地区、类型、导演、演员、
 * 备注、简介以及来源信息。
 */
public class VideoDetailAdapter
        extends RecyclerView.Adapter<VideoDetailAdapter.ViewHolder> {

    private final Context context;
    private final List<String> rows = new ArrayList<>();

    private Video video;

    public VideoDetailAdapter(Context context) {
        this.context = context;
    }

    public void setVideo(Video video) {
        this.video = video;

        rows.clear();

        if (video != null) {
            addRow("年份", video.getYear());
            addRow("地区", video.getArea());
            addRow("类型", video.getCategory());
            addRow("导演", video.getDirector());
            addRow("演员", video.getActor());
            addRow("备注", video.getRemarks());
            addRow("来源", video.getSourceName());
        }

        notifyDataSetChanged();
    }

    private void addRow(String title, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        rows.add(title + "\u0000" + value.trim());
    }

    public Video getVideo() {
        return video;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        root.setPadding(
                dp(8),
                dp(10),
                dp(8),
                dp(10)
        );

        ImageView poster = new ImageView(context);
        poster.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LinearLayout.LayoutParams posterParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(360)
                );

        root.addView(poster, posterParams);

        TextView title = new TextView(context);

        title.setTextSize(21);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setMaxLines(2);
        title.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(14);

        root.addView(title, titleParams);

        LinearLayout infoContainer =
                new LinearLayout(context);

        infoContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        infoParams.topMargin = dp(10);

        root.addView(
                infoContainer,
                infoParams
        );

        TextView description =
                new TextView(context);

        description.setTextSize(14);
        description.setLineSpacing(
                0,
                1.15f
        );

        description.setGravity(
                Gravity.TOP
        );

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin = dp(14);

        root.addView(
                description,
                descriptionParams
        );

        return new ViewHolder(
                root,
                poster,
                title,
                infoContainer,
                description
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        if (video == null) {
            return;
        }

        String title = video.getName();

        if (title == null || title.trim().isEmpty()) {
            title = "未命名视频";
        }

        holder.title.setText(title);

        String poster = video.getPoster();

        if (poster != null &&
                !poster.trim().isEmpty()) {

            ImageLoader.load(
                    context,
                    poster,
                    holder.poster
            );
        } else {
            holder.poster.setImageDrawable(null);
        }

        holder.infoContainer.removeAllViews();

        for (String row : rows) {
            int split =
                    row.indexOf('\u0000');

            if (split <= 0) {
                continue;
            }

            String label =
                    row.substring(0, split);

            String value =
                    row.substring(split + 1);

            TextView info =
                    new TextView(context);

            info.setTextSize(13);
            info.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            info.setText(
                    label + "： " + value
            );

            info.setMaxLines(3);
            info.setEllipsize(
                    TextUtils.TruncateAt.END
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            params.topMargin = dp(5);

            holder.infoContainer.addView(
                    info,
                    params
            );
        }

        String description =
                video.getDescription();

        if (description == null ||
                description.trim().isEmpty()) {

            holder.description.setVisibility(
                    View.GONE
            );

        } else {

            holder.description.setVisibility(
                    View.VISIBLE
            );

            holder.description.setText(
                    "简介\n" + description.trim()
            );
        }
    }

    @Override
    public int getItemCount() {
        return video == null ? 0 : 1;
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

        final ImageView poster;
        final TextView title;
        final LinearLayout infoContainer;
        final TextView description;

        public ViewHolder(
                @NonNull View itemView,
                ImageView poster,
                TextView title,
                LinearLayout infoContainer,
                TextView description
        ) {
            super(itemView);

            this.poster = poster;
            this.title = title;
            this.infoContainer = infoContainer;
            this.description = description;
        }
    }
        }
