package com.threew.tv.local;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.threew.tv.model.LocalFolder;
import com.threew.tv.model.Video;
import com.threew.tv.model.Episode;
import com.threew.tv.utils.FileUtils;
import com.threew.tv.utils.TitleUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 本地视频扫描器。
 *
 * 特点：
 * - 直接读取用户选择的目录
 * - 不复制视频
 * - 不移动视频
 * - 不上传视频
 * - 支持多个目录
 * - 支持常见视频格式
 * - 新增文件重新扫描即可出现
 * - 删除文件重新扫描后自然消失
 * - 子目录也会扫描
 */
public class LocalVideoScanner {

    private final Context context;
    private final LocalFolderManager folderManager;

    public LocalVideoScanner(Context context) {
        this.context =
                context.getApplicationContext();

        this.folderManager =
                new LocalFolderManager(
                        this.context
                );
    }

    /**
     * 扫描全部启用的本地目录。
     */
    public List<Video> scanAll() {
        List<Video> result =
                new ArrayList<>();

        List<LocalFolder> folders =
                folderManager.getEnabledFolders();

        for (LocalFolder folder : folders) {
            if (folder == null) {
                continue;
            }

            result.addAll(
                    scanFolder(folder)
            );
        }

        sortVideos(result);

        return result;
    }

    /**
     * 扫描指定目录。
     */
    public List<Video> scanFolder(
            LocalFolder folder
    ) {
        List<Video> result =
                new ArrayList<>();

        if (folder == null
                || !folder.isEnabled()) {
            return result;
        }

        DocumentFile directory =
                folderManager.getDirectory(folder);

        if (directory == null
                || !directory.exists()
                || !directory.isDirectory()) {
            return result;
        }

        scanDirectory(
                directory,
                folder,
                result
        );

        sortVideos(result);

        folderManager.updateScanTime(
                folder
        );

        return result;
    }

