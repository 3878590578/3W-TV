代码:
package com.oklocal.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FolderItem(
    val name: String,
    val count: Int
)

class FolderAdapter(
    private val onClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private val items =
        mutableListOf<FolderItem>()

    fun submitList(
        list: List<FolderItem>
    ) {

        items.clear()
        items.addAll(list)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FolderViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_folder,
                    parent,
                    false
                )

        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FolderViewHolder,
        position: Int
    ) {

        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        items.size

    inner class FolderViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val name =
            itemView.findViewById<TextView>(
                R.id.folderName
            )

        private val count =
            itemView.findViewById<TextView>(
                R.id.folderCount
            )

        fun bind(
            item: FolderItem
        ) {

            name.text = item.name

            count.text =
                "${item.count} 个视频"

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}