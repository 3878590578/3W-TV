package com.threew.tv.utils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 3W影视标题处理工具。
 *
 * 用于：
 * - 清理视频标题
 * - 提取集数
 * - 判断标题是否像剧集
 * - 生成搜索关键词
 * - 生成同名合并 Key
 */
public final class TitleUtils {

    private static final Pattern EPISODE_PATTERN =
            Pattern.compile(
                    "(?i)(?:第\\s*)?(\\d{1,4})\\s*(?:集|话|話)"
            );

    private static final Pattern SEASON_PATTERN =
            Pattern.compile(
                    "(?i)(?:第\\s*)?(\\d{1,2})\\s*(?:季|season|s)"
            );

    private TitleUtils() {
    }

    /**
     * 清理标题。
     *
     * 保留正常中文、英文、数字和常见符号，
     * 同时去除首尾多余空格。
     */
    public static String cleanTitle(
            String title
    ) {
        if (title == null) {
            return "";
        }

        String result =
                title.trim();

        result = result.replace(
                '\u3000',
                ' '
        );

        result = result.replaceAll(
                "\\s+",
                " "
        );

        return result.trim();
    }

    /**
     * 去掉标题中的集数信息。
     *
     * 例如：
     * 庆余年 第01集 -> 庆余年
     * 庆余年 12集 -> 庆余年
     */
    public static String removeEpisode(
            String title
    ) {
        if (title == null) {
            return "";
        }

        String result =
                title;

        result = result.replaceAll(
                "(?i)第\\s*\\d{1,4}\\s*[集话話]",
                ""
        );

        result = result.replaceAll(
                "(?i)\\s+S\\d{1,2}E\\d{1,4}",
                ""
        );

        result = result.replaceAll(
                "(?i)\\s+\\d{1,4}集",
                ""
        );

        result = result.replaceAll(
                "(?i)\\s+EP?\\.?\\s*\\d{1,4}",
                ""
        );

        return cleanTitle(result);
    }

    /**
     * 提取集数。
     *
     * 返回：
     * - 找到：正整数
     * - 未找到：-1
     */
    public static int extractEpisode(
            String title
    ) {
        if (title == null
                || title.trim().isEmpty()) {
            return -1;
        }

        java.util.regex.Matcher matcher =
                EPISODE_PATTERN.matcher(title);

        if (matcher.find()) {
            try {
                return Integer.parseInt(
                        matcher.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        matcher =
                Pattern.compile(
                        "(?i)S\\d{1,2}E(\\d{1,4})"
                ).matcher(title);

        if (matcher.find()) {
            try {
                return Integer.parseInt(
                        matcher.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        matcher =
                Pattern.compile(
                        "(?i)\\bEP?\\.?\\s*(\\d{1,4})\\b"
                ).matcher(title);

        if (matcher.find()) {
            try {
                return Integer.parseInt(
                        matcher.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    /**
     * 提取季数。
     *
     * 返回：
     * - 找到：季数
     * - 未找到：-1
     */
    public static int extractSeason(
            String title
    ) {
        if (title == null
                || title.trim().isEmpty()) {
            return -1;
        }

        java.util.regex.Matcher matcher =
                SEASON_PATTERN.matcher(title);

        if (matcher.find()) {
            try {
                return Integer.parseInt(
                        matcher.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        matcher =
                Pattern.compile(
                        "(?i)S(\\d{1,2})E\\d{1,4}"
                ).matcher(title);

        if (matcher.find()) {
            try {
                return Integer.parseInt(
                        matcher.group(1)
                );
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    /**
     * 判断标题是否包含集数。
     */
    public static boolean hasEpisode(
            String title
    ) {
        return extractEpisode(title) >= 0;
    }

    /**
     * 判断标题是否包含季数。
     */
    public static boolean hasSeason(
            String title
    ) {
        return extractSeason(title) >= 0;
    }

    /**
     * 判断是否可能为剧集标题。
     */
    public static boolean looksLikeEpisode(
            String title
    ) {
        if (title == null
                || title.trim().isEmpty()) {
            return false;
        }

        if (hasEpisode(title)) {
            return true;
        }

        return Pattern.compile(
                "(?i)S\\d{1,2}E\\d{1,4}"
        ).matcher(title).find();
    }

    /**
     * 生成搜索关键词。
     */
    public static String buildSearchKeyword(
            String title
    ) {
        String result =
                removeEpisode(title);

        result = result.replaceAll(
                "(?i)S\\d{1,2}E\\d{1,4}",
                ""
        );

        result = result.replaceAll(
                "[\\[\\]【】()（）{}]",
                " "
        );

        result = result.replaceAll(
                "\\s+",
                " "
        );

        return result.trim();
    }

    /**
     * 生成同名视频合并 Key。
     *
     * 不区分大小写，
     * 去掉空格及常见分隔符。
     */
    public static String buildMergeKey(
            String title
    ) {
        String result =
                buildSearchKeyword(title);

        result =
                result.toLowerCase(
                        Locale.ROOT
                );

        result = result.replaceAll(
                "[\\s\\-_·•:：,，.。!！?？'\"“”‘’]",
                ""
        );

        return result;
    }

    /**
     * 判断两个标题是否属于同名作品。
     */
    public static boolean isSameTitle(
            String first,
            String second
    ) {
        String a =
                buildMergeKey(first);

        String b =
                buildMergeKey(second);

        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

        return a.equals(b);
    }

    /**
     * 将标题首尾的常见装饰符去掉。
     */
    public static String trimDecorations(
            String title
    ) {
        if (title == null) {
            return "";
        }

        String result =
                title.trim();

        result = result.replaceAll(
                "^[\\[【（(《「『]+",
                ""
        );

        result = result.replaceAll(
                "[\\]】）)》」』]+$",
                ""
        );

        return result.trim();
    }

    /**
     * 生成显示用集数。
     */
    public static String formatEpisode(
            int episode
    ) {
        if (episode <= 0) {
            return "";
        }

        return String.format(
                Locale.getDefault(),
                "第%02d集",
                episode
        );
    }
}
