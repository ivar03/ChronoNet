package com.ivar7284.chrononet.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivar7284.chrononet.dataclasses.HistoryEntry

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyEntry: HistoryEntry)

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntry>

    @Query("DELETE FROM browsing_history")
    suspend fun clearHistory()

    @Query("DELETE FROM browsing_history WHERE id = :id")
    suspend fun deleteHistoryEntry(id: Int)
}
