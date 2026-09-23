package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PhoneFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackPhone)
        val etPhone = findViewById<EditText>(R.id.etPhoneInput)
        val btnGenerate = findViewById<Button>(R.id.btnGeneratePhone)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Phone number likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val phoneData = "tel:$phone"

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", phoneData)
            intent.putExtra("QR_TYPE", "Phone")
            startActivity(intent)
        }
    }
}