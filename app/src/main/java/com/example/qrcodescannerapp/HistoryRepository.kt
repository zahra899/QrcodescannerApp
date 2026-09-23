package com.example.qrcodescannerapp

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object HistoryRepository {

    private const val PREF_NAME = "qr_history_prefs"
    private const val KEY_HISTORY = "history_items"

    private lateinit var prefs: SharedPreferences
    private val historyList = mutableListOf<HistoryItem>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadFromStorage()
        isInitialized = true
    }

    fun addScan(title: String, type: String) {
        if (!isInitialized) return
        historyList.add(0, HistoryItem(title = title, type = type))
        saveToStorage()
    }

    fun getAll(): List<HistoryItem> = historyList.toList()

    fun toggleFavorite(item: HistoryItem) {
        historyList.find { it.id == item.id }?.let { it.isFavorite = !it.isFavorite }
        saveToStorage()
    }

    fun deleteItems(items: List<HistoryItem>) {
        val ids = items.map { it.id }.toSet()
        historyList.removeAll { it.id in ids }
        saveToStorage()
    }

    fun filterByType(type: String): List<HistoryItem> {
        return if (type == "All") historyList
        else historyList.filter { it.type.equals(type, ignoreCase = true) }
    }

    fun filterFavorites(): List<HistoryItem> = historyList.filter { it.isFavorite }

    private fun saveToStorage() {
        val jsonArray = JSONArray()
        historyList.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("type", item.type)
            obj.put("timestamp", item.timestamp)
            obj.put("isFavorite", item.isFavorite)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    private fun loadFromStorage() {
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return
        val jsonArray = JSONArray(jsonString)
        historyList.clear()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            historyList.add(
                HistoryItem(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    type = obj.getString("type"),
                    timestamp = obj.getLong("timestamp"),
                    isFavorite = obj.getBoolean("isFavorite")
                )
            )
        }
    }
}