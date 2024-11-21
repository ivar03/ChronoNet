package com.ivar7284.chrononet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.R
import com.ivar7284.chrononet.dataclasses.Tab

class TabSwitcherAdapter(
    private var tabs: List<Tab>,
    private val onTabSelected: (Int) -> Unit,
    private val onTabClosed: (Int) -> Unit
) : RecyclerView.Adapter<TabSwitcherAdapter.TabViewHolder>() {

    private var currentTabIndex: Int = -1

    class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tab_title)
        val thumbnail: ImageView = view.findViewById(R.id.tabThumbnail)
        val closeButton: ImageView = view.findViewById(R.id.close_tab_button)

        fun bind(tab: Tab, isCurrentTab: Boolean) {
            // Set tab title
            title.text = tab.title ?: "New Tab"

            // Set thumbnail if available
            thumbnail.setImageBitmap(tab.thumbnail)

            // Update visual state based on current tab
            val backgroundColor = if (isCurrentTab) {
                ContextCompat.getColor(itemView.context, R.color.current_tab_background)
            } else {
                ContextCompat.getColor(itemView.context, R.color.tab_background)
            }

            val textColor = if (isCurrentTab) {
                ContextCompat.getColor(itemView.context, R.color.current_tab_text)
            } else {
                ContextCompat.getColor(itemView.context, R.color.tab_text)
            }

            itemView.setBackgroundColor(backgroundColor)
            title.setTextColor(textColor)

            // Optional: Add elevation or other visual indicators for current tab
            itemView.elevation = if (isCurrentTab) 8f else 2f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tab_item, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]

        // Bind the tab data and current state
        holder.bind(tab, position == currentTabIndex)

        // Set click listeners
        holder.itemView.setOnClickListener {
            onTabSelected(position)
            // Optionally update current tab immediately for responsive UI
            setCurrentTab(position)
        }

        holder.closeButton.setOnClickListener {
            // Handle tab closure and update UI
            onTabClosed(position)
        }
    }

    override fun getItemCount(): Int = tabs.size

    fun updateTabs(newTabs: List<Tab>) {
        tabs = newTabs
        notifyDataSetChanged()
    }

    fun setCurrentTab(index: Int) {
        val oldIndex = currentTabIndex
        currentTabIndex = index

        // Only notify items that changed
        oldIndex.takeIf { it >= 0 && it < itemCount }?.let {
            notifyItemChanged(it)
        }
        index.takeIf { it >= 0 && it < itemCount }?.let {
            notifyItemChanged(it)
        }
    }

    // Optional: Add methods for more granular updates
    fun notifyTabChanged(index: Int) {
        if (index in 0 until itemCount) {
            notifyItemChanged(index)
        }
    }

    fun notifyTabInserted(index: Int) {
        if (index in 0..itemCount) {
            notifyItemInserted(index)
        }
    }

    fun notifyTabRemoved(index: Int) {
        if (index in 0 until itemCount) {
            notifyItemRemoved(index)
        }
    }
}