package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateQrActivity : AppCompatActivity() {

    private lateinit var adapter: QrTypeAdapter

    private val allTypes = listOf(
        QrTypeOption("URL", R.drawable.ic_url, "#D6EAF8", "#2E86C1"),
        QrTypeOption("Wi-Fi", R.drawable.ic_wifi, "#D5F5E3", "#27AE60"),
        QrTypeOption("Contact", R.drawable.ic_contact, "#F4ECF7", "#8E44AD"),
        QrTypeOption("Phone", R.drawable.ic_phone, "#FDEBD0", "#E67E22"),
        QrTypeOption("E-mail", R.drawable.ic_email, "#D6EAF8", "#2E86C1"),
        QrTypeOption("Text", R.drawable.ic_text, "#FCF3CF", "#F39C12"),
        QrTypeOption("SMS", R.drawable.ic_sms, "#FADBD8", "#16A085"),
        QrTypeOption("My Card", R.drawable.ic_mycard, "#EAF7EF", "#00A86B"),
        QrTypeOption("Calender", R.drawable.ic_calendar, "#D6EAF8", "#2E86C1")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_qr)

        val rv = findViewById<RecyclerView>(R.id.rvQrTypes)
        val etSearch = findViewById<EditText>(R.id.etSearchType)

        rv.layoutManager = GridLayoutManager(this, 3)
        adapter = QrTypeAdapter(allTypes) { selected ->
            navigateToTypeScreen(selected.label)
        }
        rv.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allTypes
                } else {
                    allTypes.filter { it.label.contains(query, ignoreCase = true) }
                }
                adapter.updateItems(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun navigateToTypeScreen(type: String) {
        val intent = when (type) {
            "URL" -> Intent(this, UrlFormActivity::class.java)
            "Wi-Fi" -> Intent(this, WifiFormActivity::class.java)
            "Contact" -> Intent(this, ContactFormActivity::class.java)
            "Phone" -> Intent(this, PhoneFormActivity::class.java)
            "E-mail" -> Intent(this, EmailFormActivity::class.java)
            "Text" -> Intent(this, TextFormActivity::class.java)
            "SMS" -> Intent(this, SmsFormActivity::class.java)
            "My Card" -> Intent(this, MyCardFormActivity::class.java)
            "Calender" -> Intent(this, CalendarFormActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }
}