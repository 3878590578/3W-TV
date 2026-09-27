package com.threew.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threew.tv.R;
import com.threew.tv.model.LocalFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地视频文件夹卡片适配器
 *
 * 用于：
 * - 本地视频页面
 * - 文件夹选择页面
 * - 文件夹管理页面
 *
 * 特点：
 * - 深色科技风
 * - 显示文件夹名称
 * - 显示 SAF Uri
 * - 显示启用状态
 * - 支持点击
 * - 支持长按
 */
public class LocalFolderCardAdapter
        extends RecyclerView.Adapter<LocalFolderCardAdapter.ViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(LocalFolder folder);
    }

    public interface OnFolderLongClickListener {
        boolean onFolderLongClick(LocalFolder folder);
    }

    private final Context context;
    private final List<LocalFolder> folders = new ArrayList<>();

    private OnFolderClickListener clickListener;
    private OnFolderLongClickListener longClickListener;

    public LocalFolderCardAdapter(Context context) {
        this.context = context;
    }

    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnFolderLongClickListener(OnFolderLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setItems(List<LocalFolder> items) {
        folders.clear();

        if (items != null) {
            folders.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void addItem(LocalFolder folder) {
        if (folder == null) {
            return;
        }

        folders.add(folder);
        notifyItemInserted(folders.size() - 1);
    }

    public void removeItem(LocalFolder folder) {
        if (folder == null) {
            return;
        }

        int index = folders.indexOf(folder);

        if (index >= 0) {
            folders.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void clear() {
        folders.clear();
        notifyDataSetChanged();
    }

    public LocalFolder getItem(int position) {
        if (position < 0 || position >= folders.size()) {
            return null;
        }

        return folders.get(position);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        LocalFolder folder = folders.get(position);

        holder.bind(folder);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFolderClick(folder);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onFolderLongClick(folder);
            }

            return false;
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);

            applyStyle();
        }

        private void applyStyle() {
            itemView.setPadding(
                    dp(16),
                    dp(14),
                    dp(16),
                    dp(14)
            );

            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.rgb(25, 29, 36));
            background.setCornerRadius(dp(14));

            itemView.setBackground(background);

            title.setTextColor(Color.WHITE);
            title.setTextSize(16);

            subtitle.setTextColor(Color.rgb(150, 158, 170));
            subtitle.setTextSize(12);

            title.setMaxLines(1);
            subtitle.setMaxLines(2);
        }

        public void bind(LocalFolder folder) {
            if (folder == null) {
                title.setText("未知文件夹");
                subtitle.setText("");
                return;
            }

            String name = folder.getName();

            if (name == null || name.trim().isEmpty()) {
                name = "本地文件夹";
            }

            title.setText(name);

            String uri = folder.getTreeUri();

            if (uri == null) {
                uri = "";
            }

            String state = folder.isEnabled()
                    ? "已启用"
                    : "已停用";

            if (uri.length() > 80) {
                uri = uri.substring(0, 77) + "...";
            }

            subtitle.setText(state + "  ·  " + uri);

            if (folder.isEnabled()) {
                title.setAlpha(1.0f);
                subtitle.setAlpha(1.0f);
            } else {
                title.setAlpha(0.55f);
                subtitle.setAlpha(0.45f);
            }
        }

        private int dp(int value) {
            return (int) (value * itemView.getResources()
                    .getDisplayMetrics().density + 0.5f);
        }
    }
        }
