package com.ivar7284.chrononet.functions

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ivar7284.chrononet.dataclasses.Tab
import com.ivar7284.chrononet.dataclasses.TabEntity
import com.ivar7284.chrononet.utils.TabDao
import kotlinx.coroutines.*
import org.mozilla.geckoview.GeckoSession
import kotlinx.coroutines.withContext

class TabManager private constructor(context: Context) {
    private val tabDao: TabDao = TabDatabase.getInstance(context).tabDao()
    private val tabs = mutableListOf<Tab>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentTabIndex = MutableLiveData<Int>(-1)
    val currentTabIndex: LiveData<Int> = _currentTabIndex

    init {
        scope.launch {
            loadTabsFromDatabase()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TabManager? = null

        fun getInstance(context: Context): TabManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TabManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun switchToTab(index: Int) {
        if (index in tabs.indices) {
            _currentTabIndex.value = index
        }
    }

    private suspend fun loadTabsFromDatabase() {
        try {
            withContext(Dispatchers.IO) {
                val savedTabs = tabDao.getAllTabs()

                withContext(Dispatchers.Main) {
                    savedTabs.forEach { tabEntity ->
                        // Create GeckoSession on main thread
                        val geckoSession = GeckoSession()
                        tabs.add(Tab(geckoSession, tabEntity.url, tabEntity.title, null, tabEntity.id))
                    }

                    if (tabs.isEmpty()) {
                        createNewTab("about:blank", "New Tab")
                    } else {
                        _currentTabIndex.value = tabs.size - 1
                    }
                }
            }
        } catch (e: Exception) {
            // Handle exceptions appropriately
            e.printStackTrace()
        }
    }

    suspend fun createNewTab(url: String, title: String): Tab = withContext(Dispatchers.Main) {
        val geckoSession = GeckoSession()
        val newTab = Tab(geckoSession, url, title)
        tabs.add(newTab)
        _currentTabIndex.value = tabs.size - 1

        // Save to database in background
        withContext(Dispatchers.IO) {
            val tabEntity = TabEntity(newTab.id, newTab.url ?: "about:blank", newTab.title ?: "New Tab")
            tabDao.insertTab(tabEntity)
        }

        newTab
    }

    private suspend fun saveTabToDatabase(tab: Tab) = withContext(Dispatchers.IO) {
        val tabEntity = TabEntity(tab.id, tab.url ?: "about:blank", tab.title ?: "New Tab")
        tabDao.insertTab(tabEntity)
    }

    suspend fun closeTab(index: Int) {
        if (index in tabs.indices) {
            val tab = tabs[index]
            tabs.removeAt(index)

            withContext(Dispatchers.IO) {
                tabDao.deleteTabById(tab.id)
            }

            _currentTabIndex.value = (index - 1).coerceAtLeast(0)
        }
    }

    fun getCurrentTab(): Tab? {
        return _currentTabIndex.value?.let { index ->
            if (index >= 0 && index < tabs.size) tabs[index] else null
        }
    }

    fun getTabs(): List<Tab> = tabs.toList()

    suspend fun restoreTabs(restoredTabs: List<Tab>) = withContext(Dispatchers.Main) {
        tabs.clear()
        tabs.addAll(restoredTabs)
        _currentTabIndex.value = tabs.lastIndex
    }

    fun getTabCount(): Int = tabs.size

    fun cleanup() {
        scope.cancel()
    }
}