package ediger.diarynutrition.billing

import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.*
import com.android.billingclient.api.*
import ediger.diarynutrition.*

class BillingClientLifecycle private constructor(
        private val app: Application
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    SkuDetailsResponseListener, PurchasesResponseListener {

    /**
     * Purchases are observable. This list will be updated when the Billing Library
     * detects new or existing purchases. All observers will be notified.
     */
    val purchases = MutableLiveData<List<Purchase>>()

    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()

    private lateinit var billingClient: BillingClient

    companion object {
        private const val TAG = "BillingLifecycle"

        val SUBS_SKUS = listOf(
            SKU_SUB_MONTHLY,
            SKU_SUB_SEASON,
            SKU_SUB_ANNUALLY
        )

        val INAPP_SKUS = listOf(SKU_PREMIUM_UNLIMITED, SKU_REMOVE_ADS)

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
            billingClient.startConnection(this)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "ON_RESUME")
        if (billingClient.isReady) {
            queryPurchases()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "ON_DESTROY")
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing connection")

            billingClient.endConnection()
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

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            querySkuDetails(BillingClient.SkuType.SUBS, SUBS_SKUS)
            querySkuDetails(BillingClient.SkuType.INAPP, INAPP_SKUS)

            queryPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        // It is better to try connecting again with exponential backoff
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onSkuDetailsResponse(
            billingResult: BillingResult,
            skuDetailsList: MutableList<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")

                skuDetailsList?.let { detailsList ->
                    val oldSkusWithDetails = skusWithSkuDetails.value
                    val postedList: HashMap<String, SkuDetails> = if (oldSkusWithDetails.isNullOrEmpty()) {
                        hashMapOf()
                    } else {
                        HashMap(oldSkusWithDetails)
                    }

                    skusWithSkuDetails.postValue(postedList.apply {
                        for (details in detailsList) {
                            put(details.sku, details)
                        }
                    })
                }
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

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    processPurchases(it, null)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
                queryPurchases()
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

    private fun querySkuDetails(
            @BillingClient.SkuType skuType: String,
            skuList: List<String>
    ) {
        Log.d(TAG, "querySkuDetails")
        val params = SkuDetailsParams.newBuilder()
                .setType(skuType)
                .setSkusList(skuList)
                .build()
        Log.i(TAG, "querySkuDetailsAsync")
        billingClient.querySkuDetailsAsync(params, this)
    }

    private fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
        }

        Log.d(TAG, "queryPurchases: SUBS")
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _, list ->
            processPurchases(list, BillingClient.SkuType.SUBS)
        }

        Log.d(TAG, "queryPurchases: INAPP")
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { _, list ->
            processPurchases(list, BillingClient.SkuType.INAPP)
        }
    }

    // TODO: Temporary
    override fun onQueryPurchasesResponse(billingResult: BillingResult, list: MutableList<Purchase>) {
        processPurchases(list, BillingClient.SkuType.SUBS)
    }

    private fun processPurchases(
            purchasesList: List<Purchase>,
            @BillingClient.SkuType skuType: String?) {
        Log.d(TAG, "processPurchases: ${purchasesList.size} purchase(s)")
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG, "processPurchases: Purchase list has not changed")
            return
        }
        val purchasesResult = mutableSetOf(*purchasesList.toTypedArray())

        //  Multiple threads processing this. It somewhat results in race condition
        // Possible solution is not save any local purchases at all.
        // But for that I need to find solution for querying both SUBS and INAPP


        // Save local purchase if necessary
        purchases.value?.let { localPurchases ->
            val skuTypedList = when (skuType) {
                BillingClient.SkuType.SUBS -> INAPP_SKUS
                BillingClient.SkuType.INAPP -> SUBS_SKUS
                else -> null
            }
            if (skuTypedList.isNullOrEmpty()) {
                purchasesResult.addAll(localPurchases)
            } else {
                localPurchases.forEach {
                    if (it.skus.first() in skuTypedList) {
                        purchasesResult.add(it)
                    }
                }
            }
        }

        purchases.postValue(purchasesResult.toList())

        // TODO: empty purchases list override local purchases even when it's not empty

        // TODO: Test or fix
//        if (skuType != null && purchases.value?.isNotEmpty() == true && purchasesList.isNotEmpty()) {
//            return
//        }
//
//        purchases.postValue(purchasesList)

        updatePremiumStatus(isEntitled = !purchases.value.isNullOrEmpty())

        // Acknowledge purchase
        purchases.value?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                && !purchase.isAcknowledged) {
                acknowledgePurchase(purchase.purchaseToken)
            }
        }
    }

    private fun isUnchangedPurchaseList(newPurchases: List<Purchase>?): Boolean {
        if (purchases.value.isNullOrEmpty() && newPurchases.isNullOrEmpty()) {
            return true
        }
        purchases.value?.let { localPurchases ->
            if (localPurchases.size == newPurchases?.size) {
                return newPurchases.all { newPurchase ->
                    // TODO: check if acknowledged and unacknowledged purchases are considered the same
                    newPurchase in localPurchases
                }
            }
        }
        return false
    }

    private fun updatePremiumStatus(isEntitled: Boolean) {
        app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_PREMIUM, isEntitled)
            .apply()
        Log.d(TAG, "${if (isEntitled) "Entitlement" else "Revoking"} Premium is done")
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
        }
    }

}