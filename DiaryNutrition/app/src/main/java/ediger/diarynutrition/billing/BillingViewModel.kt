package ediger.diarynutrition.billing

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.PLAN_PREMIUM_ANNUALLY
import ediger.diarynutrition.PLAN_PREMIUM_MONTHLY
import ediger.diarynutrition.PLAN_PREMIUM_SEASONALLY
import ediger.diarynutrition.PREF_FILE_PREMIUM
import ediger.diarynutrition.PREF_PREMIUM
import ediger.diarynutrition.objects.SingleLiveEvent
import ediger.diarynutrition.util.SharedPreferenceBooleanLiveData

class BillingViewModel(app: Application): AndroidViewModel(app) {

    private val billingRepository = (app as AppContext).billingRepository

    private val subscriptionPurchases =
        (app as AppContext).billingClientLifecycle.subscriptionPurchases
    private val oneTimeProductPurchases =
        (app as AppContext).billingClientLifecycle.oneTimeProductPurchases

    val oneTimeProductDetails = billingRepository.oneTimeProductDetails
    val subscriptionProductDetails = billingRepository.subscriptionProductDetails

    val buyEvent = SingleLiveEvent<BillingFlowParams>()

    val openPlayStoreSubscriptionsEvent = SingleLiveEvent<String>()

    val isPremiumActive = SharedPreferenceBooleanLiveData(
        app.getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE), PREF_PREMIUM, false)

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

    fun buySubscription(productTag: String) {
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
        val currentPurchases = subscriptionPurchases.value

        if (currentPurchases.isNotEmpty() && currentPurchases.size == 1) {
            val currentPurchase = currentPurchases.first()
            val oldPurchaseToken = currentPurchase.purchaseToken

            val billingParams =
                upDowngradeBillingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = offerToken,
                    oldToken = oldPurchaseToken
                )
            buyEvent.postValue(billingParams)
        } else if (currentPurchases.isEmpty()) {
            buyEvent.postValue(
                billingFlowParamsBuilder(productDetails, offerToken)
            )
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