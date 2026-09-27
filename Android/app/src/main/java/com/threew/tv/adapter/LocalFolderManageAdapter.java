package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.model.LocalFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地文件夹管理适配器。
 *
 * 用于设置页面 / 本地视频页面管理多个 SAF 文件夹：
 * - 显示文件夹名称
 * - 显示 Uri
 * - 启用 / 停用
 * - 点击选择
 * - 长按管理
 */
public class LocalFolderManageAdapter
        extends RecyclerView.Adapter<LocalFolderManageAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(
                LocalFolder folder,
                int position
        );
    }

    public interface OnFolderEnabledChangeListener {
        void onEnabledChanged(
                LocalFolder folder,
                boolean enabled,
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
    private final OnFolderEnabledChangeListener enabledListener;
    private final OnFolderLongClickListener longClickListener;

    public LocalFolderManageAdapter(
            OnFolderClickListener clickListener,
            OnFolderEnabledChangeListener enabledListener,
            OnFolderLongClickListener longClickListener
    ) {

        this.clickListener =
                clickListener;

        this.enabledListener =
                enabledListener;

        this.longClickListener =
                longClickListener;
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
                dp(parent, 12),
                dp(parent, 8),
                dp(parent, 10),
                dp(parent, 8)
        );

        root.setFocusable(true);
        root.setClickable(true);

        RecyclerView.LayoutParams rootParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(parent, 82)
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

        icon.setText("▰");

        icon.setTextSize(18);

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(
                        100,
                        200,
                        225
                )
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(parent, 42),
                        ViewGroup.LayoutParams.MATCH_PARENT
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
                dp(parent, 8),
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
                Color.rgb(
                        235,
                        240,
                        246
                )
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setSingleLine(true);

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

        uri.setTextSize(8);

        uri.setTextColor(
                Color.rgb(
                        92,
                        103,
                        115
                )
        );

        uri.setGravity(
                Gravity.CENTER_VERTICAL
        );

        uri.setSingleLine(true);

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

        CheckBox enabled =
                new CheckBox(
                        parent.getContext()
                );

        enabled.setButtonTintList(
                new android.content.res.ColorStateList(
                        new int[][]{
                                new int[]{
                                        android.R.attr.state_checked
                                },
                                new int[]{}
                        },
                        new int[]{
                                Color.rgb(
                                        90,
                                        205,
                                        225
                                ),
                                Color.rgb(
                                        100,
                                        108,
                                        118
                                )
                        }
                )
        );

        enabled.setText(
                "启用"
        );

        enabled.setTextSize(9);

        enabled.setTextColor(
                Color.rgb(
                        160,
                        170,
                        180
                )
        );

        enabled.setGravity(
                Gravity.CENTER
        );

        root.addView(
                enabled,
                new LinearLayout.LayoutParams(
                        dp(parent, 62),
                        dp(parent, 42)
                )
        );

        return new FolderViewHolder(
                root,
                name,
                uri,
                enabled
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FolderViewHolder holder,
            int position
    ) {

        LocalFolder folder =
                folders.get(position);

        holder.name.setText(
                safe(
                        folder.getName()
                )
        );

        holder.uri.setText(
                safe(
                        folder.getTreeUri()
                )
        );

        holder.enabled.setOnCheckedChangeListener(
                null
        );

        holder.enabled.setChecked(
                folder.isEnabled()
        );

        holder.enabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    LocalFolder current =
                            folders.get(
                                    adapterPosition
                            );

                    current.setEnabled(
                            isChecked
                    );

                    if (enabledListener != null) {

                        enabledListener.onEnabledChanged(
                                current,
                                isChecked,
                                adapterPosition
                        );
                    }
                }
        );

        applyBackground(
                holder.itemView,
                false
        );

        holder.itemView.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    applyBackground(
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

    private void applyBackground(
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
                                135,
                                155
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
        final CheckBox enabled;

        FolderViewHolder(
                @NonNull View itemView,
                TextView name,
                TextView uri,
                CheckBox enabled
        ) {

            super(itemView);

            this.name = name;
            this.uri = uri;
            this.enabled = enabled;
        }
    }
}
