// TabAdapter.kt
package com.ivar7284.chrononet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.R
import com.ivar7284.chrononet.dataclasses.TabEntity

class TabAdapter(
    private val onTabClick: (TabEntity) -> Unit,
    private val onTabClose: (TabEntity) -> Unit
) : ListAdapter<TabEntity, TabAdapter.TabViewHolder>(TabDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return TabViewHolder(view, onTabClick, onTabClose)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TabViewHolder(
        itemView: View,
        private val onTabClick: (TabEntity) -> Unit,
        private val onTabClose: (TabEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tab_title)
        private val urlTextView: TextView = itemView.findViewById(R.id.tab_url)
        private val closeButton: ImageView = itemView.findViewById(R.id.close_tab)

        fun bind(tab: TabEntity) {
            titleTextView.text = tab.title ?: "New Tab"
            urlTextView.text = tab.url

            itemView.setOnClickListener { onTabClick(tab) }
            closeButton.setOnClickListener { onTabClose(tab) }
        }
    }

    class TabDiffCallback : DiffUtil.ItemCallback<TabEntity>() {
        override fun areItemsTheSame(oldItem: TabEntity, newItem: TabEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TabEntity, newItem: TabEntity): Boolean {
            return oldItem == newItem
        }
    }
}