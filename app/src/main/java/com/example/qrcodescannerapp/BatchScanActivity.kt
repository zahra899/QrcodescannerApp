package com.example.qrcodescannerapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BatchScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var rvThumbnails: RecyclerView
    private lateinit var tvScannedCount: TextView
    private lateinit var btnDoneReview: Button

    private val scannedItems = mutableListOf<ScannedProduct>()
    private val scannedValues = mutableSetOf<String>() // duplicate scans avoid karne ke liye
    private lateinit var adapter: BatchThumbnailAdapter

    private var isPaused = false
    private lateinit var cameraProvider: ProcessCameraProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_scan)

        previewView = findViewById(R.id.previewView)
        rvThumbnails = findViewById(R.id.rvThumbnails)
        tvScannedCount = findViewById(R.id.tvScannedCount)
        btnDoneReview = findViewById(R.id.btnDoneReview)

        adapter = BatchThumbnailAdapter(scannedItems) { updateCounters() }
        rvThumbnails.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvThumbnails.adapter = adapter

        findViewById<ImageView>(R.id.btnClose).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnPauseResume).setOnClickListener {
            isPaused = !isPaused
            val label = findViewById<TextView>(R.id.tvPauseLabel)
            label.text = if (isPaused) "Resume" else "Pause"
        }

        btnDoneReview.setOnClickListener {
            // Yahan tum ek Review screen open kar sakti ho jo scannedItems ki list dikhaye
            android.widget.Toast.makeText(
                this, "Review ${scannedItems.size} items", android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val scanner = BarcodeScanning.getClient()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                if (isPaused) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(
                        mediaImage, imageProxy.imageInfo.rotationDegrees
                    )
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                handleBarcode(barcode)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleBarcode(barcode: Barcode) {
        val value = barcode.rawValue ?: return
        if (scannedValues.contains(value)) return // duplicate skip

        scannedValues.add(value)

        // NOTE: Real product SKU/Description barcode ke raw data me nahi hota
        // (barcode sirf number/text hota hai). Agar tumhe "SKU: AXP-2931,
        // DESC: Wireless Headphones" jaisa dikhana hai, ye info kisi
        // local database ya API se lookup karni hogi barcode value ke against.
        // Abhi ke liye main raw value ko hi description bana raha hoon:

        val product = ScannedProduct(
            sku = value.take(10),
            description = value,
            barcodeValue = value
        )

        scannedItems.add(product)
        runOnUiThread {
            adapter.notifyItemInserted(scannedItems.size - 1)
            updateCounters()
        }
    }

    private fun updateCounters() {
        tvScannedCount.text = "${scannedItems.size} Scanned"
        btnDoneReview.text = "Done — Review (${scannedItems.size})"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraProvider.isInitialized) cameraProvider.unbindAll()
    }
}