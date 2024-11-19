package com.ivar7284.chrononet.functions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ivar7284.chrononet.dataclasses.BookmarkEntry
import com.ivar7284.chrononet.utils.BookmarkDao

@Database(entities = [BookmarkEntry::class], version = 1)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: BookmarkDatabase? = null

        fun getDatabase(context: Context): BookmarkDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkDatabase::class.java,
                    "bookmarks_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}