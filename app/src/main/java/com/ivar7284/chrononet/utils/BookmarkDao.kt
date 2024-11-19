package com.ivar7284.chrononet.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivar7284.chrononet.dataclasses.BookmarkEntry

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmarkEntry: BookmarkEntry)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getALLBookmarks(): List<BookmarkEntry>

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id:Int)
}