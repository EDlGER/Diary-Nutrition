package ediger.diarynutrition.data.repositories

import com.android.billingclient.api.ProductDetails
import ediger.diarynutrition.PRODUCT_PREMIUM_UNLIMITED
import ediger.diarynutrition.PRODUCT_SUB_PREMIUM
import ediger.diarynutrition.billing.BillingClientLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BillingRepository(
    billingClientLifecycle: BillingClientLifecycle,
    externalScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {



    companion object {
        @Volatile private var instance: BillingRepository? = null

        fun getInstance(billingClientLifecycle: BillingClientLifecycle) =
            instance ?: synchronized(this) {
                instance ?: BillingRepository(billingClientLifecycle).also {
                    instance = it
                }
            }

    }
}