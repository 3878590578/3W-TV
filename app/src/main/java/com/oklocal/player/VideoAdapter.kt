代码:
package com.oklocal.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class VideoAdapter(
    private val onClick: (VideoItem) -> Unit,
    private val onLongClick: (VideoItem) -> Boolean
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val items =
        mutableListOf<VideoItem>()

    fun submitList(
        list: List<VideoItem>
    ) {

        items.clear()
        items.addAll(list)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_video,
                    parent,
                    false
                )

        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder,
        position: Int
    ) {

        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        items.size

    inner class VideoViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val title =
            itemView.findViewById<TextView>(
                R.id.videoTitle
            )

        private val info =
            itemView.findViewById<TextView>(
                R.id.videoInfo
            )

        fun bind(item: VideoItem) {

            title.text = item.name

            info.text =
                "${formatDuration(item.duration)}  ·  " +
                "${formatSize(item.size)}"

            itemView.setOnClickListener {
                onClick(item)
            }

            itemView.setOnLongClickListener {
                onLongClick(item)
            }
        }
    }

    private fun formatDuration(
        ms: Long
    ): String {

        if (ms <= 0) {
            return "--:--"
        }

        val total =
            ms / 1000

        val hours =
            total / 3600

        val minutes =
            (total % 3600) / 60

        val seconds =
            total % 60

        return if (hours > 0) {

            String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        } else {

            String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds
            )
        }
    }

    private fun formatSize(
        size: Long
    ): String {

        if (size <= 0) {
            return "未知大小"
        }

        val mb =
            size / 1024.0 / 1024.0

        return if (mb < 1024) {

            String.format(
                Locale.US,
                "%.1f MB",
                mb
            )

        } else {

            String.format(
                Locale.US,
                "%.2f GB",
                mb / 1024.0
            )
        }
    }
}