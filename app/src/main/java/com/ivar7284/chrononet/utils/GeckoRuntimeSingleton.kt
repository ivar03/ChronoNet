package com.ivar7284.chrononet.utils

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime

class GeckoRuntimeSingleton private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: GeckoRuntime? = null

        fun getInstance(context: Context): GeckoRuntime {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeckoRuntime.create(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }

        fun shutdown() {
            INSTANCE?.shutdown()
            INSTANCE = null
        }
    }
}