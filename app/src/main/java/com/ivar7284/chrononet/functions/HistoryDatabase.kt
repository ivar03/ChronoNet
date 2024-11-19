package com.ivar7284.chrononet.functions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ivar7284.chrononet.dataclasses.HistoryEntry
import com.ivar7284.chrononet.utils.HistoryDao

@Database(entities = [HistoryEntry::class], version = 1)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HistoryDatabase? = null

        fun getDatabase(context: Context): HistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryDatabase::class.java,
                    "browsing_history_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
