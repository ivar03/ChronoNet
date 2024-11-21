package com.ivar7284.chrononet.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ivar7284.chrononet.dataclasses.TabEntity

@Dao
interface TabDao {

    @Insert
    suspend fun insertTab(tab: TabEntity)

    @Query("SELECT * FROM tabs")
    suspend fun getAllTabs(): List<TabEntity>

    @Query("DELETE FROM tabs WHERE id = :tabId")
    suspend fun deleteTabById(tabId: String)

    @Query("DELETE FROM tabs")
    suspend fun deleteAllTabs()
}
