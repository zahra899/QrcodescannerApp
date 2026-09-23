package com.shady.billing.model

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class SubscriptionProduct(
    @StringRes val durationUnit: Int,
    val durationPeriod: Int,
    val formattedPrice: String,
    @StringRes val freeDurationUnit: Int = 0,
    val freeDurationPeriod: Int,
    val isFreeTrialAvailable: Boolean
) {
    fun durationWithPrice(context: Context): String {
        val localizedUnit =
            runCatching { context.getString(durationUnit) }.getOrElse { "" }
        val unit = if (localizedUnit.isNotEmpty()) " / $localizedUnit" else ""
        return "${formattedPrice}${unit}"
    }


    fun freeTrialPeriod(context: Context): String {
        return if (isFreeTrialAvailable) {
            val localizedUnit = runCatching { context.getString(freeDurationUnit).lowercase() }.getOrElse { "" }
            "$freeDurationPeriod $localizedUnit"
        } else ""
    }
}

