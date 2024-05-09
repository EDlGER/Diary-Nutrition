package ediger.diarynutrition.billing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.card.MaterialCardView
import ediger.diarynutrition.MainActivity
import ediger.diarynutrition.PLAN_PREMIUM_ANNUALLY
import ediger.diarynutrition.PLAN_PREMIUM_MONTHLY
import ediger.diarynutrition.PLAN_PREMIUM_SEASONALLY
import ediger.diarynutrition.PREF_FILE_PREMIUM
import ediger.diarynutrition.PREF_PREMIUM_SUB_ACTIVE
import ediger.diarynutrition.PRODUCT_PREMIUM_UNLIMITED
import ediger.diarynutrition.PRODUCT_SUB_PREMIUM
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.FragmentBillingBinding

class BillingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentBillingBinding

    private lateinit var productCards: List<MaterialCardView>

    private val billingViewModel: BillingViewModel by activityViewModels()

    private var selectedProductIndex = 1

    private var activeProductIndex = -1

    private val productList = listOf(
        PLAN_PREMIUM_MONTHLY,
        PLAN_PREMIUM_SEASONALLY,
        PLAN_PREMIUM_ANNUALLY,
        PRODUCT_PREMIUM_UNLIMITED
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBillingBinding.inflate(inflater, container, false)

        addOptionsMenu()

        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            productCards = listOf(crdSubscription1, crdSubscription2, crdSubscription3, crdOnetime)
            toggleProductCardState(crdSubscription2)
        }

        productCards.forEach { it.setOnClickListener(this) }

        binding.fabPayment.setOnClickListener {
            val selectedProduct = productList[selectedProductIndex]
            if (selectedProduct == PRODUCT_PREMIUM_UNLIMITED)
                billingViewModel.buyOneTimeProduct()
            else
                billingViewModel.manageSubscription(selectedProduct)
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

        billingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            (requireActivity() as MainActivity).toggleProgress(isLoading)
        }

        billingViewModel.isPremiumActive.observe(viewLifecycleOwner) { isPremiumActive ->
            val purchaseList = billingViewModel.getAllPurchases()

            if (purchaseList.isNotEmpty()) {
                var productId = purchaseList.first().products.first()
                if (productId == PRODUCT_SUB_PREMIUM) {
                    productId = requireActivity().applicationContext
                        .getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                        .getString(PREF_PREMIUM_SUB_ACTIVE, "")
                }

                activeProductIndex = productList.indexOf(productId)
                if (activeProductIndex != -1) {
                    productCards[activeProductIndex].setCardBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.colorAccentSecondary)
                    )
                }
            }
            // TODO: Temporary for testing
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
        selectedProductIndex = productCards.indexOf(selectedProductCard)

        toggleProductCardState(
                productCards.find { it != selectedProductCard && it.isChecked }
        )

        when (selectedProductIndex) {
            activeProductIndex ->
                binding.fabPayment.text = getString(R.string.action_manage)
            productList.indexOf(PRODUCT_PREMIUM_UNLIMITED) ->
                binding.fabPayment.text = getString(R.string.action_buy)
            else ->
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

    private fun addOptionsMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_billing, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_refresh) {
                    billingViewModel.refreshPurchases()
                    return true
                }
                return false
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

}