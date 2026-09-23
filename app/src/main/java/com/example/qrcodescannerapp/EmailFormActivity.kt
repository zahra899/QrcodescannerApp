package com.example.qrcodescannerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmailFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackEmail)
        val etTo = findViewById<EditText>(R.id.etEmailTo)
        val etSubject = findViewById<EditText>(R.id.etEmailSubject)
        val etBody = findViewById<EditText>(R.id.etEmailBody)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateEmail)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val to = etTo.text.toString().trim()
            if (to.isEmpty()) {
                Toast.makeText(this, "Email address likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val subject = Uri.encode(etSubject.text.toString().trim())
            val body = Uri.encode(etBody.text.toString().trim())
            val emailData = "mailto:$to?subject=$subject&body=$body"

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", emailData)
            intent.putExtra("QR_TYPE", "E-mail")
            startActivity(intent)
        }
    }
}