package com.shady.billing.model

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class BillingPeriod(
    @StringRes val durationUnit: Int,
    val durationPeriod: Int,
) {
    companion object {
        val empty = BillingPeriod(0,0)
    }
}