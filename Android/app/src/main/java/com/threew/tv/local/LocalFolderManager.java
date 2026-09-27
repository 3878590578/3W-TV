package com.threew.tv.local;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.threew.tv.database.DatabaseHelper;
import com.threew.tv.database.VideoDao;
import com.threew.tv.model.LocalFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 本地视频目录管理器。
 *
 * 使用 Android Storage Access Framework：
 * - 用户自行选择目录
 * - 保存目录 Uri 权限
 * - 不复制视频
 * - 不移动视频
 * - 不上传视频
 * - 每次扫描直接读取用户当前目录
 * - 文件新增/删除可以自然反映
 *
 * 支持多个本地视频目录。
 */
public class LocalFolderManager {

    private final Context context;
    private final DatabaseHelper databaseHelper;

    public LocalFolderManager(Context context) {
        this.context =
                context.getApplicationContext();

        databaseHelper =
                new DatabaseHelper(this.context);
    }

    /**
     * 保存用户选择的目录。
     *
     * 调用方应在 ACTION_OPEN_DOCUMENT_TREE
     * 返回 RESULT_OK 后，把 Uri 传进来。
     */
    public LocalFolder addFolder(
            Uri treeUri
    ) {
        if (treeUri == null) {
            return null;
        }

        if (!takePersistablePermission(treeUri)) {
            return null;
        }

        DocumentFile directory =
                DocumentFile.fromTreeUri(
                        context,
                        treeUri
                );

        if (directory == null
                || !directory.exists()
                || !directory.isDirectory()) {
            return null;
        }

        String uri =
                treeUri.toString();

        LocalFolder existing =
                findByUri(uri);

        if (existing != null) {
            existing.setEnabled(true);

            save(existing);

            return existing;
        }

        LocalFolder folder =
                new LocalFolder();

        folder.setTreeUri(uri);

        String name =
                directory.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "本地视频";
        }

        folder.setName(name);

        folder.setCreateTime(
                System.currentTimeMillis()
        );

        folder.setLastScanTime(0);

        folder.setEnabled(true);

        save(folder);

