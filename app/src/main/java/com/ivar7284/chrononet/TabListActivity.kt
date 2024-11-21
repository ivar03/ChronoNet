package com.ivar7284.chrononet

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.adapters.TabSwitcherAdapter
import com.ivar7284.chrononet.functions.TabManager
import kotlinx.coroutines.launch

class TabListActivity : AppCompatActivity() {

    private lateinit var newTabButton: ImageButton
    private lateinit var tabCountText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabAdapter: TabSwitcherAdapter
    private val tabManager = TabManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_list)

        // Initialize views
        newTabButton = findViewById(R.id.new_tab_button)
        tabCountText = findViewById(R.id.tab_count_text)
        recyclerView = findViewById(R.id.tab_container)

        // Initialize adapter
        tabAdapter = TabSwitcherAdapter(
            tabs = tabManager.getTabs(),
            onTabSelected = { index ->
                tabManager.switchToTab(index)
                updateTabCount()
            },
            onTabClosed = { index ->
                lifecycleScope.launch {
                    tabManager.closeTab(index)
                    tabAdapter.updateTabs(tabManager.getTabs())
                    updateTabCount()
                }
            }
        )
        recyclerView.adapter = tabAdapter

        // Handle new tab creation
        newTabButton.setOnClickListener {
            lifecycleScope.launch {
                tabManager.createNewTab("about:blank", "New Tab")
                tabAdapter.updateTabs(tabManager.getTabs())
                updateTabCount()
            }
        }

        // Observe current tab changes
        tabManager.currentTabIndex.observe(this) { index ->
            // Optional: highlight current tab in the UI
            tabAdapter.setCurrentTab(index)
        }

        // Update tab count on creation
        updateTabCount()
    }

    private fun updateTabCount() {
        tabCountText.text = "Tabs: ${tabManager.getTabCount()}"
    }

    override fun onDestroy() {
        super.onDestroy()
        tabManager.cleanup()
    }
}