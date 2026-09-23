package com.shady.billing

import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.shady.billing.utils.asDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class BillingRepository  constructor(
   private val context: Context
) {
    val billingManager get() =  BillingManager.getInstance()
    val productState = billingManager.productState

    private val _buyEvent = MutableStateFlow(false)
    val buyEvent = _buyEvent.asStateFlow()

    val yearlyProductText = billingManager.yearlyProductDetails.map { it.toProductText() }
    val lifetimeProductText = billingManager.lifetimeProductDetails.map { it.getPrice() }
    val trialAndPriceYearly get() = billingManager.yearlyProductDetails.map { it.getTrialAndPrice() }


    init {
        Timber.e("aaa------")
        billingManager.setOnNewPurchaseListener {
            _buyEvent.value = true
        }
    }

    fun onRestorePurchases(callback: (purchases: List<Purchase>) -> Unit) {
        billingManager.setOnRestorePurchase(callback)
        billingManager.onRestorePurchases()
    }

    private fun ProductDetails?.toProductText(): String {
        val offer = this?.asDomainModel() ?: return ""

        val duration = offer.durationWithPrice(context)
        val freePeriod = offer.freeTrialPeriod(context)

        Timber.e("offer: $offer")

        return if (offer.isFreeTrialAvailable) {
            context.getString(R.string.billing_free_trial_with_original_price, freePeriod, duration)
        } else {
            context.getString(R.string.billing_original_price, duration)
        }
    }

    private fun ProductDetails?.getPriceWithDuration(): String {
        val offer = this?.asDomainModel() ?: return "${this?.oneTimePurchaseOfferDetails?.formattedPrice}"


        Timber.e("BillingManager-->1   ${this} ${this.oneTimePurchaseOfferDetails?.formattedPrice} ")

        val duration = offer.durationWithPrice(context)

        return /*context.getString(R.string.billing_original_price,*/( duration)
    }

    private fun ProductDetails?.getPrice(): String {
        val offer = this?.asDomainModel() ?: return "${this?.oneTimePurchaseOfferDetails?.formattedPrice}"


        Timber.e("BillingManager-->1   ${this} ${this.oneTimePurchaseOfferDetails?.formattedPrice} ")

        val duration = offer.formattedPrice//durationWithPrice(context)

        return ( duration)

    }

    private fun ProductDetails?.getTrialAndPrice(): Pair<String,String> {


        val offer = this?.asDomainModel() ?: return Pair("","")//"${this?.oneTimePurchaseOfferDetails?.formattedPrice}"

//        offer.
        val duration = offer.durationWithPrice(context)
        val freePeriod = offer.freeTrialPeriod(context)

        return Pair(freePeriod,duration)///*context.getString(R.string.billing_original_price,*/( duration)
    }
}