package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UrlFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackUrl)
        val etUrl = findViewById<EditText>(R.id.etUrlInput)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateUrl)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            var url = etUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "URL likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", url)
            intent.putExtra("QR_TYPE", "URL")
            startActivity(intent)
        }
    }
}