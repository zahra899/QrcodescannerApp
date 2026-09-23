package com.shady.billing.utils

import androidx.annotation.StringRes
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.shady.billing.R
import com.shady.billing.model.BillingPeriod
import com.shady.billing.model.SubscriptionProduct

fun getBillingPeriod(period: String): BillingPeriod {
    val periodList = period.toList().map { it.toString() }
    val duration = runCatching {
        periodList.getOrEmpty(1).toInt()
    }.getOrElse { -1 }

    val periodUnit = when (periodList.getOrEmpty(2)) {
        "D" -> getCalenderPeriod(duration, R.string.day, R.string.days)
        "W" -> getCalenderPeriod(duration, R.string.week, R.string.weeks)
        "M" -> getCalenderPeriod(duration, R.string.month, R.string.months)
        "Y" -> getCalenderPeriod(duration, R.string.year, R.string.years)
        else -> 0
    }

    return BillingPeriod(
        durationUnit = periodUnit,
        durationPeriod = duration
    )
}

fun ProductDetails.asDomainModel(): SubscriptionProduct? {
    if (productType == ProductType.INAPP) {
        val offer = oneTimePurchaseOfferDetails?.formattedPrice ?: return null
        return SubscriptionProduct(
            durationUnit = 0,
            durationPeriod = 0,
            formattedPrice = offer,
            isFreeTrialAvailable = false,
            freeDurationPeriod = 0,
            freeDurationUnit = 0,
        )
    }

    val subsOffer = subscriptionOfferDetails?.getOrNull(0) ?: return null
    val prices = subsOffer.pricingPhases.pricingPhaseList

    val freePrice = prices.find { it.isFree() }
    val originalPrice = prices.find { !it.isFree() }

    val freePeriod =
        freePrice?.billingPeriod?.let { getBillingPeriod(it) } ?: BillingPeriod.empty
    val originalPeriod = originalPrice?.billingPeriod?.let { getBillingPeriod(it) } ?: BillingPeriod.empty

    return SubscriptionProduct(
        durationUnit = originalPeriod.durationUnit,
        durationPeriod = originalPeriod.durationPeriod,
        formattedPrice = originalPrice?.formattedPrice ?: "",
        freeDurationUnit = freePeriod.durationUnit,
        freeDurationPeriod = freePeriod.durationPeriod,
        isFreeTrialAvailable = freePrice != null
    )
}


fun ProductDetails.PricingPhase.isFree() = priceAmountMicros == 0L

/**
 * BillingFlowParams Builder for normal purchases.
 *
 * @param productDetails ProductDetails object returned by the library.
 * @param offerToken the least priced offer's offer id token returned by
 * [leastPricedOfferToken].
 *
 * @return [BillingFlowParams] builder.
 */
/*fun billingFlowParamsBuilder(productDetails: ProductDetails, offerToken: String):
        BillingFlowParams {
    return BillingFlowParams.newBuilder().setProductDetailsParamsList(
        listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
    ).build()
}*/
fun billingFlowParamsBuilder(productDetails: ProductDetails): BillingFlowParams {
    val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
        .setProductDetails(productDetails)

    if (productDetails.productType == BillingClient.ProductType.SUBS) {
        val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }
    }

    return BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
        .build()
}


/**
 * BillingFlowParams Builder for upgrades and downgrades.
 *
 * @param productDetails ProductDetails object returned by the library.
 * @param offerToken the least priced offer's offer id token returned by
 * [leastPricedOfferToken].
 * @param oldToken the purchase token of the subscription purchase being upgraded or downgraded.
 *
 * @return [BillingFlowParams] builder.
 */
fun upDowngradeBillingFlowParamsBuilder(
    productDetails: ProductDetails, offerToken: String, oldToken: String
): BillingFlowParams {
    return BillingFlowParams.newBuilder().setProductDetailsParamsList(
        listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
    ).setSubscriptionUpdateParams(
        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
            .setOldPurchaseToken(oldToken)
            .setSubscriptionReplacementMode(
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
            ).build()
    ).build()
}


/**
 * Retrieves all eligible base plans and offers using tags from ProductDetails.
 *
 * @param offerDetails offerDetails from a ProductDetails returned by the library.
 * @param tag string representing tags associated with offers and base plans.
 *
 * @return the eligible offers and base plans in a list.
 *
 */
fun retrieveEligibleOffers(
    offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>, tag: String
): List<ProductDetails.SubscriptionOfferDetails> {
    val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
    offerDetails.forEach { offerDetail ->
        if (offerDetail.offerTags.contains(tag)) {
            eligibleOffers.add(offerDetail)
        }
    }
    return eligibleOffers
}

/**
 * Calculates the lowest priced offer amongst all eligible offers.
 * In this implementation the lowest price of all offers' pricing phases is returned.
 * It's possible the logic can be implemented differently.
 * For example, the lowest average price in terms of month could be returned instead.
 *
 * @param offerDetails List of of eligible offers and base plans.
 *
 * @return the offer id token of the lowest priced offer.
 *
 */
fun leastPricedOfferToken(
    offerDetails: List<ProductDetails.SubscriptionOfferDetails>
): String {
    // TODO : Replace this with least average priced offer implementation

    var offerToken = String()
    var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
    var lowestPrice = Int.MAX_VALUE

    if (offerDetails.isNotEmpty()) {
        for (offer in offerDetails) {
            for (price in offer.pricingPhases.pricingPhaseList) {
                if (price.priceAmountMicros < lowestPrice) {
                    lowestPrice = price.priceAmountMicros.toInt()
                    leastPricedOffer = offer
                    offerToken = leastPricedOffer.offerToken
                }
            }
        }
    }
    return offerToken
}

private fun getCalenderPeriod(
    periodDuration: Int,
    @StringRes singular: Int,
    @StringRes plural: Int
): Int {
    return if (periodDuration > 1) plural else singular
}
