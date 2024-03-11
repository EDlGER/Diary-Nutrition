package ediger.diarynutrition.billing

import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.*
import com.android.billingclient.api.*
import ediger.diarynutrition.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingClientLifecycle private constructor(
        private val app: Application,
        private val externalScope: CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    ProductDetailsResponseListener {

    private val _subscriptionPurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val subscriptionPurchases = _subscriptionPurchases.asStateFlow()

    private val _oneTimeProductPurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val oneTimeProductPurchases = _oneTimeProductPurchases.asStateFlow()

    private var cachedPurchasesList: List<Purchase>? = null

    private val _productWithProductDetails =
        MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productWithProductDetails = _productWithProductDetails.asStateFlow()

    private lateinit var billingClient: BillingClient

    private var retryAttempts = 3

    companion object {
        private const val TAG = "BillingLifecycle"

        val SUBS_LIST = listOf(PRODUCT_SUB_PREMIUM)
        val INAPP_LIST = listOf(PRODUCT_PREMIUM_UNLIMITED, PRODUCT_REMOVE_ADS)
        //val INAPP_LIST = listOf(PRODUCT_PREMIUM_UNLIMITED)

        private var INSTANCE: BillingClientLifecycle? = null

        fun getInstance(app: Application): BillingClientLifecycle =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: BillingClientLifecycle(app).also { INSTANCE = it }
                }
    }

    override fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "ON_CREATE")

        billingClient = BillingClient.newBuilder(app.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            Log.d(TAG, "BillingClient: Start connection...")
            retryAttempts = 3
            billingClient.startConnection(this)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "ON_RESUME")

        if (billingClient.isReady) {
            querySubscriptionPurchases()
            queryOneTimePurchases()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "ON_DESTROY")
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing connection")

            billingClient.endConnection()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            querySubscriptionProductDetails()
            queryOneTimeProductDetails()
            querySubscriptionPurchases()
            queryOneTimePurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        if (!billingClient.isReady) {
            if (retryAttempts > 0) {
                retryAttempts--
                Log.d(TAG, "Reconnecting attempts left: $retryAttempts")
                billingClient.startConnection(this)
            }
        }
    }

    private fun querySubscriptionProductDetails() {
        Log.d(TAG, "querySubscriptionProductDetails")
        val params = QueryProductDetailsParams.newBuilder()

        val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        for (subProduct in SUBS_LIST) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(subProduct)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }
        params.setProductList(productList)

        Log.i(TAG, "queryProductDetailsAsync for subscriptions")
        billingClient.queryProductDetailsAsync(params.build(), this)
    }

    private fun queryOneTimeProductDetails() {
        Log.d(TAG, "queryOneTimeProductDetails")
        val params = QueryProductDetailsParams.newBuilder()

        val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PREMIUM_UNLIMITED)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        params.setProductList(productList)

        Log.i(TAG, "queryProductDetailsAsync for oneTimePurchase")
        billingClient.queryProductDetailsAsync(params.build(), this)
    }

    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: List<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(TAG, "onProductDetailsResponse: $debugMessage")
                processProductDetails(productDetailsList)
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }

            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                // These response codes are not expected.
                Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    private fun processProductDetails(productDetailsList: List<ProductDetails>) {
        val expectedProductDetailsCount = SUBS_LIST.size + INAPP_LIST.size - 1
        var newMap = emptyMap<String, ProductDetails>()
        if (productDetailsList.isEmpty()) {
            Log.e(TAG,"processProductDetails: " +
                    "Expected ${expectedProductDetailsCount}, " +
                    "Found null ProductDetails. ")
        } else {
            newMap = productDetailsList.associateBy { it.productId }
        }
        _productWithProductDetails.value = newMap
    }

    private fun querySubscriptionPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "querySubscriptionPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }

        Log.d(TAG, "queryPurchases: SUBS")
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        billingClient.queryPurchasesAsync(params.build()) { _, list ->
            processPurchases(list, BillingClient.ProductType.SUBS)
        }
    }

    private fun queryOneTimePurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "querySubscriptionPurchases: BillingClient is not ready")
            billingClient.startConnection(this)
        }

        Log.d(TAG, "queryPurchases: INAPP")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        billingClient.queryPurchasesAsync(params.build()) { _, list ->
            processPurchases(list, BillingClient.ProductType.INAPP)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Log.d(TAG, "onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                } else {
                    processPurchases(purchases)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                if (!billingClient.isReady) {
                    billingClient.startConnection(this)
                }
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
                )
            }
        }
    }

    private fun processPurchases(
        purchasesList: List<Purchase>?,
        @BillingClient.ProductType productType: String? = null
    ) {
        Log.d(TAG, "processPurchases: ${purchasesList?.size} purchase(s)")
        purchasesList?.let { list ->
            if (isUnchangedPurchaseList(purchasesList)) {
                Log.d(TAG, "processPurchases: Purchase list has not changed")
                return
            }
            val subscriptionPurchaseList = list.filter { purchase ->
                purchase.products.any { product ->
                    product in SUBS_LIST
                }
            }
            val oneTimeProductPurchaseList = list.filter { purchase ->
                purchase.products.any { product ->
                    product in INAPP_LIST
                }
            }
            when (productType) {
                BillingClient.ProductType.SUBS ->
                    _subscriptionPurchases.value = subscriptionPurchaseList
                BillingClient.ProductType.INAPP ->
                    _oneTimeProductPurchases.value = oneTimeProductPurchaseList
                else -> {
                    _subscriptionPurchases.value = subscriptionPurchaseList
                    _oneTimeProductPurchases.value = oneTimeProductPurchaseList
                }
            }

            logAcknowledgementStatus(list)

            // Acknowledge purchase
            purchasesList.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    && !purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }
            }

            updatePremiumStatus()
        }
    }

    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        val isUnchanged = purchasesList == cachedPurchasesList
        if (!isUnchanged) {
            cachedPurchasesList = purchasesList
        }
        return isUnchanged
    }

    private fun updatePremiumStatus(isJustAcknowledged: Boolean = false) {
        val purchases = subscriptionPurchases.value + oneTimeProductPurchases.value
        val isEntitled = isJustAcknowledged || purchases.isNotEmpty()
                && purchases.any {
                    it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

        app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE).apply {
            val premiumPref = getBoolean(PREF_PREMIUM, false)

            if (isEntitled != premiumPref) {
                edit()
                    .putBoolean(PREF_PREMIUM, isEntitled)
                    .apply()
                Log.d(TAG, "${if (isEntitled) "Entitlement" else "Revoking"} Premium is done")
            }
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        Log.d(TAG, "acknowledgePurchase")
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            Log.d(TAG, "acknowledgePurchase: $responseCode $debugMessage")
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                updatePremiumStatus(isJustAcknowledged = true)
            }
        }
    }

    private fun logAcknowledgementStatus(purchasesList: List<Purchase>) {
        var acknowledgedCounter = 0
        var unacknowledgedCounter = 0
        for (purchase in purchasesList) {
            if (purchase.isAcknowledged) {
                acknowledgedCounter++
            } else {
                unacknowledgedCounter++
            }
        }
        Log.d(
            TAG,
            "logAcknowledgementStatus: acknowledged=$acknowledgedCounter " +
                    "unacknowledged=$unacknowledgedCounter"
        )
    }

}