package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Episode;
import com.threew.tv.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 剧集卡片适配器
 *
 * 用于详情页、播放器选集面板等位置。
 *
 * 支持：
 * 1. 集数
 * 2. 剧集名称
 * 3. 播放进度
 * 4. 已观看状态
 * 5. 已下载状态
 * 6. TV 遥控器焦点
 * 7. 点击选择剧集
 */
public class EpisodeCardAdapter
        extends RecyclerView.Adapter<EpisodeCardAdapter.ViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode, int position);
    }

    private final Context context;
    private final List<Episode> episodes = new ArrayList<>();

    private OnEpisodeClickListener listener;
    private int selectedPosition = -1;

    public EpisodeCardAdapter(Context context) {
        this.context = context;
    }

    public EpisodeCardAdapter(
            Context context,
            List<Episode> data
    ) {
        this.context = context;

        if (data != null) {
            episodes.addAll(data);
        }
    }

    public void setOnEpisodeClickListener(
            OnEpisodeClickListener listener
    ) {
        this.listener = listener;
    }

    public void setEpisodes(List<Episode> data) {
        episodes.clear();

        if (data != null) {
            episodes.addAll(data);
        }

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void addEpisodes(List<Episode> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int start = episodes.size();

        episodes.addAll(data);

        notifyItemRangeInserted(
                start,
                data.size()
        );
    }

    public void clear() {
        episodes.clear();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= episodes.size()) {
            return;
        }

        int oldPosition = selectedPosition;

        selectedPosition = position;

        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }

        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Episode getItem(int position) {
        if (position < 0 || position >= episodes.size()) {
            return null;
        }

        return episodes.get(position);
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

        int margin = dp(4);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(62)
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
        content.setGravity(Gravity.CENTER_VERTICAL);

        int padding = dp(12);

        content.setPadding(
                padding,
                dp(7),
                padding,
                dp(7)
        );

        root.addView(
                content,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView number = new TextView(context);

        number.setTextSize(14);
        number.setGravity(Gravity.CENTER_VERTICAL);
        number.setSingleLine(true);

        content.addView(
                number,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView name = new TextView(context);

        name.setTextSize(11);
        name.setAlpha(0.65f);
        name.setSingleLine(true);
        name.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        nameParams.topMargin = dp(2);

        content.addView(
                name,
                nameParams
        );

        TextView status = new TextView(context);

        status.setTextSize(9);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setSingleLine(true);
        status.setAlpha(0.65f);

        FrameLayout.LayoutParams statusParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.END | Gravity.CENTER_VERTICAL
                );

        statusParams.rightMargin = dp(10);

        root.addView(
                status,
                statusParams
        );

        return new ViewHolder(
                root,
                number,
                name,
                status
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Episode episode = episodes.get(position);

        String episodeNumber =
                episode.getNumber();

        if (episodeNumber == null ||
                episodeNumber.trim().isEmpty()) {

            episodeNumber =
                    String.valueOf(position + 1);
        }

        String episodeName =
                episode.getName();

        if (episodeName == null ||
                episodeName.trim().isEmpty()) {

            episodeName =
                    "第 " + episodeNumber + " 集";
        }

        holder.number.setText(
                "第 " + episodeNumber + " 集"
        );

        holder.name.setText(episodeName);

        StringBuilder status =
                new StringBuilder();

        long duration =
                episode.getDurationMs();

        long positionMs =
                episode.getPositionMs();

        if (episode.isWatched()) {
            status.append("已看");
        } else if (positionMs > 0) {
            status.append("观看 ");
            status.append(
                    FormatUtils.formatDuration(positionMs)
            );

            if (duration > 0) {
                status.append(" / ");
                status.append(
                        FormatUtils.formatDuration(duration)
                );
            }
        }

        if (episode.isDownloaded()) {
            if (status.length() > 0) {
                status.append(" · ");
            }

            status.append("已下载");
        }

        holder.status.setText(status.toString());

        boolean selected =
                position == selectedPosition;

        if (selected) {
            holder.number.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            holder.number.setTextSize(15);

            holder.itemView.setScaleX(1.025f);
            holder.itemView.setScaleY(1.025f);

        } else {
            holder.number.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            );

            holder.number.setTextSize(14);

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
                listener.onEpisodeClick(
                        episodes.get(adapterPosition),
                        adapterPosition
                );
            }
        });

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.045f)
                                .scaleY(1.045f)
                                .setDuration(120)
                                .start();

                        holder.number.setTypeface(
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

                        holder.number.setTypeface(
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
        return episodes.size();
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

        final TextView number;
        final TextView name;
        final TextView status;

        public ViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView name,
                TextView status
        ) {
            super(itemView);

            this.number = number;
            this.name = name;
            this.status = status;
        }
    }
}
