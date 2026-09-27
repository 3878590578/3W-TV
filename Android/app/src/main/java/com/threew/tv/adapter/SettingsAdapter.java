package com.threew.tv.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置页面通用列表适配器。
 *
 * 用于：
 * - 设置分类
 * - 设置项
 * - 当前值展示
 * - TV 遥控器焦点操作
 *
 * 不直接修改设置。
 * 点击事件交给 SettingsActivity 处理。
 */
public class SettingsAdapter
        extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public static class SettingItem {

        private String key;
        private String title;
        private String summary;
        private String value;
        private int type;

        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_SWITCH = 1;
        public static final int TYPE_VALUE = 2;
        public static final int TYPE_SECTION = 3;

        public SettingItem(
                String key,
                String title,
                String summary,
                String value,
                int type
        ) {

            this.key = key;
            this.title = title;
            this.summary = summary;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getValue() {
            return value;
        }

        public int getType() {
            return type;
        }

        public void setSummary(
                String summary
        ) {
            this.summary = summary;
        }

        public void setValue(
                String value
        ) {
            this.value = value;
        }
    }

    public interface OnSettingClickListener {
        void onSettingClick(
                SettingItem item,
                int position
        );
    }

    private final List<SettingItem> data =
            new ArrayList<>();

    private final OnSettingClickListener listener;

    public SettingsAdapter(
            OnSettingClickListener listener
    ) {

        this.listener = listener;
    }

    public void setData(
            List<SettingItem> items
    ) {

        data.clear();

        if (items != null) {
            data.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void addItem(
            SettingItem item
    ) {

        if (item == null) {
            return;
        }

        int position = data.size();

        data.add(item);

        notifyItemInserted(position);
    }

    public void clear() {

        int size = data.size();

        data.clear();

        if (size > 0) {
            notifyItemRangeRemoved(0, size);
        }
    }

    public SettingItem getItem(
            int position
    ) {
