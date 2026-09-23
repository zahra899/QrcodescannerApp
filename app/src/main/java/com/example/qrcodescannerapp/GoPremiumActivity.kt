package com.example.qrcodescannerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.shady.billing.BillingManager
import kotlinx.coroutines.launch

class GoPremiumActivity : AppCompatActivity() {

    private val features = listOf(
        "No Ads",
        "Unlimited Batch Scan",
        "Custom QR Colors & Logos",
        "Export History (CSV)",
        "Priority Support"
    )

    private var selectedPlan = "yearly"
    private var monthlyDetails: ProductDetails? = null
    private var yearlyDetails: ProductDetails? = null
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_premium)

        billingManager = BillingManager.getInstance()

        val container = findViewById<LinearLayout>(R.id.featureListContainer)
        features.forEach { feature -> addFeatureRow(container, feature) }

        val cardMonthly = findViewById<LinearLayout>(R.id.cardMonthly)
        val cardYearly = findViewById<LinearLayout>(R.id.cardYearly)

        cardMonthly.setOnClickListener {
            selectedPlan = "monthly"
            cardMonthly.setBackgroundResource(R.drawable.bg_plan_card_selected)
            cardYearly.setBackgroundResource(R.drawable.bg_plan_card_unselected)
        }

        cardYearly.setOnClickListener {
            selectedPlan = "yearly"
            cardYearly.setBackgroundResource(R.drawable.bg_plan_card_selected)
            cardMonthly.setBackgroundResource(R.drawable.bg_plan_card_unselected)
        }

        findViewById<Button>(R.id.btnStartTrial).setOnClickListener {
            launchPurchaseFlow(selectedPlan)
        }

        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            finish()
        }

        billingManager.setOnNewPurchaseListener {
            runOnUiThread {
                Toast.makeText(this, "Subscription successful! Welcome to Premium", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        lifecycleScope.launch {
            billingManager.monthlyProductDetails.collect { details ->
                monthlyDetails = details
                details?.let { updatePriceText(it, isMonthly = true) }
            }
        }

        lifecycleScope.launch {
            billingManager.yearlyProductDetails.collect { details ->
                yearlyDetails = details
                details?.let { updatePriceText(it, isMonthly = false) }
            }
        }
    }

    private fun updatePriceText(details: ProductDetails, isMonthly: Boolean) {
        val price = details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice ?: return

        if (isMonthly) {
            findViewById<TextView>(R.id.tvMonthlyPrice).text = price
        } else {
            findViewById<TextView>(R.id.tvYearlyPrice).text = price
        }
    }

    private fun addFeatureRow(container: LinearLayout, text: String) {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_premium_feature, container, false)
        row.findViewById<TextView>(R.id.tvFeatureText).text = text
        container.addView(row)
    }

    private fun launchPurchaseFlow(plan: String) {
        val productDetails = if (plan == "monthly") monthlyDetails else yearlyDetails

        if (productDetails == null) {
            Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show()
            return
        }

        billingManager.launchFlow(this, productDetails)
    }
}