package com.threew.tv.player;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放地址管理器
 *
 * 负责保存当前媒体地址以及备用地址。
 */
public class PlayerUrlManager {

    private final List<String> urls =
            new ArrayList<>();

    private int selectedIndex = -1;

    public PlayerUrlManager() {
    }

    public void setUrl(String url) {
        clear();

        if (isValid(url)) {
            urls.add(url.trim());
            selectedIndex = 0;
        }
    }

    public void setUrls(List<String> values) {
        clear();

        if (values == null) {
            return;
        }

        for (String value : values) {
            addUrl(value);
        }

        if (!urls.isEmpty()) {
            selectedIndex = 0;
        }
    }

    public void addUrl(String url) {
        if (!isValid(url)) {
            return;
        }

        String value = url.trim();

        if (!urls.contains(value)) {
            urls.add(value);

            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
        }
    }

    public void removeUrl(int index) {
        if (index < 0 ||
                index >= urls.size()) {
            return;
        }

        urls.remove(index);

        if (urls.isEmpty()) {
            selectedIndex = -1;
        } else if (selectedIndex >= urls.size()) {
            selectedIndex = urls.size() - 1;
        }
    }

    public List<String> getUrls() {
        return Collections.unmodifiableList(
                new ArrayList<>(urls)
        );
    }

    public int getCount() {
        return urls.size();
    }

    public String getSelectedUrl() {
        if (selectedIndex < 0 ||
                selectedIndex >= urls.size()) {
            return null;
        }

        return urls.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean select(int index) {
        if (index < 0 ||
                index >= urls.size()) {
            return false;
        }

        selectedIndex = index;
        return true;
    }

    public String next() {
        if (urls.isEmpty()) {
            return null;
        }

        if (selectedIndex + 1 >= urls.size()) {
            return null;
        }

        selectedIndex++;

        return getSelectedUrl();
    }

    public String previous() {
        if (urls.isEmpty()) {
            return null;
        }

        if (selectedIndex <= 0) {
            return null;
        }

        selectedIndex--;

        return getSelectedUrl();
    }

    public boolean hasNext() {
        return selectedIndex >= 0 &&
                selectedIndex + 1 < urls.size();
    }

    public boolean hasPrevious() {
        return selectedIndex > 0 &&
                selectedIndex < urls.size();
    }

    public boolean isHls() {
        String url = getSelectedUrl();

        if (url == null) {
            return false;
        }

        String lower =
                url.toLowerCase();

        return lower.contains(".m3u8")
                || lower.contains("m3u8");
    }

    public boolean isHttp() {
        String url = getSelectedUrl();

        if (url == null) {
            return false;
        }

        return url.startsWith("http://")
                || url.startsWith("https://");
    }

    public boolean isValidSelectedUrl() {
        return isValid(
                getSelectedUrl()
        );
    }

    public Uri getSelectedUri() {
        String url = getSelectedUrl();

        if (!isValid(url)) {
            return null;
        }

        return Uri.parse(url);
    }

    public void clear() {
        urls.clear();
        selectedIndex = -1;
    }

    private boolean isValid(String url) {
        if (url == null ||
                url.trim().isEmpty()) {
            return false;
        }

        String value =
                url.trim().toLowerCase();

        return value.startsWith("http://")
                || value.startsWith("https://");
    }
}