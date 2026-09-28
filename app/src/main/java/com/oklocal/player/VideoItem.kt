代码:
package com.oklocal.player

data class VideoItem(
    val id: Long,
    val name: String,
    val path: String,
    val uri: String,
    val duration: Long,
    val size: Long,
    val folder: String,
    val dateAdded: Long
)