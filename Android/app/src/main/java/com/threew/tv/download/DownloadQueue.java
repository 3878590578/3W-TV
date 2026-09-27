package com.threew.tv.download;

import com.threew.tv.model.DownloadItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 下载任务队列。
 *
 * 负责：
 * - 等待任务
 * - 下载中任务
 * - 暂停任务
 * - 失败任务
 * - 已完成任务
 *
 * 真正的下载由 DownloadManager 负责。
 */
public class DownloadQueue {

    private final List<DownloadItem> items =
            new ArrayList<>();

    /**
     * 添加任务。
     *
     * 已存在相同任务时不重复添加。
     */
    public synchronized boolean add(
            DownloadItem item
    ) {
        if (item == null) {
            return false;
        }

        if (contains(
                item.getId()
        )) {
            return false;
        }

        items.add(item);

        sort();

        return true;
    }

    /**
     * 批量添加。
     */
    public synchronized int addAll(
            List<DownloadItem> list
    ) {
        if (list == null
                || list.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (DownloadItem item : list) {
            if (add(item)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 删除任务。
     *
     * 这里只从内存队列删除，
     * 数据库由 DownloadDao 管理。
     */
    public synchronized boolean remove(
            long id
    ) {
        for (int i = items.size() - 1;
             i >= 0;
             i--) {

            DownloadItem item =
                    items.get(i);

            if (item != null
                    && item.getId() == id) {

                items.remove(i);

                return true;
            }
        }

        return false;
    }

    /**
     * 删除指定对象。
     */
    public synchronized boolean remove(
            DownloadItem item
    ) {
        if (item == null) {
            return false;
        }

        return remove(
                item.getId()
        );
    }

    /**
     * 清空内存队列。
     */
    public synchronized void clear() {
        items.clear();
    }

    /**
     * 是否包含任务。
     */
    public synchronized boolean contains(
            long id
    ) {
        for (DownloadItem item : items) {

            if (item != null
                    && item.getId() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取任务。
     */
    public synchronized DownloadItem get(
            long id
    ) {
        for (DownloadItem item : items) {

            if (item != null
                    && item.getId() == id) {
                return item;
            }
        }

        return null;
    }

    /**
     * 获取全部任务。
     *
     * 返回副本，避免外部直接修改内部队列。
     */
    public synchronized List<DownloadItem> getAll() {
        return new ArrayList<>(
                items
        );
    }

    /**
     * 获取等待中的任务。
     */
    public synchronized List<DownloadItem> getWaiting() {
        return getByStatus(
                DownloadItem.STATUS_WAITING
        );
    }

    /**
     * 获取下载中的任务。
     */
    public synchronized List<DownloadItem> getDownloading() {
        return getByStatus(
                DownloadItem.STATUS_DOWNLOADING
        );
    }

    /**
     * 获取暂停任务。
     */
    public synchronized List<DownloadItem> getPaused() {
        return getByStatus(
                DownloadItem.STATUS_PAUSED
        );
    }

    /**
     * 获取失败任务。
     */
    public synchronized List<DownloadItem> getFailed() {
        return getByStatus(
                DownloadItem.STATUS_FAILED
        );
    }

    /**
     * 获取已完成任务。
     */
    public synchronized List<DownloadItem> getCompleted() {
        return getByStatus(
                DownloadItem.STATUS_COMPLETED
        );
    }

    /**
     * 获取指定状态。
     */
    public synchronized List<DownloadItem> getByStatus(
            String status
    ) {
        List<DownloadItem> result =
                new ArrayList<>();

        if (status == null) {
            return result;
        }

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (status.equals(
                    item.getStatus()
            )) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * 等待任务数量。
     */
    public synchronized int waitingCount() {
        return getWaiting().size();
    }

    /**
     * 下载中数量。
     */
    public synchronized int downloadingCount() {
        return getDownloading().size();
    }

    /**
     * 暂停数量。
     */
    public synchronized int pausedCount() {
        return getPaused().size();
    }

    /**
     * 失败数量。
     */
    public synchronized int failedCount() {
        return getFailed().size();
    }

    /**
     * 完成数量。
     */
    public synchronized int completedCount() {
        return getCompleted().size();
    }

    /**
     * 总任务数量。
     */
    public synchronized int size() {
        return items.size();
    }

    /**
     * 是否为空。
     */
    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 将任务状态设置为等待。
     */
    public synchronized boolean markWaiting(
            long id
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        sort();

        return true;
    }

    /**
     * 将任务状态设置为下载中。
     */
    public synchronized boolean markDownloading(
            long id
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_DOWNLOADING
        );

        sort();

        return true;
    }

    /**
     * 将任务状态设置为暂停。
     */
    public synchronized boolean markPaused(
            long id
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_PAUSED
        );

        sort();

        return true;
    }

    /**
     * 将任务状态设置为失败。
     */
    public synchronized boolean markFailed(
            long id,
            String message
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_FAILED
        );

        item.setErrorMessage(
                message
        );

        sort();

        return true;
    }

    /**
     * 将任务设置为完成。
     */
    public synchronized boolean markCompleted(
            long id
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_COMPLETED
        );

        item.setProgress(100);

        sort();

        return true;
    }

    /**
     * 更新进度。
     */
    public synchronized boolean updateProgress(
            long id,
            long downloadedBytes,
            long totalBytes
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        item.updateProgress(
                downloadedBytes,
                totalBytes
        );

        return true;
    }

    /**
     * 获取下一个等待任务。
     */
    public synchronized DownloadItem pollWaiting() {
        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (DownloadItem.STATUS_WAITING.equals(
                    item.getStatus()
            )) {

                item.setStatus(
                        DownloadItem.STATUS_DOWNLOADING
                );

                return item;
            }
        }

        return null;
    }

    /**
     * 将下载中的任务全部恢复为等待状态。
     *
     * App / Service 异常退出后使用。
     */
    public synchronized int resetDownloading() {
        int count = 0;

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (DownloadItem.STATUS_DOWNLOADING.equals(
                    item.getStatus()
            )) {

                item.setStatus(
                        DownloadItem.STATUS_WAITING
                );

                count++;
            }
        }

        sort();

        return count;
    }

    /**
     * 重新加入失败任务。
     */
    public synchronized boolean retry(
            long id
    ) {
        DownloadItem item =
                get(id);

        if (item == null) {
            return false;
        }

        if (!DownloadItem.STATUS_FAILED.equals(
                item.getStatus()
        )) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        item.setErrorMessage(
                ""
        );

        item.setRetryCount(
                item.getRetryCount() + 1
        );

        sort();

        return true;
    }

    /**
     * 暂停全部下载任务。
     */
    public synchronized int pauseAll() {
        int count = 0;

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (DownloadItem.STATUS_WAITING.equals(
                    item.getStatus()
            )
                    || DownloadItem.STATUS_DOWNLOADING.equals(
                    item.getStatus()
            )) {

                item.setStatus(
                        DownloadItem.STATUS_PAUSED
                );

                count++;
            }
        }

        sort();

        return count;
    }

    /**
     * 恢复全部暂停任务。
     */
    public synchronized int resumeAll() {
        int count = 0;

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (DownloadItem.STATUS_PAUSED.equals(
                    item.getStatus()
            )) {

                item.setStatus(
                        DownloadItem.STATUS_WAITING
                );

                count++;
            }
        }

        sort();

        return count;
    }

