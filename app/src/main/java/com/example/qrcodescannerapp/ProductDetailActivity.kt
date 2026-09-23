package com.example.qrcodescannerapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrcodescannerapp.R

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Back navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Save to favorites action
        findViewById<Button>(R.id.btnSaveFavorites).setOnClickListener {
            Toast.makeText(this, "Product saved to favorites!", Toast.LENGTH_SHORT).show()
        }

        // Search online action
        findViewById<Button>(R.id.btnSearchOnline).setOnClickListener {
            Toast.makeText(this, "Searching online stores...", Toast.LENGTH_SHORT).show()
        }
    }
}