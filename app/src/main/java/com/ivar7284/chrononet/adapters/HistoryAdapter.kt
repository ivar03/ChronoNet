package com.ivar7284.chrononet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.R
import com.ivar7284.chrononet.dataclasses.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
        private val onItemSelected: (HistoryEntry, Boolean) -> Unit
    ) : ListAdapter<HistoryEntry, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    private val selectedItems = mutableSetOf<HistoryEntry>()

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryEntry>() {
            override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyEntry = getItem(position)
        val isSelected = selectedItems.contains(historyEntry)
        holder.bind(historyEntry, isSelected)

        holder.itemView.findViewById<CheckBox>(R.id.checkbox).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(historyEntry)
            } else {
                selectedItems.remove(historyEntry)
            }
            onItemSelected(historyEntry, isChecked)
        }
    }

    fun getSelectedItems(): List<HistoryEntry> {
        return selectedItems.toList()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val urlTextView: TextView = itemView.findViewById(R.id.url_text)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(history: HistoryEntry, isSelected: Boolean) {
            urlTextView.text = history.url
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            timestampTextView.text = dateFormat.format(Date(history.timestamp))
            checkBox.isChecked = isSelected
        }
    }
}
