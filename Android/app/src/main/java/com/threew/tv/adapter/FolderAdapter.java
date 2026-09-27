package com.threew.tv.adapter;

import android.graphics.Color;
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

import com.threew.tv.model.LocalFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地视频文件夹适配器。
 *
 * 文件夹使用 Storage Access Framework（SAF）授权。
 * 本适配器只负责展示，不负责文件复制、移动或上传。
 */
public class FolderAdapter
        extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(
                LocalFolder folder,
                int position
        );
    }

    public interface OnFolderLongClickListener {
        boolean onFolderLongClick(
                LocalFolder folder,
                int position
        );
    }

    private final List<LocalFolder> data =
            new ArrayList<>();

    private final OnFolderClickListener clickListener;
    private final OnFolderLongClickListener longClickListener;

    public FolderAdapter(
            OnFolderClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public FolderAdapter(
            OnFolderClickListener clickListener,
            OnFolderLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<LocalFolder> folders
    ) {

        data.clear();

        if (folders != null) {
            data.addAll(folders);
        }

        notifyDataSetChanged();
    }

    public void addData(
            List<LocalFolder> folders
    ) {

        if (folders == null ||
                folders.isEmpty()) {
            return;
        }

        int start = data.size();

        data.addAll(folders);

        notifyItemRangeInserted(
                start,
                folders.size()
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

    public LocalFolder getItem(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    public List<LocalFolder> getItems() {
        return new ArrayList<>(data);
    }

    public void updateFolder(
            LocalFolder folder
    ) {

        if (folder == null) {
            return;
        }

        long id =
                folder.getId();

        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getId() == id) {

                data.set(i, folder);

                notifyItemChanged(i);

                return;
            }
        }
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(
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
                dp(parent, 12),
                dp(parent, 10),
                dp(parent, 12),
                dp(parent, 10)
        );

        root.setBackgroundColor(
                Color.rgb(20, 23, 31)
        );

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 82)
                );

        params.setMargins(
                0,
                0,
                0,
                dp(parent, 6)
        );

        root.setLayoutParams(params);

        ImageView icon =
                new ImageView(
                        parent.getContext()
                );

        icon.setImageResource(
                android.R.drawable.ic_menu_agenda
        );

        icon.setColorFilter(
                Color.rgb(65, 190, 255)
        );

        icon.setScaleType(
                ImageView.ScaleType.CENTER
        );

        icon.setBackgroundColor(
                Color.rgb(30, 38, 49)
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 54),
                        dp(parent, 58)
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

        info.setPadding(
                dp(parent, 12),
                0,
                dp(parent, 8),
                0
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

        name.setTextSize(14);

        name.setTypeface(
                null,
                Typeface.BOLD
        );

        name.setMaxLines(1);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        info.addView(name);

        TextView uri =
                new TextView(
                        parent.getContext()
                );

        uri.setTextColor(
                Color.rgb(105, 115, 130)
        );

        uri.setTextSize(9);

        uri.setMaxLines(2);

        uri.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        LinearLayout.LayoutParams uriParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        uriParams.setMargins(
                0,
                dp(parent, 4),
                0,
                0
        );

        info.addView(
                uri,
                uriParams
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setTextSize(10);

        state.setGravity(
                Gravity.CENTER
        );

        state.setPadding(
                dp(parent, 8),
                dp(parent, 4),
                dp(parent, 8),
                dp(parent, 4)
        );

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        dp(parent, 30)
                )
        );

        TextView arrow =
                new TextView(
                        parent.getContext()
                );

        arrow.setText("›");

        arrow.setTextColor(
                Color.rgb(90, 105, 120)
        );

        arrow.setTextSize(24);

        arrow.setGravity(
                Gravity.CENTER
        );

        root.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(parent, 28),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        return new FolderViewHolder(
                root,
                icon,
                name,
                uri,
                state,
                arrow
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FolderViewHolder holder,
            int position
    ) {

        LocalFolder folder =
                data.get(position);

        String name =
                safe(folder.getName());

        if (name.isEmpty()) {
            name = "本地视频文件夹";
        }

        holder.name.setText(name);

        String treeUri =
                safe(folder.getTreeUri());

        if (treeUri.isEmpty()) {

            holder.uri.setText(
                    "未设置文件夹授权"
            );

        } else {

            holder.uri.setText(
                    treeUri
            );
        }

        boolean enabled =
                folder.isEnabled();

        if (enabled) {

            holder.state.setText(
                    "启用"
            );

            holder.state.setTextColor(
                    Color.rgb(75, 210, 155)
            );

            holder.state.setBackgroundColor(
                    Color.rgb(25, 62, 48)
            );

        } else {

            holder.state.setText(
                    "停用"
            );

            holder.state.setTextColor(
                    Color.rgb(145, 155, 170)
            );

            holder.state.setBackgroundColor(
                    Color.rgb(43, 46, 54)
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

                        clickListener.onFolderClick(
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
                                .onFolderLongClick(
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

    static class FolderViewHolder
            extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView name;
        final TextView uri;
        final TextView state;
        final TextView arrow;

        FolderViewHolder(
                @NonNull View itemView,
                ImageView icon,
                TextView name,
                TextView uri,
                TextView state,
                TextView arrow
        ) {

            super(itemView);

            this.icon = icon;
            this.name = name;
            this.uri = uri;
            this.state = state;
            this.arrow = arrow;
        }
    }
}
