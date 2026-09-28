代码:
package com.oklocal.player

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import java.io.File

object VideoRepository {

    fun queryVideos(context: Context): List<VideoItem> {

        val result = mutableListOf<VideoItem>()

        val collection =
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )

        val sortOrder =
            "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media._ID
                )

            val nameIndex =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DISPLAY_NAME
                )

            val pathIndex =
                cursor.getColumnIndex(
                    MediaStore.Video.Media.DATA
                )

            val durationIndex =
                cursor.getColumnIndex(
                    MediaStore.Video.Media.DURATION
                )

            val sizeIndex =
                cursor.getColumnIndex(
                    MediaStore.Video.Media.SIZE
                )

            val dateIndex =
                cursor.getColumnIndex(
                    MediaStore.Video.Media.DATE_ADDED
                )

            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(idIndex)

                val name =
                    cursor.getString(nameIndex)
                        ?: "未知视频"

                val path =
                    if (pathIndex >= 0) {
                        cursor.getString(pathIndex) ?: ""
                    } else {
                        ""
                    }

                val duration =
                    if (durationIndex >= 0) {
                        cursor.getLong(durationIndex)
                    } else {
                        0L
                    }

                val size =
                    if (sizeIndex >= 0) {
                        cursor.getLong(sizeIndex)
                    } else {
                        0L
                    }

                val dateAdded =
                    if (dateIndex >= 0) {
                        cursor.getLong(dateIndex)
                    } else {
                        0L
                    }

                val uri =
                    ContentUris.withAppendedId(
                        collection,
                        id
                    ).toString()

                val folder =
                    if (path.isNotEmpty()) {
                        File(path).parentFile?.name
                            ?: "其他"
                    } else {
                        "其他"
                    }

                result.add(
                    VideoItem(
                        id = id,
                        name = name,
                        path = path,
                        uri = uri,
                        duration = duration,
                        size = size,
                        folder = folder,
                        dateAdded = dateAdded
                    )
                )
            }
        }

        return result
    }
}