package com.example.qrcodescannerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Open Premium Subscription Screen
        findViewById<Button?>(R.id.btnPremium)?.setOnClickListener {
            startActivity(Intent(this, GoPremiumActivity::class.java))
        }

        // Open Batch Scan Screen
        findViewById<Button?>(R.id.btnBatchScan)?.setOnClickListener {
            startActivity(Intent(this, BatchScanActivity::class.java))
        }

        // Open Product Detail Screen
        findViewById<Button?>(R.id.btnProductDetail)?.setOnClickListener {
            startActivity(Intent(this, ProductDetailActivity::class.java))
        }

        // Bottom Navigation Bar Clicks
        findViewById<LinearLayout>(R.id.navScan)?.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navHistory)?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navGenerate)?.setOnClickListener {
            startActivity(Intent(this, CreateQrActivity::class.java))
        }

        // Check camera permissions
        if (!allPermissionsGranted()) {
            val intent = Intent(this, CameraPermissionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}