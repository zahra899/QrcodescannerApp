package com.example.qrcodescannerapp

data class HistoryItem(
    val id: Long = System.currentTimeMillis() + (0..9999).random(),
    val title: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false
)