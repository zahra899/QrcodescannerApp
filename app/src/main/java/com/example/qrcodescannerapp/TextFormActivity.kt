package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TextFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackText)
        val etText = findViewById<EditText>(R.id.etTextInput)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateText)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val text = etText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Kuch text likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", text)
            intent.putExtra("QR_TYPE", "Text")
            startActivity(intent)
        }
    }
}