package ediger.diarynutrition.billing

import com.android.billingclient.api.Purchase
import ediger.diarynutrition.SKU_SUB_ANNUALLY
import ediger.diarynutrition.SKU_SUB_MONTHLY
import ediger.diarynutrition.SKU_SUB_SEASON

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

fun Purchase.isSubscription() =
    this.skus.first() in listOf(SKU_SUB_MONTHLY, SKU_SUB_SEASON, SKU_SUB_ANNUALLY)