package com.ivar7284.chrononet.dataclasses

import android.graphics.Bitmap
import org.mozilla.geckoview.GeckoSession
import java.util.UUID

data class Tab(
    val session: GeckoSession,
    var url: String? = null,
    var title: String? = null,
    var thumbnail: Bitmap? = null,
    val id: String = UUID.randomUUID().toString()
)
