package com.shady.billing.initializer

import android.content.Context
import androidx.startup.Initializer
import com.shady.billing.BillingManager

class BillingInitializer : Initializer<BillingManager> {
    override fun create(context: Context): BillingManager {
        return BillingManager.newInstance(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}