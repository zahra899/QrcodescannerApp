package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SmsFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackSms)
        val etPhone = findViewById<EditText>(R.id.etSmsPhone)
        val etMessage = findViewById<EditText>(R.id.etSmsMessage)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateSms)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Phone number likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val message = etMessage.text.toString().trim()
            val smsData = "SMSTO:$phone:$message"

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", smsData)
            intent.putExtra("QR_TYPE", "SMS")
            startActivity(intent)
        }
    }
}