package com.example.qrcodescannerapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var cameraPreviewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    @Volatile
    private var isScanningPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qr_scanner)

        cameraPreviewView = findViewById(R.id.cameraPreviewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val tabScan = findViewById<TextView>(R.id.actionTabScan)
        val tabBatchScan = findViewById<TextView>(R.id.actionTabBatchScan)
        val shutterCentralButton = findViewById<View>(R.id.shutterCentralButton)
        val layoutGalleryClick = findViewById<LinearLayout>(R.id.layoutGalleryClick)
        val layoutCreateQrClick = findViewById<LinearLayout>(R.id.layoutCreateQrClick)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomAppTabsSystem)

        // Fix: bottom nav ko system navigation bar ke upar push karna
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBars.bottom)
            insets
        }

        bottomNavigation.selectedItemId = R.id.nav_scan

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scan -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_create -> {
                    startActivity(Intent(this, CreateQrActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Screen Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        shutterCentralButton.setOnClickListener {
            Toast.makeText(this, "Scanning Code Frame...", Toast.LENGTH_SHORT).show()
        }

        layoutGalleryClick.setOnClickListener {
            Toast.makeText(this, "Opening Photo Gallery...", Toast.LENGTH_SHORT).show()
        }

        layoutCreateQrClick.setOnClickListener {
            startActivity(Intent(this, CreateQrActivity::class.java))
        }

        startCamera()

        tabScan.setOnClickListener {
            tabScan.setBackgroundResource(R.drawable.toggle_active_tab)
            tabScan.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            tabBatchScan.setBackgroundResource(android.R.color.transparent)
            tabBatchScan.setTextColor(ContextCompat.getColor(this, R.color.text_muted_gray))
        }

        tabBatchScan.setOnClickListener {
            tabBatchScan.setBackgroundResource(R.drawable.toggle_active_tab)
            tabBatchScan.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            tabScan.setBackgroundResource(android.R.color.transparent)
            tabScan.setTextColor(ContextCompat.getColor(this, R.color.text_muted_gray))
        }
    }

    private fun navigateToTypeScreen(type: String) {
        val intent = when (type) {
            "URL" -> Intent(this, UrlFormActivity::class.java)
            "Wi-Fi" -> Intent(this, WifiFormActivity::class.java)
            "Contact" -> Intent(this, ContactFormActivity::class.java)
            "Phone" -> Intent(this, PhoneFormActivity::class.java)
            "E-mail" -> Intent(this, EmailFormActivity::class.java)
            "Text" -> Intent(this, TextFormActivity::class.java)
            "SMS" -> Intent(this, SmsFormActivity::class.java)
            "My Card" -> Intent(this, MyCardFormActivity::class.java)
            "Calender" -> Intent(this, CalendarFormActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreviewView.surfaceProvider)
            }

            val scannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
                .build()
            val barcodeScanner = BarcodeScanning.getClient(scannerOptions)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera Initialization Failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(scanner: BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage == null || isScanningPaused) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && !isScanningPaused) {
                    val firstBarcode = barcodes.firstOrNull()
                    firstBarcode?.rawValue?.let { value ->
                        isScanningPaused = true

                        runOnUiThread {
                            val detectedType = BarcodeTypeDetector.detectType(value)
                            HistoryRepository.addScan(title = value, type = detectedType)
                            Toast.makeText(this, "Scanned: $value", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, HistoryActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Failures ko yahan log kar sakte hain agar zaroorat ho
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}