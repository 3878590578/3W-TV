package com.threew.tv.local;

import android.content.Context;
import android.net.Uri;

import com.threew.tv.model.LocalFolder;
import com.threew.tv.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 本地视频管理器。
 *
 * 负责：
 * - 管理多个本地视频目录
 * - 添加 / 删除 / 启用 / 禁用目录
 * - 扫描本地视频
 * - 根据 Uri 查找视频
 * - 获取本地视频播放地址
 *
 * 不复制、不移动、不上传用户原始视频。
 */
public class LocalVideoManager {

    private final Context context;
    private final LocalFolderManager folderManager;
    private final LocalVideoScanner scanner;

    public LocalVideoManager(Context context) {
        this.context =
                context.getApplicationContext();

        this.folderManager =
                new LocalFolderManager(
                        this.context
                );

        this.scanner =
                new LocalVideoScanner(
                        this.context
                );
    }

    /**
     * 添加本地目录。
     *
     * Uri 必须来自 ACTION_OPEN_DOCUMENT_TREE。
     */
    public LocalFolder addFolder(
            Uri treeUri,
            String name
    ) {
        if (treeUri == null) {
            return null;
        }

        return folderManager.addFolder(
                treeUri,
                name
        );
    }

    /**
     * 添加目录并尝试获取持久化权限。
     */
    public LocalFolder addFolderWithPermission(
            Uri treeUri,
            String name,
            int takeFlags
    ) {
        if (treeUri == null) {
            return null;
        }

        boolean granted =
                folderManager.takePersistablePermission(
                        treeUri,
                        takeFlags
                );

        if (!granted) {
            return null;
        }

        return folderManager.addFolder(
                treeUri,
                name
        );
    }

    /**
     * 获取全部本地目录。
     */
    public List<LocalFolder> getFolders() {
        return folderManager.getLocalFolders();
    }

    /**
     * 获取启用中的目录。
     */
    public List<LocalFolder> getEnabledFolders() {
        return folderManager.getEnabledFolders();
    }

    /**
     * 获取指定目录。
     */
    public LocalFolder getFolder(
            long id
    ) {
        return folderManager.getLocalFolder(
                id
        );
    }

    /**
     * 启用目录。
     */
    public boolean enableFolder(
            long id
    ) {
        return folderManager.setEnabled(
                id,
                true
        );
    }

    /**
     * 禁用目录。
     */
    public boolean disableFolder(
            long id
    ) {
        return folderManager.setEnabled(
                id,
                false
        );
    }

    /**
     * 切换目录启用状态。
     */
    public boolean toggleFolder(
            long id
    ) {
        LocalFolder folder =
                getFolder(id);

        if (folder == null) {
            return false;
        }

        return folderManager.setEnabled(
                id,
                !folder.isEnabled()
        );
    }

    /**
     * 删除目录记录。
     *
     * 不删除用户真实视频。
     */
    public boolean removeFolder(
            long id
    ) {
        return folderManager.deleteFolder(
                id
        );
    }

    /**
     * 重新扫描全部目录。
     */
    public List<Video> scan() {
        return scanner.scanAll();
    }

    /**
     * 刷新全部本地视频。
     */
    public List<Video> refresh() {
        return scanner.refresh();
    }

    /**
     * 扫描指定目录。
     */
    public List<Video> scanFolder(
            long folderId
    ) {
        LocalFolder folder =
                getFolder(folderId);

        if (folder == null) {
            return new ArrayList<>();
        }

        return scanner.scanFolder(
                folder
        );
    }

    /**
     * 查找指定本地 Uri。
     */
    public Video findByUri(
            String uri
    ) {
        return scanner.findByUri(
                uri
        );
    }

    /**
     * 判断 Uri 是否属于已添加的本地目录。
     */
    public boolean isLocalUri(
            String uri
    ) {
        return scanner.isLocalVideoUri(
                uri
        );
    }

    /**
     * 获取 Video 的原始播放 Uri。
     */
    public Uri getPlayUri(
            Video video
    ) {
        return scanner.getVideoUri(
                video
        );
    }

    /**
     * 获取本地视频数量。
     */
    public int getVideoCount() {
        return scanner.countAll();
    }

