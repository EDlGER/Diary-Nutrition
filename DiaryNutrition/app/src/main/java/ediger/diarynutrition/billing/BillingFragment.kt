package ediger.diarynutrition.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import ediger.diarynutrition.PLAN_PREMIUM_ANNUALLY
import ediger.diarynutrition.PLAN_PREMIUM_MONTHLY
import ediger.diarynutrition.PLAN_PREMIUM_SEASONALLY
import ediger.diarynutrition.PRODUCT_PREMIUM_UNLIMITED
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.FragmentBillingBinding
import kotlinx.coroutines.launch

class BillingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentBillingBinding

    private lateinit var productCards: List<MaterialCardView>

    private val billingViewModel: BillingViewModel by activityViewModels()

    private var selectedProductId = 1

    private val productList = listOf(
        PLAN_PREMIUM_MONTHLY,
        PLAN_PREMIUM_SEASONALLY,
        PLAN_PREMIUM_ANNUALLY,
        PRODUCT_PREMIUM_UNLIMITED
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBillingBinding.inflate(inflater, container, false)

        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            productCards = listOf(crdSubscription1, crdSubscription2, crdSubscription3, crdOnetime)
            toggleProductCardState(crdSubscription2)
        }

        productCards.forEach { it.setOnClickListener(this) }

        binding.fabPayment.setOnClickListener {
            val selectedProduct = productList[selectedProductId]

            if (selectedProduct == PRODUCT_PREMIUM_UNLIMITED) {
                billingViewModel.buyOneTimeProduct()
            } else {
                billingViewModel.buySubscription(selectedProduct)
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingViewModel.subscriptionProductDetails.observe(viewLifecycleOwner) {
            it?.let { product ->
                val prices = billingViewModel.getSubscriptionPrices(product)
                with(binding) {
                    txtPriceMonthly.text = prices[PLAN_PREMIUM_MONTHLY] ?: ""
                    txtPriceSeasonally.text = prices[PLAN_PREMIUM_SEASONALLY] ?: ""
                    txtPriceAnnually.text = prices[PLAN_PREMIUM_ANNUALLY] ?: ""
                }
            }
        }
        billingViewModel.oneTimeProductDetails.observe(viewLifecycleOwner) {
            it?.let { product ->
                if (product.productId == PRODUCT_PREMIUM_UNLIMITED) {
                    val price = product.oneTimePurchaseOfferDetails?.formattedPrice
                    binding.txtPriceOnetime.text = price ?: ""
                }
            }
        }

        // TODO: Temporary for testing
        billingViewModel.isPremiumActive.observe(viewLifecycleOwner) { isPremiumActive ->
            binding.txtPremiumStatus.text =
                if (isPremiumActive) "Active" else "Inactive"
        }

    }

    // Subscription cards
    override fun onClick(v: View?) {
        val selectedProductCard = v as? MaterialCardView
        if (selectedProductCard?.isChecked == false) {
            toggleProductCardState(selectedProductCard)
        }
        selectedProductId = productCards.indexOf(selectedProductCard)

        toggleProductCardState(
                productCards.find { it != selectedProductCard && it.isChecked }
        )

        if (selectedProductId == 3) {
            // OneTimePurchase Card
            binding.fabPayment.text = getString(R.string.action_buy)
        } else {
            binding.fabPayment.text = getString(R.string.action_subscribe)
        }
    }

    private fun toggleProductCardState(card: MaterialCardView?) {
        val scale = requireContext().resources.displayMetrics.density

        card?.apply {
            isChecked = !isChecked
            strokeWidth = if (isChecked) (2 * scale + 0.5f).toInt() else (1 * scale + 0.5f).toInt()
            strokeColor = if (isChecked) {
                ContextCompat.getColor(requireActivity(), R.color.colorAccent)
            } else {
                ContextCompat.getColor(requireActivity(), R.color.stroke)
            }
        }
    }

}