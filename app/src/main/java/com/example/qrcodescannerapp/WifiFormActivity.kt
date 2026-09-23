package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WifiFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_form)

        val btnBack = findViewById<ImageView>(R.id.btnBackWifi)
        val etSsid = findViewById<EditText>(R.id.etWifiSsid)
        val etPassword = findViewById<EditText>(R.id.etWifiPassword)
        val rgSecurity = findViewById<RadioGroup>(R.id.rgWifiSecurity)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateWifi)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val ssid = etSsid.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Network name likhna zaroori hai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val securityType = when (rgSecurity.checkedRadioButtonId) {
                R.id.rbWep -> "WEP"
                R.id.rbNone -> "nopass"
                else -> "WPA"
            }

            // Standard Wi-Fi QR format
            val wifiData = "WIFI:T:$securityType;S:$ssid;P:$password;;"

            // Intent code update kar diya gaya hai
            val intent = Intent(this, QrResultActivity::class.java)
            intent.putExtra("QR_CONTENT", wifiData)
            intent.putExtra("QR_TYPE", "Wi-Fi")
            startActivity(intent)
        }
    }
}