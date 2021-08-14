package ediger.diarynutrition.billing

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