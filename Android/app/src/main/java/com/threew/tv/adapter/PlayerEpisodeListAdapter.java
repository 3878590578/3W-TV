package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.Episode;

import java.util.ArrayList;
import java.util.List;

public class PlayerEpisodeListAdapter
        extends RecyclerView.Adapter<PlayerEpisodeListAdapter.ViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode, int position);
    }

    private final Context context;
    private final List<Episode> episodes = new ArrayList<>();

    private OnEpisodeClickListener listener;
    private int selectedPosition = -1;

    public PlayerEpisodeListAdapter(Context context) {
        this.context = context;
    }

    public void setOnEpisodeClickListener(
            OnEpisodeClickListener listener
    ) {
        this.listener = listener;
    }

    public void setEpisodes(List<Episode> values) {
        episodes.clear();

        if (values != null) {
            episodes.addAll(values);
        }

        if (selectedPosition >= episodes.size()) {
            selectedPosition = -1;
        }

        notifyDataSetChanged();
    }

    public void addEpisode(Episode episode) {
        if (episode == null) {
            return;
        }

        episodes.add(episode);
        notifyItemInserted(episodes.size() - 1);
    }

    public void clear() {
        episodes.clear();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public Episode getItem(int position) {
        if (position < 0 || position >= episodes.size()) {
            return null;
        }

        return episodes.get(position);
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayoutHolder layout = new LinearLayoutHolder(context);

        TextView number = new TextView(context);
        number.setGravity(Gravity.CENTER);
        number.setTextSize(13);
        number.setSingleLine(true);

        layout.addView(
                number,
                new android.widget.LinearLayout.LayoutParams(
                        dp(54),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        TextView name = new TextView(context);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setTextSize(13);
        name.setSingleLine(true);

        android.widget.LinearLayout.LayoutParams nameParams =
                new android.widget.LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );

        nameParams.leftMargin = dp(8);

        layout.addView(name, nameParams);

        return new ViewHolder(
                layout,
                number,
                name
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Episode episode = episodes.get(position);

        holder.number.setText(
                String.valueOf(episode.getNumber())
        );

        String name = episode.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "第 " + episode.getNumber() + " 集";
        }

        holder.name.setText(name);

        boolean selected = position == selectedPosition;

        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(dp(10));

        if (selected) {
            background.setColor(
                    Color.rgb(38, 111, 170)
            );

            holder.number.setTextColor(
                    Color.WHITE
            );

            holder.name.setTextColor(
                    Color.WHITE
            );

        } else if (episode.isWatched()) {
            background.setColor(
                    Color.rgb(30, 35, 43)
            );

            holder.number.setTextColor(
                    Color.rgb(105, 185, 245)
            );

            holder.name.setTextColor(
                    Color.rgb(170, 178, 190)
            );

        } else {
            background.setColor(
                    Color.rgb(20, 23, 31)
            );

            holder.number.setTextColor(
                    Color.rgb(190, 196, 205)
            );

            holder.name.setTextColor(
                    Color.rgb(225, 228, 234)
            );
        }

        holder.itemView.setBackground(background);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            selectedPosition = adapterPosition;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onEpisodeClick(
                        episodes.get(adapterPosition),
                        adapterPosition
                );
            }
        });
    }

    private int dp(int value) {
        return (int) (
                value
                        * context.getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    private static class LinearLayoutHolder
            extends android.widget.LinearLayout {

        LinearLayoutHolder(Context context) {
            super(context);

            setOrientation(
                    android.widget.LinearLayout.HORIZONTAL
            );

            setGravity(
                    Gravity.CENTER_VERTICAL
            );

            setPadding(
                    4,
                    4,
                    8,
                    4
            );

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            48
                    );

            params.bottomMargin = 6;

            setLayoutParams(params);
        }
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView number;
        private final TextView name;

        public ViewHolder(
                @NonNull View itemView,
                TextView number,
                TextView name
        ) {
            super(itemView);

            this.number = number;
            this.name = name;
        }
    }
}