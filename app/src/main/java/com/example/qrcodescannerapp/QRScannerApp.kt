package com.example.qrcodescannerapp

import android.app.Application
import com.shady.billing.BillingManager

class QRScannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        HistoryRepository.init(this)
        BillingManager.newInstance(this)
    }
}