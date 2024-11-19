package com.ivar7284.chrononet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ivar7284.chrononet.adapters.HistoryAdapter
import com.ivar7284.chrononet.functions.HistoryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    private lateinit var noHistoryTv: TextView

    private lateinit var clearAllHistoryBtn: AppCompatButton
    private lateinit var clearSelectedHistoryBtn: AppCompatButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        noHistoryTv = findViewById(R.id.no_history_tv)
        recyclerView = findViewById(R.id.history_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter { _, isSelected ->
            clearSelectedHistoryBtn.visibility = if (adapter.getSelectedItems().isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        recyclerView.adapter = adapter

        // Loading history
        val db = HistoryDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val historyList = db.historyDao().getAllHistory()
            runOnUiThread {
                if (historyList.isEmpty()) {
                    noHistoryTv.visibility = View.VISIBLE
                } else {
                    noHistoryTv.visibility = View.GONE
                    adapter.submitList(historyList)
                }
            }
        }

        clearAllHistoryBtn = findViewById(R.id.clear_all_history_button)
        clearAllHistoryBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                db.historyDao().clearHistory()
                Toast.makeText(applicationContext, "History is deleted!!", Toast.LENGTH_SHORT).show()
                runOnUiThread {
                    adapter.submitList(emptyList())
                }
            }
        }

        clearSelectedHistoryBtn = findViewById(R.id.clear_selected_button)
        clearSelectedHistoryBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val selectedItems = adapter.getSelectedItems()
                selectedItems.forEach { db.historyDao().deleteHistoryEntry(it.id) }
                val updatedHistoryList = db.historyDao().getAllHistory()
                runOnUiThread {
                    adapter.submitList(updatedHistoryList)
                    clearSelectedHistoryBtn.visibility = View.GONE
                }
            }
        }

        //back button
        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener { finish() }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}