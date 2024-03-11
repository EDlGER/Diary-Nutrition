package ediger.diarynutrition.billing

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase

fun purchaseForSku(purchases: List<Purchase>?, sku: String): Purchase? {
    purchases?.let {
        for (purchase in it) {
            if (purchase.skus[0] == sku) {
                return purchase
            }
        }
    }
    return null
}

fun hasPurchase(purchases: List<Purchase>?, sku: String): Boolean =
    purchaseForSku(purchases, sku) != null

fun getActivePurchase(purchases: List<Purchase>?): Purchase? {
    purchases?.let { purchasesList ->
        if (purchasesList.isNotEmpty()) {
            return purchasesList.first()
        }
    }
    return null
}


//fun isSubscription(sku: String) =
//    sku in listOf(PRODUCT_SUB_MONTHLY, PRODUCT_SUB_SEASON, PRODUCT_SUB_ANNUALLY)


//fun Purchase.isSubscription() =
//    this.skus.first() in listOf(PRODUCT_SUB_MONTHLY, PRODUCT_SUB_SEASON, PRODUCT_SUB_ANNUALLY)