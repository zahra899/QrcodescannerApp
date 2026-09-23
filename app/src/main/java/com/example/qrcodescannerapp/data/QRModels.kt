package com.example.qrcodescannerapp.data

// 1. History Item Model (History Row ke liye)
data class HistoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String,       // e.g., "Phone", "Email", "URL"
    val timestamp: String,  // "TODAY" ya "YESTERDAY"
    val isFavorite: Boolean = false
)

// 2. Create QR Grid Item Model (Grid Screen ke liye)
data class CreateOption(
    val title: String,
    val iconResId: Int,          // Drawable icon resource ID (e.g., R.drawable.ic_phone)
    val backgroundColorResId: Int // Colors.xml ka reference id (e.g., R.color.icon_phone)
)