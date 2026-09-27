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

import com.threew.tv.model.LocalFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地视频文件夹选择适配器。
 *
 * 用于：
 * - 设置页 / 本地视频页选择文件夹
 * - 显示文件夹名称和 SAF Uri
 * - 显示启用状态
 * - 支持多选
 * - 支持 Android TV 遥控器焦点
 */
public class LocalFolderChoiceAdapter
        extends RecyclerView.Adapter<LocalFolderChoiceAdapter.FolderChoiceViewHolder> {

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

    private boolean multiSelectMode = false;

    private final List<Long> selectedIds =
            new ArrayList<>();

    public LocalFolderChoiceAdapter(
            OnFolderClickListener clickListener
    ) {
        this(
                clickListener,
                null
        );
    }

    public LocalFolderChoiceAdapter(
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

        selectedIds.clear();

        notifyDataSetChanged();
    }

    public void setMultiSelectMode(
            boolean enabled
    ) {

        if (multiSelectMode == enabled) {
            return;
        }

        multiSelectMode =
                enabled;

        if (!multiSelectMode) {
            selectedIds.clear();
        }

        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void toggleSelection(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return;
        }

        LocalFolder folder =
                data.get(position);

        if (folder == null) {
            return;
        }

        long id =
                folder.getId();

        if (selectedIds.contains(id)) {

            selectedIds.remove(id);

        } else {

            selectedIds.add(id);
        }

        notifyItemChanged(position);
    }

    public boolean isSelected(
            int position
    ) {

        if (position < 0 ||
                position >= data.size()) {
            return false;
        }

        LocalFolder folder =
                data.get(position);

        return folder != null &&
                selectedIds.contains(
                        folder.getId()
                );
    }

    public List<Long> getSelectedIds() {
        return new ArrayList<>(
                selectedIds
        );
    }

    public List<LocalFolder> getSelectedFolders() {

        List<LocalFolder> result =
                new ArrayList<>();

        for (LocalFolder folder : data) {

            if (folder != null &&
                    selectedIds.contains(
                            folder.getId()
                    )) {

                result.add(folder);
            }
        }

        return result;
    }

    public void clearSelection() {

        selectedIds.clear();

        notifyDataSetChanged();
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

    @NonNull
    @Override
    public FolderChoiceViewHolder onCreateViewHolder(
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
                dp(parent, 8),
                dp(parent, 12),
                dp(parent, 8)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 76)
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

        TextView icon =
                new TextView(
                        parent.getContext()
                );

        icon.setText("▣");

        icon.setTextSize(23);

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(90, 190, 225)
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 40),
                        dp(parent, 50)
                )
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
                dp(parent, 10),
                0,
                dp(parent, 8),
                0
        );

        root.addView(
                content,
                contentParams
        );

        TextView name =
                new TextView(
                        parent.getContext()
                );

        name.setTextSize(14);

        name.setTextColor(
                Color.rgb(230, 234, 240)
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setMaxLines(1);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        content.addView(
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

        uri.setTextSize(9);

        uri.setTextColor(
                Color.rgb(105, 115, 128)
        );

        uri.setGravity(
                Gravity.CENTER_VERTICAL
        );

        uri.setMaxLines(1);

        uri.setEllipsize(
                TextUtils.TruncateAt.MIDDLE
        );

        content.addView(
                uri,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 20)
                )
        );

        TextView state =
                new TextView(
                        parent.getContext()
                );

        state.setTextSize(10);

        state.setGravity(
                Gravity.CENTER
        );

        state.setMaxLines(1);

        root.addView(
                state,
                new LinearLayout.LayoutParams(
                        dp(parent, 62),
                        dp(parent, 32)
                )
        );

        TextView check =
                new TextView(
                        parent.getContext()
                );

        check.setTextSize(18);

        check.setGravity(
                Gravity.CENTER
        );

        root.addView(
                check,
                new LinearLayout.LayoutParams(
                        dp(parent, 34),
                        dp(parent, 50)
                )
        );

        return new FolderChoiceViewHolder(
                root,
                icon,
                name,
                uri,
                state,
                check
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FolderChoiceViewHolder holder,
            int position
    ) {

        LocalFolder folder =
                data.get(position);

        if (folder == null) {
            return;
        }

        String name =
                folder.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "本地文件夹";
        }

        holder.name.setText(
                name
        );

        String treeUri =
                folder.getTreeUri();

        if (treeUri == null) {
            treeUri = "";
        }

        holder.uri.setText(
                treeUri
        );

        boolean enabled =
                folder.isEnabled();

        boolean selected =
                selectedIds.contains(
                        folder.getId()
                );

        if (enabled) {

            holder.state.setText(
                    "已启用"
            );

            holder.state.setTextColor(
                    Color.rgb(90, 205, 245)
            );

        } else {

            holder.state.setText(
                    "已停用"
            );

            holder.state.setTextColor(
                    Color.rgb(125, 130, 138)
            );
        }

        if (multiSelectMode) {

            holder.check.setVisibility(
                    View.VISIBLE
            );

            holder.check.setText(
                    selected
                            ? "✓"
                            : "○"
            );

            holder.check.setTextColor(
                    selected
                            ? Color.rgb(90, 210, 250)
                            : Color.rgb(105, 115, 128)
            );

        } else {

            holder.check.setVisibility(
                    View.VISIBLE
            );

            holder.check.setText(
                    "›"
            );

            holder.check.setTextColor(
                    Color.rgb(75, 90, 105)
            );
        }

        holder.itemView.setBackgroundColor(
                selected
                        ? Color.rgb(28, 65, 82)
                        : Color.rgb(20, 23, 30)
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        v.setBackgroundColor(
                                selected
                                        ? Color.rgb(34, 79, 99)
                                        : Color.rgb(34, 43, 53)
                        );

                        v.animate()
                                .scaleX(1.015f)
                                .scaleY(1.015f)
                                .setDuration(100)
                                .start();

                    } else {

                        v.setBackgroundColor(
                                selected
                                        ? Color.rgb(28, 65, 82)
                                        : Color.rgb(20, 23, 30)
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

                    LocalFolder clicked =
                            data.get(adapterPosition);

                    if (multiSelectMode) {

                        toggleSelection(
                                adapterPosition
                        );

                        return;
                    }

                    if (clickListener != null) {

                        clickListener.onFolderClick(
                                clicked,
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

                    LocalFolder clicked =
                            data.get(adapterPosition);

                    if (longClickListener != null) {

                        return longClickListener
                                .onFolderLongClick(
                                        clicked,
                                        adapterPosition
                                );
                    }

                    if (multiSelectMode) {

                        toggleSelection(
                                adapterPosition
                        );

                        return true;
                    }

                    return false;
                }
        );
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

    static class FolderChoiceViewHolder
            extends RecyclerView.ViewHolder {

        final TextView icon;
        final TextView name;
        final TextView uri;
        final TextView state;
        final TextView check;

        FolderChoiceViewHolder(
                @NonNull View itemView,
                TextView icon,
                TextView name,
                TextView uri,
                TextView state,
                TextView check
        ) {

            super(itemView);

            this.icon = icon;
            this.name = name;
            this.uri = uri;
            this.state = state;
            this.check = check;
        }
    }
}
