package ediger.diarynutrition.billing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.BillingFlowParams
import ediger.diarynutrition.AppContext
import ediger.diarynutrition.objects.SingleLiveEvent

class BillingViewModel(app: Application): AndroidViewModel(app) {

    private val purchases = (app as AppContext).billingClientLifecycle.purchases

    private val skusWithDetails = (app as AppContext).billingClientLifecycle.skusWithSkuDetails

    val buyEvent = SingleLiveEvent<BillingFlowParams>()


    /*
    val client: BillingClient = ...
    val acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener = ...

    suspend fun handlePurchase() {
        if (purchase.purchaseState === PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                   client.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
            }
         }
    }

     */

}