    /**
     * 递归扫描目录。
     */
    private void scanDirectory(
            DocumentFile directory,
            LocalFolder folder,
            List<Video> result
    ) {
        if (directory == null
                || !directory.exists()
                || !directory.isDirectory()) {
            return;
        }

        DocumentFile[] files;

        try {
            files =
                    directory.listFiles();
        } catch (Exception e) {
            return;
        }

        if (files == null) {
            return;
        }

        for (DocumentFile file : files) {

            if (file == null
                    || !file.exists()) {
                continue;
            }

            try {
                if (file.isDirectory()) {

                    scanDirectory(
                            file,
                            folder,
                            result
                    );

                } else if (file.isFile()) {

                    Video video =
                            buildVideo(
                                    file,
                                    folder
                            );

                    if (video != null) {
                        result.add(video);
                    }
                }

            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 将 DocumentFile 转换成 Video。
     *
     * 本地视频没有网络 API 元数据，
     * 因此使用文件名生成基础信息。
     */
    private Video buildVideo(
            DocumentFile file,
            LocalFolder folder
    ) {
        String name =
                file.getName();

        if (name == null
                || name.trim().isEmpty()) {
            return null;
        }

        if (!FileUtils.isVideoFile(name)) {
            return null;
        }

        Uri uri =
                file.getUri();

        if (uri == null) {
            return null;
        }

        String title =
                FileUtils.removeExtension(
                        name
                );

        title =
                TitleUtils.cleanTitle(
                        title
                );

        title =
                TitleUtils.trimDecorations(
                        title
                );

        if (title.isEmpty()) {
            title = name;
        }

        Video video =
                new Video();

        /*
         * 使用 Uri 作为本地视频唯一标识。
         */
        video.setId(
                buildVideoId(
                        folder,
                        uri
                )
        );

        video.setName(title);

        video.setOriginalName(
                name
        );

        /*
         * 本地文件的播放地址直接使用 Uri。
         */
        video.setSourceId(
                "local"
        );

        video.setSourceName(
                folderManager.getDisplayName(
                        folder
                )
        );

        video.setCategory(
                "本地视频"
        );

        /*
         * 将本地文件作为单集视频。
         */
        List<Episode> episodes =
                new ArrayList<>();

        Episode episode =
                new Episode();

        episode.setId(
                buildEpisodeId(uri)
        );

        episode.setNumber(1);

        episode.setName(
                title
        );

        episode.setPlayUrl(
                uri.toString()
        );

        episode.setDurationMs(0);

        episode.setFileSizeBytes(
                Math.max(
                        file.length(),
                        0
                )
        );

        episode.setWatched(false);

        episode.setDownloaded(false);

        episodes.add(episode);

        video.setEpisodes(
                episodes
        );

        return video;
    }

    /**
     * 根据目录 + Uri 生成稳定 ID。
     */
    private String buildVideoId(
            LocalFolder folder,
            Uri uri
    ) {
        String folderId =
                folder == null
                        ? ""
                        : String.valueOf(
                                folder.getId()
                        );

        String value =
                "local|"
                        + folderId
                        + "|"
                        + uri.toString();

        return String.valueOf(
                value.hashCode()
        );
    }

    /**
     * 根据 Uri 生成稳定集 ID。
     */
    private String buildEpisodeId(
            Uri uri
    ) {
        String value =
                "local_episode|"
                        + uri.toString();

        return String.valueOf(
                value.hashCode()
        );
    }

    /**
     * 获取指定目录中的视频数量。
     */
    public int countFolder(
            LocalFolder folder
    ) {
        return scanFolder(folder).size();
    }

    /**
     * 获取全部本地视频数量。
     */
    public int countAll() {
        return scanAll().size();
    }

    /**
     * 查找指定 Uri 对应的视频。
     */
    public Video findByUri(
            String uri
    ) {
        if (uri == null
                || uri.trim().isEmpty()) {
            return null;
        }

        List<Video> videos =
                scanAll();

        for (Video video : videos) {

            if (video == null
                    || video.getEpisodes() == null) {
                continue;
            }

            for (Episode episode :
                    video.getEpisodes()) {

                if (episode == null) {
                    continue;
                }

                String playUrl =
                        episode.getPlayUrl();

                if (uri.equals(playUrl)) {
                    return video;
                }
            }
        }

        return null;
    }

    /**
     * 判断某个 Uri 是否属于本地视频。
     */
    public boolean isLocalVideoUri(
            String uri
    ) {
        if (uri == null
                || uri.trim().isEmpty()) {
            return false;
        }

        List<LocalFolder> folders =
                folderManager.getEnabledFolders();

        for (LocalFolder folder : folders) {

            Uri treeUri =
                    folderManager.getTreeUri(
                            folder
                    );

            if (treeUri == null) {
                continue;
            }

            String tree =
                    treeUri.toString();

            if (uri.startsWith(tree)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取本地视频的原始 Uri。
     */
    public Uri getVideoUri(
            Video video
    ) {
        if (video == null
                || video.getEpisodes() == null
                || video.getEpisodes().isEmpty()) {
            return null;
        }

        Episode episode =
                video.getEpisodes().get(0);

        if (episode == null) {
            return null;
        }

        String playUrl =
                episode.getPlayUrl();

        if (playUrl == null
                || playUrl.trim().isEmpty()) {
            return null;
        }

        try {
            return Uri.parse(playUrl);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 重新扫描。
     *
     * 本地视频不做缓存索引，因此这里直接扫描当前目录。
     */
    public List<Video> refresh() {
        return scanAll();
    }

    /**
     * 对视频排序。
     */
    private void sortVideos(
            List<Video> videos
    ) {
        if (videos == null) {
            return;
        }

        Collections.sort(
                videos,
                new Comparator<Video>() {
                    @Override
                    public int compare(
                            Video first,
                            Video second
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

                        String a =
                                first.getName();

                        String b =
                                second.getName();

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
    }
}
