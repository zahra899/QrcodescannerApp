package com.example.qrcodescannerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraPermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_permission)

        val btnAllowAccess = findViewById<Button>(R.id.btnAllowAccess)
        val tvNotNow = findViewById<TextView>(R.id.tvNotNow)

        // Allow Button Trigger Logic
        btnAllowAccess.setOnClickListener {
            if (allPermissionsGranted()) {
                navigateToScanner()
            } else {
                // System native permission popup display request
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        // Not Now Trigger Logic
        tvNotNow.setOnClickListener {
            Toast.makeText(this, "Camera access is required to scan codes.", Toast.LENGTH_SHORT).show()
            finish() // App close ho jayegi
        }
    }

    private fun navigateToScanner() {
        val intent = Intent(this, QrScannerActivity::class.java)
        startActivity(intent)
        finish() // Frame backstack clear logic rule
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                navigateToScanner()
            } else {
                Toast.makeText(this, "Permission denied. Cannot open scanner.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}