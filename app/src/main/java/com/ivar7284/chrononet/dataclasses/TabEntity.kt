package com.ivar7284.chrononet.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val thumbnail: ByteArray? = null
)
