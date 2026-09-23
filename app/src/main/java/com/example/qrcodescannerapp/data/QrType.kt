package com.example.qrcodescannerapp.model

data class QrType(
    val id: String,
    val title: String,
    val iconRes: Int
)

object QrTypeProvider {
    fun getMostUsedTypes(): List<QrType> = listOf(
        QrType("url", "URL", com.example.qrcodescannerapp.R.drawable.ic_url),
        QrType("wifi", "Wi-Fi", com.example.qrcodescannerapp.R.drawable.ic_wifi),
        QrType("contact", "Contact", com.example.qrcodescannerapp.R.drawable.ic_contact),
        QrType("phone", "Phone", com.example.qrcodescannerapp.R.drawable.ic_phone),
        QrType("email", "E-mail", com.example.qrcodescannerapp.R.drawable.ic_email),
        QrType("text", "Text", com.example.qrcodescannerapp.R.drawable.ic_text),
        QrType("sms", "SMS", com.example.qrcodescannerapp.R.drawable.ic_sms),
        QrType("mycard", "My Card", com.example.qrcodescannerapp.R.drawable.ic_mycard),
        QrType("calendar", "Calender", com.example.qrcodescannerapp.R.drawable.ic_calendar)
    )
}