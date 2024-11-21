package com.ivar7284.chrononet.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivar7284.chrononet.dataclasses.HistoryEntry

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(historyEntry: HistoryEntry)

    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntry>

    @Query("DELETE FROM history_table")
    suspend fun clearHistory()

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun deleteHistoryEntry(id: Int)
}

