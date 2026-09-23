package com.shady.billing.constant

const val MAX_RETRY_ATTEMPT = 3

const val WEEKLY_PRODUCT = "weekly"
const val MONTHLY_PRODUCT = "monthly"
const val YEARLY_PRODUCT = "yearly"
const val LIFETIME_INAPP = "remove_ads"



const val LIFETIME_PRODUCT = "remove_ads"


const val PREF_IS_SUBSCRIBED = "is_subscribe"
const val PREF_PURCHASE_KEY = "purchase_key"

val LIST_OF_SUBSCRIPTION_PRODUCTS = listOf(
    WEEKLY_PRODUCT,
    MONTHLY_PRODUCT,
    YEARLY_PRODUCT,
)
val LIST_OF_INAPP_PRODUCTS = listOf(
    LIFETIME_INAPP,
)

