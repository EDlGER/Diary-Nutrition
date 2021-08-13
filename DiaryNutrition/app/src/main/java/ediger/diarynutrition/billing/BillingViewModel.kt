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

    val openPlayStoreSubscriptionsEvent = SingleLiveEvent<String>()


    fun buySubscription(sku: String) {

    }

    // TODO: Buy subscription
    /**
     * It constructs BillingFlowParams for the [sku] and notify buyEvent
     * Later the activity that subscribed on buyEvent calls billingClientLifecycle.launchBillingFlow
     */
    fun buy(sku: String) {

    }

}