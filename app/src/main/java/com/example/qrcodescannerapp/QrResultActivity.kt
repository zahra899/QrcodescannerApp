package com.example.qrcodescannerapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class QrResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_result)

        val btnBack = findViewById<ImageView>(R.id.btnBackResult)
        val ivQr = findViewById<ImageView>(R.id.ivQrResult)
        val tvLabel = findViewById<TextView>(R.id.tvResultLabel)
        val btnShare = findViewById<Button>(R.id.btnShareQr)
        val btnDone = findViewById<Button>(R.id.btnDoneQr)

        val qrContent = intent.getStringExtra("QR_CONTENT") ?: ""
        val qrType = intent.getStringExtra("QR_TYPE") ?: "QR Code"

        tvLabel.text = "Scan this $qrType code to open"

        val bitmap = QrCodeGenerator.generate(qrContent)
        if (bitmap != null) {
            ivQr.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "QR Code banane mein masla hua", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener { finish() }
        btnDone.setOnClickListener {
            // Seedha History screen par bhej dein taake user apna record dekh sake
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }

        btnShare.setOnClickListener {
            bitmap?.let { shareQrBitmap(it) }
        }

        // Optional: generated QR ko bhi History mein save kar dein (real-time record)
        if (qrContent.isNotEmpty()) {
            HistoryRepository.addScan(title = qrContent, type = qrType)
        }
    }

    private fun shareQrBitmap(bitmap: Bitmap) {
        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "qr_code.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri: Uri = FileProvider.getUriForFile(
                this, "$packageName.provider", file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        } catch (e: Exception) {
            Toast.makeText(this, "Share karne mein masla hua", Toast.LENGTH_SHORT).show()
        }
    }
}