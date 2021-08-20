package ediger.diarynutrition.billing

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.objects.SingleLiveEvent

class BillingViewModel(app: Application): AndroidViewModel(app) {

    private val purchases = (app as AppContext).billingClientLifecycle.purchases

    private val skusWithDetails = (app as AppContext).billingClientLifecycle.skusWithSkuDetails

    val buyEvent = SingleLiveEvent<BillingFlowParams>()

    val openPlayStoreSubscriptionsEvent = SingleLiveEvent<String>()

    fun buySubscription(sku: String) {
        
        //TODO: Determine upgrade/downgrade, check if subscription is active

        buy(sku, null)
    }

    /**
     * It constructs BillingFlowParams for the [sku] and notify buyEvent
     * Later the activity that subscribed on buyEvent calls billingClientLifecycle.launchBillingFlow
     */
    private fun buy(sku: String, oldSku: String?) {
        if (hasPurchase(purchases.value, sku)) {
            Log.e("Billing", "You cannot buy a SKU that is already owned: $sku.")
            return
        }
        
        val oldSkuToBeReplaced = if (isSkuReplaceable(purchases.value, oldSku)) {
            sku
        } else {
            null
        }

        val skuDetails = skusWithDetails.value?.get(sku) ?: run {
            Log.e("Billing", "Could not find SkuDetails to make purchase.")
            return
        }

        val billingBuilder = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
        
        if (oldSkuToBeReplaced != null && oldSkuToBeReplaced != sku) {
            purchaseForSku(purchases.value, oldSkuToBeReplaced)?.apply { 
                billingBuilder.setSubscriptionUpdateParams(
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldSkuPurchaseToken(purchaseToken)
                        .build()
                )
            }
        }
        val billingParams = billingBuilder.build()

        buyEvent.postValue(billingParams)
    }

    private fun isSkuReplaceable(purchases: List<Purchase>?, sku: String?): Boolean {
        if (sku == null) {
            return false
        }
        if (!hasPurchase(purchases, sku)) {
            Log.e("Billing", "You cannot replace sku that is not already owned")
            return false
        }
        return true
    }
    
}