    /**
     * 获取指定目录视频数量。
     */
    public int getVideoCount(
            long folderId
    ) {
        LocalFolder folder =
                getFolder(folderId);

        if (folder == null) {
            return 0;
        }

        return scanner.countFolder(
                folder
        );
    }

    /**
     * 检查某个目录是否仍然可以访问。
     */
    public boolean isFolderAvailable(
            long folderId
    ) {
        LocalFolder folder =
                getFolder(folderId);

        if (folder == null) {
            return false;
        }

        return folderManager.isFolderAvailable(
                folder
        );
    }

    /**
     * 检查所有启用目录。
     */
    public List<LocalFolder> getUnavailableFolders() {
        List<LocalFolder> result =
                new ArrayList<>();

        List<LocalFolder> folders =
                getFolders();

        if (folders == null) {
            return result;
        }

        for (LocalFolder folder : folders) {

            if (folder == null
                    || !folder.isEnabled()) {
                continue;
            }

            if (!folderManager.isFolderAvailable(
                    folder
            )) {
                result.add(folder);
            }
        }

        return result;
    }

    /**
     * 更新目录名称。
     */
    public boolean renameFolder(
            long id,
            String name
    ) {
        return folderManager.renameFolder(
                id,
                name
        );
    }

    /**
     * 重新取得目录显示名称。
     */
    public String getFolderDisplayName(
            long id
    ) {
        LocalFolder folder =
                getFolder(id);

        if (folder == null) {
            return "";
        }

        return folderManager.getDisplayName(
                folder
        );
    }

    /**
     * 获取目录 Uri。
     */
    public Uri getFolderUri(
            long id
    ) {
        LocalFolder folder =
                getFolder(id);

        if (folder == null) {
            return null;
        }

        return folderManager.getTreeUri(
                folder
        );
    }

    /**
     * 获取当前扫描到的全部视频。
     *
     * 返回新的列表，调用方可以安全修改。
     */
    public List<Video> getVideos() {
        List<Video> videos =
                scanner.scanAll();

        if (videos == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                videos
        );
    }

    /**
     * 根据名称搜索本地视频。
     */
    public List<Video> search(
            String keyword
    ) {
        List<Video> videos =
                getVideos();

        if (keyword == null
                || keyword.trim().isEmpty()) {
            return videos;
        }

        String query =
                keyword.trim()
                        .toLowerCase();

        List<Video> result =
                new ArrayList<>();

        for (Video video : videos) {

            if (video == null) {
                continue;
            }

            String name =
                    video.getName();

            String original =
                    video.getOriginalName();

            boolean match =
                    name != null
                            && name.toLowerCase()
                            .contains(query);

            if (!match
                    && original != null) {
                match =
                        original.toLowerCase()
                                .contains(query);
            }

            if (match) {
                result.add(video);
            }
        }

        return result;
    }

    /**
     * 按目录筛选视频。
     */
    public List<Video> getVideosByFolder(
            long folderId
    ) {
        List<Video> videos =
                scanFolder(folderId);

        if (videos == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                videos
        );
    }

    /**
     * 删除失效目录。
     *
     * 只删除应用中的目录记录，
     * 不影响用户真实文件。
     */
    public int removeUnavailableFolders() {
        List<LocalFolder> folders =
                getUnavailableFolders();

        int count = 0;

        for (LocalFolder folder : folders) {

            if (folder == null) {
                continue;
            }

            if (removeFolder(
                    folder.getId()
            )) {
                count++;
            }
        }

        return count;
    }

    /**
     * 清理所有本地目录记录。
     *
     * 不删除真实视频。
     */
    public int clearFolders() {
        List<LocalFolder> folders =
                getFolders();

        if (folders == null
                || folders.isEmpty()) {
            return 0;
        }

        int count = 0;

        /*
         * 复制一份，避免删除过程中修改原列表。
         */
        List<LocalFolder> copy =
                new ArrayList<>(
                        folders
                );

        for (LocalFolder folder : copy) {

            if (folder == null) {
                continue;
            }

            if (removeFolder(
                    folder.getId()
            )) {
                count++;
            }
        }

        return count;
    }
}
