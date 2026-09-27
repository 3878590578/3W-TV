package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
 * 用于：
 * - 本地视频目录管理
 * - 多文件夹展示
 * - 启用 / 停用状态展示
 * - TV 遥控器焦点操作
 *
 * 这里只管理 SAF 文件夹 Uri，
 * 不复制、不移动、不上传用户本地文件。
 */
public class LocalFolderAdapter
        extends RecyclerView.Adapter<LocalFolderAdapter.FolderViewHolder> {

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

    private final List<LocalFolder> folders =
            new ArrayList<>();

    private final OnFolderClickListener clickListener;
    private final OnFolderLongClickListener longClickListener;

    public LocalFolderAdapter(
            OnFolderClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public LocalFolderAdapter(
            OnFolderClickListener clickListener,
            OnFolderLongClickListener longClickListener
    ) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setData(
            List<LocalFolder> data
    ) {

        folders.clear();

        if (data != null) {
            folders.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void add(
            LocalFolder folder
    ) {

        if (folder == null) {
            return;
        }

        folders.add(folder);

        notifyItemInserted(
                folders.size() - 1
        );
    }

    public void remove(
            int position
    ) {

        if (position < 0 ||
                position >= folders.size()) {

            return;
        }

        folders.remove(position);

        notifyItemRemoved(position);
    }

    public void clear() {

        int count =
                folders.size();

        if (count == 0) {
            return;
        }

        folders.clear();

        notifyItemRangeRemoved(
                0,
                count
        );
    }

    public LocalFolder getItem(
            int position
    ) {

        if (position < 0 ||
                position >= folders.size()) {

            return null;
        }

        return folders.get(position);
    }

    public List<LocalFolder> getItems() {
        return new ArrayList<>(folders);
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
                        dp(parent, 78)
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

        TextView folderIcon =
                new TextView(
                        parent.getContext()
                );

        folderIcon.setText("▰");

        folderIcon.setTextSize(19);

        folderIcon.setGravity(
                Gravity.CENTER
        );

        folderIcon.setTextColor(
                Color.rgb(
                        105,
                        200,
                        225
                )
        );

        root.addView(
                folderIcon,
                new LinearLayout.LayoutParams(
                        dp(parent, 44),
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        LinearLayout textContainer =
                new LinearLayout(
                        parent.getContext()
                );

        textContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        textContainer.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                textContainer,
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

        name.setTextSize(14);

        name.setTextColor(
                Color.rgb(
                        238,
                        242,
                        247
                )
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setSingleLine(true);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        textContainer.addView(
                name,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 28)
                )
        );

        TextView uri =
                new TextView(
                        parent.getContext()
                );

        uri.setTextSize(8);

        uri.setTextColor(
                Color.rgb(
                        90,
                        101,
                        114
                )
        );

        uri.setGravity(
                Gravity.CENTER_VERTICAL
        );

        uri.setSingleLine(true);

        uri.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        textContainer.addView(
                uri,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        TextView status =
                new TextView(
                        parent.getContext()
                );

        status.setTextSize(9);

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                dp(parent, 7),
                dp(parent, 3),
                dp(parent, 7),
                dp(parent, 3)
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(parent, 58),
                        dp(parent, 28)
                )
        );

        return new FolderViewHolder(
                root,
                name,
                uri,
                status
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FolderViewHolder holder,
            int position
    ) {

        LocalFolder folder =
                folders.get(position);

        String name =
                safe(folder.getName());

        if (name.isEmpty()) {
            name = "本地文件夹";
        }

        holder.name.setText(
                name
        );

        String treeUri =
                safe(folder.getTreeUri());

        holder.uri.setText(
                treeUri.isEmpty()
                        ? "未记录目录 Uri"
                        : treeUri
        );

        boolean enabled =
                folder.isEnabled();

        holder.status.setText(
                enabled
                        ? "已启用"
                        : "已停用"
        );

        holder.status.setTextColor(
                enabled
                        ? Color.rgb(
                                110,
                                215,
                                185
                        )
                        : Color.rgb(
                                145,
                                150,
                                158
                        )
        );

        holder.status.setBackground(
                createStatusBackground(
                        enabled
                )
        );

        applyItemBackground(
                holder.itemView,
                false
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    applyItemBackground(
                            v,
                            hasFocus
                    );

                    if (hasFocus) {

                        v.animate()
                                .scaleX(1.012f)
                                .scaleY(1.012f)
                                .setDuration(100)
                                .start();

                    } else {

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

                        clickListener.onFolderClick(
                                folders.get(
                                        adapterPosition
                                ),
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
                                        folders.get(
                                                adapterPosition
                                        ),
                                        adapterPosition
                                );
                    }

                    return false;
                }
        );
    }

    private void applyItemBackground(
            View view,
            boolean focused
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                focused
                        ? Color.rgb(
                                32,
                                45,
                                56
                        )
                        : Color.rgb(
                                20,
                                23,
                                30
                        )
        );

        drawable.setStroke(
                dpValue(
                        view,
                        1
                ),
                focused
                        ? Color.rgb(
                                75,
                                130,
                                150
                        )
                        : Color.rgb(
                                43,
                                51,
                                61
                        )
        );

        drawable.setCornerRadius(
                dpValue(
                        view,
                        9
                )
        );

        view.setBackground(
                drawable
        );
    }

    private GradientDrawable createStatusBackground(
            boolean enabled
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                enabled
                        ? Color.rgb(
                                22,
                                55,
                                52
                        )
                        : Color.rgb(
                                40,
                                42,
                                47
                        )
        );

        drawable.setStroke(
                dpValue(
                        1
                ),
                enabled
                        ? Color.rgb(
                                60,
                                135,
                                120
                        )
                        : Color.rgb(
                                70,
                                75,
                                83
                        )
        );

        drawable.setCornerRadius(
                dpValue(
                        7
                )
        );

        return drawable;
    }

    private int dpValue(
            int value
    ) {

        return Math.round(
                value
        );
    }

    private int dpValue(
            View view,
            int value
    ) {

        return Math.round(
                value *
                        view.getResources()
                                .getDisplayMetrics()
                                .density
        );
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

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder
            extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView uri;
        final TextView status;

        FolderViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView uri,
                TextView status
        ) {

            super(itemView);

            this.name = name;
            this.uri = uri;
            this.status = status;
        }
    }
}
