package com.shady.billing

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class BillingViewModel(var application1: Application) : AndroidViewModel(application1) {
    private val billingRepository: BillingRepository by lazy { BillingRepository(application1) }
    val productState = billingRepository.productState
    val yearlyProductString = billingRepository.yearlyProductText

    val lifetimeBtnPriceString = billingRepository.lifetimeProductText

    val trialAndPriceYearly get() = billingRepository.trialAndPriceYearly
    val buyEvent = billingRepository.buyEvent

    fun getBillingManager() = billingRepository.billingManager
}

