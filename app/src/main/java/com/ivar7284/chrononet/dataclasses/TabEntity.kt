// TabEntity.kt
package com.ivar7284.chrononet.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val title: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)