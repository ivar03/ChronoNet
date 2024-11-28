package com.ivar7284.chrononet.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.HomeActivity
import com.ivar7284.chrononet.R
import com.ivar7284.chrononet.adapters.TabAdapter
import com.ivar7284.chrononet.dataclasses.TabEntity
import com.ivar7284.chrononet.functions.TabDatabase
import kotlinx.coroutines.launch

class TabSelectorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabAdapter: TabAdapter
    private lateinit var tabDatabase: TabDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_selector, container, false)

        recyclerView = view.findViewById(R.id.tabs_recycler_view)
        tabDatabase = TabDatabase.getDatabase(requireContext())

        setupRecyclerView()
        observeTabs()

        return view
    }

    private fun setupRecyclerView() {
        tabAdapter = TabAdapter(
            onTabClick = { tab -> openTab(tab) },
            onTabClose = { tab -> closeTab(tab) }
        )
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = tabAdapter
    }

    private fun observeTabs() {
        lifecycleScope.launch {
            tabDatabase.tabDao().getAllTabs().collect { tabs ->
                tabAdapter.submitList(tabs)
            }
        }
    }

    private fun openTab(tab: TabEntity) {
        // Get reference to HomeActivity to access currentActiveFragment
        val homeActivity = activity as? HomeActivity

        // Close the current active session
        homeActivity?.currentActiveFragment?.closeSession()

        val currentFragment = parentFragmentManager.findFragmentById(R.id.frag_home)
        if (currentFragment is TabFragment) {
            currentFragment.closeSession()
        }

        // Clear active tabs and set current tab as active
        lifecycleScope.launch {
            tabDatabase.tabDao().clearActiveTabs()

            val updatedTab = tab.copy(isActive = true)
            tabDatabase.tabDao().updateTab(updatedTab)

            // Open the tab in TabFragment
            val tabFragment = TabFragment().apply {
                arguments = Bundle().apply {
                    putString("URL", tab.url)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frag_home, tabFragment)
                .commit()
        }
    }

    private fun closeTab(tab: TabEntity) {
        lifecycleScope.launch {
            tabDatabase.tabDao().deleteTab(tab.id)
        }
    }
}