// TabDao.kt
package com.ivar7284.chrononet.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ivar7284.chrononet.dataclasses.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Insert
    suspend fun insertTab(tab: TabEntity): Long

    @Query("SELECT * FROM tabs ORDER BY timestamp DESC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("UPDATE tabs SET isActive = 0")
    suspend fun clearActiveTabs()

    @Query("SELECT COUNT(*) FROM tabs")
    suspend fun getTabCount(): Int

    @Query("SELECT * FROM tabs LIMIT 1")
    suspend fun getSingleTab(): TabEntity?

    @Update
    suspend fun updateTab(tab: TabEntity)

    // If no tabs exist, insert the first tab
    suspend fun ensureTabExists() {
        val count = getTabCount()
        if (count == 0) {
            val initialTab = TabEntity(
                url = "about:blank",
                title = "New Tab",
                isActive = true
            )
            insertTab(initialTab)
        }
    }

    suspend fun addNewTab(url: String): TabEntity {
        clearActiveTabs()
        val newTab = TabEntity(
            url = url,
            title = null,
            isActive = true,
            timestamp = System.currentTimeMillis()
        )
        val id = insertTab(newTab)
        return newTab.copy(id = id.toInt())
    }

    @Query("DELETE FROM tabs WHERE id = :tabId")
    suspend fun deleteTab(tabId: Int)

    @Query("SELECT * FROM tabs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTab(): TabEntity?

}