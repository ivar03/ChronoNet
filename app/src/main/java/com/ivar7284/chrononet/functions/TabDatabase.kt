package com.ivar7284.chrononet.functions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ivar7284.chrononet.dataclasses.TabEntity
import com.ivar7284.chrononet.utils.TabDao

@Database(entities = [TabEntity::class], version = 1)
abstract class TabDatabase : RoomDatabase() {

    abstract fun tabDao(): TabDao

    companion object {
        private var INSTANCE: TabDatabase? = null

        fun getInstance(context: Context): TabDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    TabDatabase::class.java,
                    "tabs_database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}
