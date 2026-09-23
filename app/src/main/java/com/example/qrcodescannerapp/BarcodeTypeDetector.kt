package com.example.qrcodescannerapp

object BarcodeTypeDetector {
    fun detectType(value: String): String {
        return when {
            value.startsWith("http://") || value.startsWith("https://") -> "URL"
            value.startsWith("WIFI:") -> "Wi-Fi"
            value.startsWith("BEGIN:VCARD") -> "Contact"
            value.startsWith("BEGIN:VEVENT") -> "Calender"
            value.startsWith("SMSTO:") -> "SMS"
            value.startsWith("tel:") || value.matches(Regex("^[+0-9\\-\\s]{7,15}$")) -> "Phone"
            value.contains("@") && value.contains(".") -> "E-mail"
            else -> "Text"
        }
    }
}