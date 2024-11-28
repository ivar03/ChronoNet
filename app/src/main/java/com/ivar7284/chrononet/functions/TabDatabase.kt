package com.ivar7284.chrononet.functions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ivar7284.chrononet.dataclasses.TabEntity
import com.ivar7284.chrononet.utils.TabDao

@Database(entities = [TabEntity::class], version = 2, exportSchema = false)
abstract class TabDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: TabDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes in this version.
                // Add SQL for column changes or table alterations here if needed.
            }
        }

        fun getDatabase(context: Context): TabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TabDatabase::class.java,
                    "tabs_database"
                )
                    //.addMigrations(MIGRATION_1_2) // Use migration
                    //.fallbackToDestructiveMigration() // Uncomment for destructive migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