        return folder;
    }

    /**
     * 保存目录。
     */
    public boolean save(
            LocalFolder folder
    ) {
        if (folder == null
                || folder.getTreeUri() == null
                || folder.getTreeUri()
                .trim()
                .isEmpty()) {
            return false;
        }

        try {
            long id =
                    databaseHelper.saveLocalFolder(
                            folder
                    );

            if (folder.getId() <= 0) {
                folder.setId(id);
            }

            return id > 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取全部目录。
     */
    public List<LocalFolder> getAll() {
        try {
            List<LocalFolder> list =
                    databaseHelper.getLocalFolders();

            if (list == null) {
                return new ArrayList<>();
            }

            sortFolders(list);

            return list;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取启用的目录。
     */
    public List<LocalFolder> getEnabledFolders() {
        List<LocalFolder> result =
                new ArrayList<>();

        for (LocalFolder folder : getAll()) {
            if (folder != null
                    && folder.isEnabled()) {
                result.add(folder);
            }
        }

        return result;
    }

    /**
     * 根据数据库 ID 获取目录。
     */
    public LocalFolder getById(
            long id
    ) {
        try {
            return databaseHelper.getLocalFolder(id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据 Uri 查找目录。
     */
    public LocalFolder findByUri(
            String treeUri
    ) {
        if (treeUri == null
                || treeUri.trim().isEmpty()) {
            return null;
        }

        for (LocalFolder folder : getAll()) {

            if (folder == null) {
                continue;
            }

            String current =
                    folder.getTreeUri();

            if (treeUri.equals(current)) {
                return folder;
            }
        }

        return null;
    }

    /**
     * 启用/禁用目录。
     */
    public boolean setEnabled(
            long id,
            boolean enabled
    ) {
        LocalFolder folder =
                getById(id);

        if (folder == null) {
            return false;
        }

        folder.setEnabled(enabled);

        return save(folder);
    }

    /**
     * 删除目录记录。
     *
     * 同时释放 Uri 持久化权限。
     *
     * 不删除用户实际视频文件。
     */
    public boolean remove(
            long id
    ) {
        LocalFolder folder =
                getById(id);

        if (folder == null) {
            return false;
        }

        releasePersistablePermission(
                folder.getTreeUri()
        );

        try {
            return databaseHelper.deleteLocalFolder(
                    id
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除所有目录记录。
     *
     * 不删除实际视频。
     */
    public boolean removeAll() {
        boolean result = true;

        for (LocalFolder folder : getAll()) {

            if (folder == null) {
                continue;
            }

            if (!remove(folder.getId())) {
                result = false;
            }
        }

        return result;
    }

    /**
     * 获取目录 Uri。
     */
    public Uri getTreeUri(
            LocalFolder folder
    ) {
        if (folder == null) {
            return null;
        }

        String value =
                folder.getTreeUri();

        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Uri.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取目录 DocumentFile。
     */
    public DocumentFile getDirectory(
            LocalFolder folder
    ) {
        Uri uri =
                getTreeUri(folder);

        if (uri == null) {
            return null;
        }

        try {
            return DocumentFile.fromTreeUri(
                    context,
                    uri
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断目录是否仍然可访问。
     */
    public boolean isAccessible(
            LocalFolder folder
    ) {
        DocumentFile directory =
                getDirectory(folder);

        return directory != null
                && directory.exists()
                && directory.isDirectory();
    }

    /**
     * 更新最后扫描时间。
     */
    public boolean updateScanTime(
            LocalFolder folder
    ) {
        if (folder == null) {
            return false;
        }

        folder.setLastScanTime(
                System.currentTimeMillis()
        );

        return save(folder);
    }

    /**
     * 获取目录下第一层文件。
     *
     * 真正的视频扫描由 LocalVideoScanner 完成。
     */
    public List<DocumentFile> listFiles(
            LocalFolder folder
    ) {
        List<DocumentFile> result =
                new ArrayList<>();

        DocumentFile directory =
                getDirectory(folder);

        if (directory == null
                || !directory.exists()
                || !directory.isDirectory()) {
            return result;
        }

        try {
            DocumentFile[] files =
                    directory.listFiles();

            if (files == null) {
                return result;
            }

            Collections.addAll(
                    result,
                    files
            );

            result.sort(
                    new Comparator<DocumentFile>() {
                        @Override
                        public int compare(
                                DocumentFile first,
                                DocumentFile second
                        ) {
                            String a =
                                    first == null
                                            ? ""
                                            : first.getName();

                            String b =
                                    second == null
                                            ? ""
                                            : second.getName();

                            if (a == null) {
                                a = "";
                            }

                            if (b == null) {
                                b = "";
                            }

                            return a.compareToIgnoreCase(
                                    b
                            );
                        }
                    }
            );

        } catch (Exception ignored) {
        }

        return result;
    }

    /**
     * 获取目录名称。
     */
    public String getDisplayName(
            LocalFolder folder
    ) {
        if (folder == null) {
            return "";
        }

        String name =
                folder.getName();

        if (name == null
                || name.trim().isEmpty()) {
            return "本地视频";
        }

        return name;
    }

    /**
     * 是否有任何本地目录。
     */
    public boolean hasFolders() {
        return !getAll().isEmpty();
    }

    /**
     * 是否有启用目录。
     */
    public boolean hasEnabledFolders() {
        return !getEnabledFolders().isEmpty();
    }

    /**
     * 构造系统目录选择器 Intent。
     */
    public static Intent createFolderPickerIntent() {
        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT_TREE
                );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        return intent;
    }

    /**
     * 接收系统目录选择结果并保存。
     */
    public LocalFolder handleFolderPickerResult(
            int resultCode,
            Intent data
    ) {
        if (resultCode != android.app.Activity.RESULT_OK
                || data == null) {
            return null;
        }

        Uri uri =
                data.getData();

        return addFolder(uri);
    }

    /**
     * 持久化目录读取权限。
     */
    private boolean takePersistablePermission(
            Uri uri
    ) {
        try {
            int flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

            context.getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            flags
                    );

            return true;

        } catch (Exception e) {

            /*
             * 部分文件管理器只允许持久化读权限。
             */
            try {
                context.getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                return true;

            } catch (Exception ignored) {
                return false;
            }
        }
    }

    /**
     * 释放目录权限。
     */
    private void releasePersistablePermission(
            String treeUri
    ) {
        if (treeUri == null
                || treeUri.trim().isEmpty()) {
            return;
        }

        try {
            Uri uri =
                    Uri.parse(treeUri);

            context.getContentResolver()
                    .releasePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );

        } catch (Exception ignored) {

            try {
                Uri uri =
                        Uri.parse(treeUri);

                context.getContentResolver()
                        .releasePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignoredAgain) {
            }
        }
    }

    private void sortFolders(
            List<LocalFolder> folders
    ) {
        folders.sort(
                new Comparator<LocalFolder>() {
                    @Override
                    public int compare(
                            LocalFolder first,
                            LocalFolder second
                    ) {
                        if (first == null
                                && second == null) {
                            return 0;
                        }

                        if (first == null) {
                            return 1;
                        }

                        if (second == null) {
                            return -1;
                        }

                        return Long.compare(
                                first.getCreateTime(),
                                second.getCreateTime()
                        );
                    }
                }
        );
    }
}
