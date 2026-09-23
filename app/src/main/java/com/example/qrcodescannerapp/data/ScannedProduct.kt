package com.example.qrcodescannerapp

data class ScannedProduct(
    val sku: String,
    val description: String,
    val barcodeValue: String,
    val thumbnailBitmap: android.graphics.Bitmap? = null
)