    /**
     * 按状态和创建时间排序。
     *
     * 等待任务优先，其次下载中，
     * 最后是暂停/失败/完成。
     */
    private void sort() {
        Collections.sort(
                items,
                new Comparator<DownloadItem>() {
                    @Override
                    public int compare(
                            DownloadItem a,
                            DownloadItem b
                    ) {
                        if (a == null
                                && b == null) {
                            return 0;
                        }

                        if (a == null) {
                            return 1;
                        }

                        if (b == null) {
                            return -1;
                        }

                        int statusA =
                                statusPriority(
                                        a.getStatus()
                                );

                        int statusB =
                                statusPriority(
                                        b.getStatus()
                                );

                        if (statusA != statusB) {
                            return Integer.compare(
                                    statusA,
                                    statusB
                            );
                        }

                        return Long.compare(
                                a.getCreateTime(),
                                b.getCreateTime()
                        );
                    }
                }
        );
    }

    /**
     * 状态优先级。
     */
    private int statusPriority(
            String status
    ) {
        if (DownloadItem.STATUS_DOWNLOADING.equals(
                status
        )) {
            return 0;
        }

        if (DownloadItem.STATUS_WAITING.equals(
                status
        )) {
            return 1;
        }

        if (DownloadItem.STATUS_PAUSED.equals(
                status
        )) {
            return 2;
        }

        if (DownloadItem.STATUS_FAILED.equals(
                status
        )) {
            return 3;
        }

        if (DownloadItem.STATUS_COMPLETED.equals(
                status
        )) {
            return 4;
        }

        if (DownloadItem.STATUS_DELETED.equals(
                status
        )) {
            return 5;
        }

        return 6;
    }
}
