package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContactFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackContact)
        val etName = findViewById<EditText>(R.id.etContactName)
        val etPhone = findViewById<EditText>(R.id.etContactPhone)
        val etEmail = findViewById<EditText>(R.id.etContactEmail)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateContact)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Naam aur phone number zaroori hain", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val vCard = buildString {
                append("BEGIN:VCARD\n")
                append("VERSION:3.0\n")
                append("FN:$name\n")
                append("TEL:$phone\n")
                if (etEmail.text.isNotEmpty()) append("EMAIL:${etEmail.text}\n")
                append("END:VCARD")
            }

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", vCard)
            intent.putExtra("QR_TYPE", "Contact")
            startActivity(intent)
        }
    }
}