package com.shady.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.ai.chatmate.billing.utils.BillingResponse
import com.ai.chatmate.billing.utils.ProductState
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.shady.billing.config.TinyDB
import com.shady.billing.constant.LIFETIME_INAPP
import com.shady.billing.constant.LIST_OF_INAPP_PRODUCTS
import com.shady.billing.constant.LIST_OF_SUBSCRIPTION_PRODUCTS
import com.shady.billing.constant.MAX_RETRY_ATTEMPT
import com.shady.billing.constant.MONTHLY_PRODUCT
import com.shady.billing.constant.PREF_IS_SUBSCRIBED
import com.shady.billing.constant.PREF_PURCHASE_KEY
import com.shady.billing.constant.WEEKLY_PRODUCT
import com.shady.billing.constant.YEARLY_PRODUCT
import com.shady.billing.utils.billingFlowParamsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.pow

@SuppressLint("BinaryOperationInTimber")
/**
 * From
 * [play-billing-samples](https://github.com/android/play-billing-samples/blob/main/ClassyTaxiAppKotlin/app/src/main/java/com/example/billing/gpbl/BillingClientLifecycle.kt)
 * */
class BillingManager(
    applicationContext: Context,
    private val externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PurchasesUpdatedListener, BillingClientStateListener, ProductDetailsResponseListener,
    PurchasesResponseListener {

    private val config = TinyDB.getInstance(applicationContext)
    private var isQueryingToRestore = false
    private val _productState = MutableStateFlow<ProductState>(ProductState.Loading)
    private val _lifetimeProductDetails = MutableStateFlow<ProductDetails?>(null)
    private val _yearlyProductDetails = MutableStateFlow<ProductDetails?>(null)
    private val _weeklyProductDetails = MutableStateFlow<ProductDetails?>(null)
    private val _monthlyProductDetails = MutableStateFlow<ProductDetails?>(null)

    /**
     * Cached in-app product purchases details.
     */
    private var cachedPurchasesList: List<Purchase>? = null

    val productState = _productState.asStateFlow()

    val yearlyProductDetails = _yearlyProductDetails.asStateFlow()
    val lifetimeProductDetails = _lifetimeProductDetails.asStateFlow()

    val weeklyProductDetails = _weeklyProductDetails.asStateFlow()
    val monthlyProductDetails = _monthlyProductDetails.asStateFlow()
    val connectionLiveData = ConnectionLiveData(applicationContext)

    private var onNewPurchase: (() -> Unit)? = null
    private var onRestorePurchase: ((purchases: List<Purchase>) -> Unit)? = null

    private var hasProcessPurchase = false

    /**
     * Instantiate a new BillingClient instance.
     */
    val billingClient = BillingClient.newBuilder(applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        billingClient.startConnection(this)
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }

        startConnection()

        connectionLiveData.observeForever { isOnline ->
            Timber.tag(TAG).w("networkConnection change, isOnline: $isOnline")

            if (isOnline == true && _productState.value !is ProductState.Available) {
                Timber.tag(TAG)
                    .w("networkConnection change, requesting product details billingClient.isReady: ${billingClient.isReady}")

                if (billingClient.isReady) {
                    queryProductDetails()
                    queryProductPurchases()
                } else {
                    startConnection()
                }
            }
        }
    }

    fun startConnection() {
//        billingClient.endConnection()
        billingClient.startConnection(this)
    }


    fun launchFlow(
        activity: Activity,
        productDetails: ProductDetails?
    ) {
        val product = productDetails ?: run {

            println("launchFlow, Could not find product details. $productDetails")
            return
        }
        println("launchFlow, Could  find product details. $productDetails")

        val params = billingFlowParamsBuilder(product)
        launchFlow(activity, params)

    }

    fun launchFlow(activity: Activity, params: BillingFlowParams): Int {
        if (!billingClient.isReady) {
            Timber.tag(TAG).e("launchBillingFlow: BillingClient is not ready")
            println("launchBillingFlow: BillingClient is not ready")

        }

        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        Timber.tag(TAG).d("launchBillingFlow: BillingResponse $responseCode $debugMessage")
        println("launchBillingFlow: BillingResponse $responseCode $debugMessage")

        return responseCode
    }

    /**
     * onNewPurchase listener, called from [onPurchasesUpdated] once user made a new purchase.
     * */
    fun setOnNewPurchaseListener(callback: () -> Unit) {
        onNewPurchase = callback
    }

    /**
     * restorePurchase listener, called from [onQueryPurchasesResponse] when [onRestorePurchases] is invoke.
     * */
    fun setOnRestorePurchase(callback: (purchases: List<Purchase>) -> Unit) {
        onRestorePurchase = callback
    }

    fun onRestorePurchases() {
        if (isQueryingToRestore) return

        cachedPurchasesList = null
        isQueryingToRestore = true
        queryProductPurchases()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage

        Timber.tag(TAG).d("onPurchasesUpdated: $responseCode $debugMessage")
        println("onPurchasesUpdated:axim $responseCode $debugMessage")

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                for (purchase in purchases!!) {
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PENDING ->
                            println("onPurchasesUpdated:  Purchase is pending... so wait for long time")

                        Purchase.PurchaseState.PURCHASED -> {
                            if (purchases.isNullOrEmpty()) {
                                processPurchases(null)
                                println("onPurchasesUpdated: Purchase is pending... purchased null or empty")
                            } else {
                                println("onPurchasesUpdated:  Purchase is pending... purchased")
                                acknowledgePurchase(purchases)
                                processPurchases(purchases)
                                onNewPurchase?.invoke()
                            }
                        }
                    }
                }
                ////////////////////////////////////
                /*if (purchases.isNullOrEmpty()) {
                    processPurchases(null)
                } else {
                    acknowledgePurchase(purchases)
                    processPurchases(purchases)
                    onNewPurchase?.invoke()
                }*/
                ////////////////////////////////////

            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.tag(TAG).i("onPurchasesUpdated: User canceled the purchase")
                println("onPurchasesUpdated: User canceled the purchase")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.tag(TAG).i("onPurchasesUpdated: The user already owns this item")
                println("onPurchasesUpdated: The user already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Timber.tag(TAG).e(
                    "onPurchasesUpdated: Developer error means that Google Play does " +
                            "not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
                println(
                    "onPurchasesUpdated: Developer error means that Google Play does " +
                            "not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
            }
        }
    }


    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = BillingResponse(billingResult.responseCode)
        val debugMessage = billingResult.debugMessage

        Timber.tag(TAG).d("onBillingSetupFinished: ${responseCode.code}, message:$debugMessage")
        println("onBillingSetupFinished: ${responseCode.code}, message:$debugMessage")

        if (responseCode.isOk) {
            queryProductDetails()
            queryProductPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.tag(TAG).e("onBillingServiceDisconnected")
        println("onBillingServiceDisconnected")
    }

    /**
     * In order to make purchases, you need the [ProductDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in [onProductDetailsResponse].
     * */
    private fun queryProductDetails() {
        Timber.tag(TAG).d("queryProductDetails, isReady: ${billingClient.isReady}")
        println("queryProductDetails, isReady: ${billingClient.isReady}")

        queryProductDetailsWith(LIST_OF_SUBSCRIPTION_PRODUCTS, ProductType.SUBS)
        queryProductDetailsWith(LIST_OF_INAPP_PRODUCTS, ProductType.INAPP)
    }

    override fun onProductDetailsResponse(p0: BillingResult, p1: QueryProductDetailsResult) {
        val response = BillingResponse(p0.responseCode)
        val debugMessage = p0.debugMessage

        if (response.isOk) {
            processProductDetails(p1.productDetailsList)
        } else {
            Timber.tag(TAG).e("onProductDetailsResponse: ${response.code}, $debugMessage")
        }
    }

    /**
     * Query Google Play Billing for existing subscription purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    private fun queryProductPurchases() {
        Timber.tag(TAG).d("queryProductPurchases")
        println("queryProductPurchases")

        queryProductPurchasesWith(ProductType.SUBS)
        queryProductPurchasesWith(ProductType.INAPP)
    }

    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        purchasesList: MutableList<Purchase>
    ) {
        val response = BillingResponse(billingResult.responseCode)
        val debugMessage = billingResult.debugMessage

        if (response.isOk) {
            processPurchases(purchasesList)
            restorePurchases(purchasesList)
        } else {
            Timber.tag(TAG).e("onQueryPurchasesResponse: ${response.code}, $debugMessage")
            println("onQueryPurchasesResponse: ${response.code}, $debugMessage")
        }
    }

    private fun restorePurchases(purchasesList: MutableList<Purchase>) {
        if (isQueryingToRestore) {
            isQueryingToRestore = false
            onRestorePurchase?.invoke(purchasesList)
        }
    }

    /**
     * Saves the user purchases
     */
    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (hasProcessPurchase) return
        Timber.tag(TAG).d("processPurchases: ${purchasesList?.size} purchase(s)")
        println("processPurchases: ${purchasesList?.size} purchase(s)")

        purchasesList?.let { list ->
            if (isUnchangedPurchaseList(list)) {
                Timber.tag(TAG).d("processPurchases: Purchase list has not changed")
                println("processPurchases: Purchase list has not changed")
                return
            }

            val purchaseToken =
                list.firstOrNull { it.purchaseToken.isNotEmpty() }?.purchaseToken

            if (purchaseToken != null) {
                hasProcessPurchase = true
                config.putBoolean(PREF_IS_SUBSCRIBED, true)
                config.putString(PREF_PURCHASE_KEY, purchaseToken)
            } else {
                config.putBoolean(PREF_IS_SUBSCRIBED, false)
                config.putString(PREF_PURCHASE_KEY, "")
            }
        }
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */

    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        val isUnchanged = purchasesList == cachedPurchasesList
        if (!isUnchanged) {
            cachedPurchasesList = purchasesList
        }
        return isUnchanged
    }

    private fun queryProductPurchasesWith(productType: String) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build(), this
        )
    }

    /**
     * This method is used to process the product details list returned by the [BillingClient]
     * in the [onProductDetailsResponse] listener.
     *
     * @param productDetailsList The list of product details.
     */
    @SuppressLint("BinaryOperationInTimber")
    private fun processProductDetails(productDetailsList: MutableList<ProductDetails>) {
        if (productDetailsList.isEmpty()) {

            _productState.value = ProductState.Empty
            postProductDetails(emptyList())
        } else {
            _productState.value = ProductState.Available
            postProductDetails(productDetailsList)
        }
    }

    private fun postProductDetails(productDetailsList: List<ProductDetails>) {
        productDetailsList.forEach { productDetails ->
            when (productDetails.productType) {
                ProductType.SUBS -> {
                    when (productDetails.productId) {
                        YEARLY_PRODUCT -> {
                            _yearlyProductDetails.value = productDetails
                        }

                        WEEKLY_PRODUCT -> {
                            _weeklyProductDetails.value = productDetails
                        }

                        MONTHLY_PRODUCT -> {
                            _monthlyProductDetails.value = productDetails
                        }
                    }

                }

                ProductType.INAPP -> {
                    when (productDetails.productId) {
                        LIFETIME_INAPP -> {
                            _lifetimeProductDetails.value = productDetails
                        }
                    }

                }
            }

            Timber.tag(TAG).d("ProductDetails: $productDetails")
            println("ProductDetails: $productDetails")
            val offers = productDetails.subscriptionOfferDetails?.map {
                it.offerTags
            } ?: productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros
            Timber.tag(TAG).d("ProductOffer TAGS: $offers")
            println("ProductOffer TAGS: $offers")
        }
    }

    private fun queryProductDetailsWith(products: List<String>, productType: String) {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<Product>()

        for (product in products) {
            productList.add(
                Product.newBuilder()
                    .setProductId(product)
                    .setProductType(productType)
                    .build()
            )
        }

        params.setProductList(productList).let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }

    }

    private fun acknowledgePurchase(purchases: MutableList<Purchase>) {
        externalScope.launch {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase.purchaseToken)
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        for (trial in 1..MAX_RETRY_ATTEMPT) {
            var response = BillingResponse(500)
            var bResult: BillingResult? = null
            billingClient.acknowledgePurchase(params) { billingResult ->
                response = BillingResponse(billingResult.responseCode)
                bResult = billingResult
            }

            when {
                response.isOk -> {
                    Timber.tag(TAG).i("Acknowledge success - token: $purchaseToken")
                    println("Acknowledge success - token: $purchaseToken")
                    return true
                }

                response.canFailGracefully -> {
                    // Ignore the error
                    Timber.tag(TAG).e("Token $purchaseToken is already owned.")
                    println("Token $purchaseToken is already owned.")
                    return true
                }

                response.isRecoverableError -> {
                    // Retry to ack because these errors may be recoverable.
                    val duration = 500L * 2.0.pow(trial).toLong()
                    delay(duration)
                    if (trial < MAX_RETRY_ATTEMPT) {
                        Timber.tag(TAG).w(
                            "Retrying($trial) to acknowledge for token $purchaseToken - " +
                                    "code: ${bResult?.responseCode}, message: " +
                                    bResult?.debugMessage
                        )
                        println(
                            "Retrying($trial) to acknowledge for token $purchaseToken - " +
                                    "code: ${bResult?.responseCode}, message: " +
                                    bResult?.debugMessage
                        )
                    }
                }

                response.isNonrecoverableError || response.isTerribleFailure -> {
                    Timber.tag(TAG).e(
                        "Failed to acknowledge for token $purchaseToken - " +
                                "code: ${bResult?.responseCode}, message: " +
                                bResult?.debugMessage
                    )
                    println(
                        "Failed to acknowledge for token $purchaseToken - " +
                                "code: ${bResult?.responseCode}, message: " +
                                bResult?.debugMessage
                    )
                    break
                }
            }
        }

        return false
    }

    companion object {
        private const val TAG = "BillingManager"

        @Volatile
        private var INSTANCE: BillingManager? = null

        fun newInstance(context: Context): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(context).also { INSTANCE = it }
            }
        }

        fun getInstance(): BillingManager {
            return INSTANCE ?: throw IllegalStateException("BillingManager is not initialized")
        }
    }
}