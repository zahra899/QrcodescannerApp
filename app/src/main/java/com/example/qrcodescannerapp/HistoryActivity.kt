package com.example.qrcodescannerapp

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnStartScanning: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var layoutSelectionControls: LinearLayout
    private lateinit var adapter: HistoryAdapter

    private var currentFilter = "All"
    private var isSelectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        HistoryRepository.init(this)

        rvHistory = findViewById(R.id.rvHistory)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnStartScanning = findViewById(R.id.btnStartScanning)
        bottomNavigation = findViewById(R.id.bottomAppTabsSystem)
        layoutSelectionControls = findViewById(R.id.layoutSelectionControls)

        // Fix: bottom nav ko system navigation bar ke upar push karna
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBars.bottom)
            insets
        }

        rvHistory.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(
            items = emptyList(),
            onFavClick = { item ->
                HistoryRepository.toggleFavorite(item)
                refreshList()
            },
            onLongClick = { enterSelectionMode() }
        )
        rvHistory.adapter = adapter

        setupFilterChips()
        setupBottomNav()
        setupSelectionControls()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        btnStartScanning.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
            finish()
        }

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun setupSelectionControls() {
        val btnSelectAll = findViewById<ImageView>(R.id.btnSelectAll)
        val btnDeleteSelected = findViewById<ImageView>(R.id.btnDeleteSelected)

        btnSelectAll.setOnClickListener {
            adapter.selectAll()
        }

        btnDeleteSelected.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isEmpty()) {
                Toast.makeText(this, "Koi item select nahi kiya", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HistoryRepository.deleteItems(selected)
            exitSelectionMode()
            refreshList()
        }
    }

    private fun setupBottomNav() {
        bottomNavigation.selectedItemId = R.id.nav_history
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_history -> true
                R.id.nav_scan -> {
                    startActivity(Intent(this, QrScannerActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_create -> {
                    startActivity(Intent(this, CreateQrActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Screen Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFilterChips() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterUrls = findViewById<TextView>(R.id.filterUrls)
        val filterWifi = findViewById<TextView>(R.id.filterWifi)
        val filterContacts = findViewById<TextView>(R.id.filterContacts)
        val filterFavorites = findViewById<TextView>(R.id.filterFavorites)

        val chips = listOf(filterAll, filterUrls, filterWifi, filterContacts, filterFavorites)

        fun selectChip(selected: TextView, filterValue: String) {
            chips.forEach {
                it.setBackgroundResource(R.drawable.circle_dark_button)
                it.setTextColor("#8E8E93".toColorInt())
                it.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            selected.setTextColor("#10FA9E".toColorInt())
            selected.setTypeface(null, android.graphics.Typeface.BOLD)
            currentFilter = filterValue
            refreshList()
        }

        filterAll.setOnClickListener { selectChip(filterAll, "All") }
        filterUrls.setOnClickListener { selectChip(filterUrls, "URL") }
        filterWifi.setOnClickListener { selectChip(filterWifi, "WiFi") }
        filterContacts.setOnClickListener { selectChip(filterContacts, "Contact") }
        filterFavorites.setOnClickListener { selectChip(filterFavorites, "Favorites") }
    }

    private fun refreshList() {
        val filteredItems = if (currentFilter == "Favorites") {
            HistoryRepository.filterFavorites()
        } else {
            HistoryRepository.filterByType(currentFilter)
        }

        if (filteredItems.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }

        adapter.updateItems(buildGroupedList(filteredItems))
    }

    private fun buildGroupedList(items: List<HistoryItem>): List<Any> {
        val result = mutableListOf<Any>()
        val todayItems = items.filter { isToday(it.timestamp) }
        val yesterdayItems = items.filter { isYesterday(it.timestamp) }
        val olderItems = items.filter { !isToday(it.timestamp) && !isYesterday(it.timestamp) }

        if (todayItems.isNotEmpty()) {
            result.add("TODAY")
            result.addAll(todayItems)
        }
        if (yesterdayItems.isNotEmpty()) {
            result.add("YESTERDAY")
            result.addAll(yesterdayItems)
        }
        if (olderItems.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            olderItems.groupBy { sdf.format(Date(it.timestamp)) }.forEach { (dateLabel, list) ->
                result.add(dateLabel.uppercase())
                result.addAll(list)
            }
        }
        return result
    }

    private fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        adapter.setSelectionMode(true)
        layoutSelectionControls.visibility = View.VISIBLE
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        adapter.setSelectionMode(false)
        layoutSelectionControls.visibility = View.GONE
    }
}