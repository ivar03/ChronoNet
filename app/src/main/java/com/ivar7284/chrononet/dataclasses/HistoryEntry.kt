package com.ivar7284.chrononet.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browsing_history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)
