package ediger.diarynutrition.billing

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.Event
import ediger.diarynutrition.PLAN_PREMIUM_ANNUALLY
import ediger.diarynutrition.PLAN_PREMIUM_MONTHLY
import ediger.diarynutrition.PLAN_PREMIUM_SEASONALLY
import ediger.diarynutrition.PREF_FILE_PREMIUM
import ediger.diarynutrition.PREF_PREMIUM
import ediger.diarynutrition.PREF_PREMIUM_SUB_ACTIVE
import ediger.diarynutrition.PREF_PREMIUM_SUB_PENDING
import ediger.diarynutrition.PRODUCT_SUB_PREMIUM
import ediger.diarynutrition.R
import ediger.diarynutrition.objects.SingleLiveEvent
import ediger.diarynutrition.util.SharedPreferenceBooleanLiveData
import kotlinx.coroutines.launch

class BillingViewModel(val app: Application): AndroidViewModel(app) {

    private val billingRepository = (app as AppContext).billingRepository

    private val subscriptionPurchases =
        (app as AppContext).billingClientLifecycle.subscriptionPurchases
    private val oneTimeProductPurchases =
        (app as AppContext).billingClientLifecycle.oneTimeProductPurchases

    val oneTimeProductDetails = (app as AppContext).billingClientLifecycle.oneTimeProductDetails
    val subscriptionProductDetails = (app as AppContext).billingClientLifecycle.subscriptionProductDetails

    val buyEvent = SingleLiveEvent<BillingFlowParams>()

    val openPlayStoreSubscriptionsEvent = SingleLiveEvent<String>()

    val isPremiumActive = SharedPreferenceBooleanLiveData(
        app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE), PREF_PREMIUM, false)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    fun getSubscriptionPrices(productDetails: ProductDetails): Map<String, String> {
        val pricesMap = mutableMapOf<String, String>()
        productDetails.subscriptionOfferDetails?.forEach {
            it.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice?.let { price ->
                val offerTag = it.offerTags.first { tag ->
                    tag in listOf(
                        PLAN_PREMIUM_MONTHLY,
                        PLAN_PREMIUM_SEASONALLY,
                        PLAN_PREMIUM_ANNUALLY
                    )
                }
                pricesMap[offerTag] = price
            }
        }
        return pricesMap
    }

    fun getAllPurchases(): List<Purchase> =
        subscriptionPurchases.value + oneTimeProductPurchases.value

    fun refreshPurchases() = viewModelScope.launch {
            _isLoading.postValue(true)

            (app as AppContext).billingClientLifecycle.refreshPurchases()

            _isLoading.postValue(false)
    }

    fun manageSubscription(productToken: String) {
        if (subscriptionPurchases.value.isNotEmpty()) {
            val activeSubscriptionToken = app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
                .getString(PREF_PREMIUM_SUB_ACTIVE, "")
            if (productToken == activeSubscriptionToken) {
                openPlayStoreSubscriptionsEvent.postValue(PRODUCT_SUB_PREMIUM)
            }
            // else -- UpDowngradeSubscription
        } else {
            buySubscription(productToken)
        }
    }

    fun buyOneTimeProduct() {
        if (subscriptionPurchases.value.isNotEmpty()) {
            Log.e(TAG, "SubscriptionPurchase is found. Cannot make subscription purchase")
            _snackbarText.postValue(
                Event(R.string.message_billing_onetime_fail)
            )
            return
        }
        val productDetails: ProductDetails = oneTimeProductDetails.value ?: run {
            Log.e(TAG, "Could not find ProductDetails to make purchase.")
            return
        }
        val billingParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        ).build()

        buyEvent.postValue(billingParams)
    }

    private fun buySubscription(productTag: String) {
        if (oneTimeProductPurchases.value.isNotEmpty()) {
            Log.e(TAG, "OneTimePurchase is found. Cannot make subscription purchase")
            return
        }
        val productDetails: ProductDetails = subscriptionProductDetails.value ?: run {
            Log.e(TAG, "Could not find ProductDetails to make the purchase")
            return
        }
        if (productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
            Log.e(TAG, "There is no information about subscription plans")
            return
        }

        val offers = productDetails.subscriptionOfferDetails?.let {
           retrieveEligibleOffers(
               offerDetails = it,
               tag = productTag
           )
        }

        val offerToken = offers?.let { leastPricedOfferToken(it) } ?: run {
            Log.e(TAG, "Invalid offer token")
            return
        }

        if (subscriptionPurchases.value.isNotEmpty()) {
            val currentPurchase = subscriptionPurchases.value.first()
            val oldPurchaseToken = currentPurchase.purchaseToken

            val billingParams =
                upDowngradeBillingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = offerToken,
                    oldToken = oldPurchaseToken
                )
            buyEvent.postValue(billingParams)
        } else {
            buyEvent.postValue(
                billingFlowParamsBuilder(productDetails, offerToken)
            )
            // Mark chosen plan
            app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
                .edit()
                .putString(PREF_PREMIUM_SUB_PENDING, productTag)
                .apply()

        }
    }

    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>,
        tag: String
    ): List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }

        return eligibleOffers
    }

    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>
    ): String {
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

    private fun upDowngradeBillingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String,
        oldToken: String
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
                .build()
        ).build()
    }

    private fun billingFlowParamsBuilder(productDetails: ProductDetails, offerToken: String):
            BillingFlowParams {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        ).build()
    }

   companion object {
       private const val TAG = "BillingViewModel"
   }
}