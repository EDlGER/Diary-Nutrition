package ediger.diarynutrition.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.FragmentBillingBinding
import ediger.diarynutrition.util.showSnackbar

class BillingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentBillingBinding

    private lateinit var subsCards: List<MaterialCardView>

    private val billingViewModel: BillingViewModel by activityViewModels()

    private var selectedSubId = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBillingBinding.inflate(inflater, container, false)

        with(binding) {
            subsCards = listOf(crdSubscription1, crdSubscription2, crdSubscription3)
            toggleSubState(crdSubscription2)
        }

        subsCards.forEach { it.setOnClickListener(this) }

        binding.fabPayment.setOnClickListener {
            billingViewModel.buySubscription(BillingClientLifecycle.SUBS_SKUS[selectedSubId])
        }

        return binding.root
    }

    // Subscription cards
    override fun onClick(v: View?) {
        val selectedSub = v as? MaterialCardView
        if (selectedSub?.isChecked == false) {
            toggleSubState(selectedSub)
        }
        selectedSubId = subsCards.indexOf(selectedSub)

        toggleSubState(
                subsCards.find { it != selectedSub && it.isChecked }
        )

        // TODO: Temporary
        view?.showSnackbar("Selected card position: $selectedSubId", Snackbar.LENGTH_SHORT)
    }

    private fun toggleSubState(card: MaterialCardView?) {
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

/*

var mHelper: IabHelper? = null
    var payload = "123"
    private var isAdsRemoved = false
    private var adsStatus: TextView? = null
    private var buyButton: Button? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootview = inflater.inflate(R.layout.fragment_billing_old, container, false)
        buyButton = rootview.findViewById<View>(R.id.btn_buy) as Button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buyButton!!.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        } else {
            buyButton!!.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black_semi_transparent))
        }
        isAdsRemoved = requireActivity().getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false)
        adsStatus = rootview.findViewById<View>(R.id.txt_ads_status) as TextView
        if (isAdsRemoved) {
            adsStatus!!.text = getString(R.string.billing_ads_disable)
            buyButton!!.visibility = View.GONE
        } else {
            adsStatus!!.text = getString(R.string.billing_ads_enable)
            buyButton!!.visibility = View.VISIBLE
        }
        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhuPm/qMEeS+CaCZfsFnMc8J5b6fkSlFihv" +
                "ZwE7HlAC/hOHUinZLX3aG3m5sU3zMMTEVoCgR50/wgJt6BMFXri5V3+z+xgK4hkSHaX+L4" +
                "djcoHgECyLjb73WwR1YbEOksQNaVM/MLIfBhtWfJuVaMwe0teAVRzGpxAPmTMB2jNYTxxt" +
                "R9SMUwWt3VoU9lU1BJH8zd8TtPfqtSxTJbeNxe2i9/l2Ew4P2/J/BdeK1ZNnjeBM2Kz+S8" +
                "5TuIuKhYle7x8DRf2CogSiIgZXf2Jnl+kU+vwr+Qw0QOYPwdLcakErx0ienUga3qmFTsCT" +
                "sTS88zAFRXdFYDPip+CBrkpzbPhwIDAQAB"
        mHelper = IabHelper(activity, base64EncodedPublicKey)
        mHelper!!.enableDebugLogging(false)
        mHelper!!.startSetup(OnIabSetupFinishedListener { result ->
            if (!result.isSuccess) {
                Log.d(TAG, "In-app Billing setup failed: $result")
                return@OnIabSetupFinishedListener
            }
            if (mHelper == null) {
                return@OnIabSetupFinishedListener
            }
            Log.d(TAG, "In-app Billing setup is OK")
            mHelper!!.queryInventoryAsync(mReceivedInventoryListener)
        })
        buyButton!!.setOnClickListener {
            mHelper!!.launchPurchaseFlow(activity, SKU_REMOVE_ADS, 10001,shReportData calculate detail steps data
01-02 13:02:30.532 30135  2143 W HiH_HiSyncControl: 1200012
                    mPurchaseFinishedListener, payload)
        }
        return rootview
    }

    var mReceivedInventoryListener = QueryInventoryFinishedListener { result, inv ->
        Log.d(TAG, "Query inventory finished")
        if (mHelper == null) return@QueryInventoryFinishedListener
        if (result.isFailure) {
            return@QueryInventoryFinishedListener
        }
        Log.d(TAG, "Query inventory was successful")
        val removeAdsPurchase = inv.getPurchase(SKU_REMOVE_ADS)
        isAdsRemoved = removeAdsPurchase != null
        //isAdsRemoved = (removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase));
        //inv.hasPurchase(SKU_REMOVE_ADS);
        val spe = requireActivity().getSharedPreferences(PREF_FILE_PREMIUM,
                Context.MODE_PRIVATE).edit()
        spe.putBoolean(PREF_ADS_REMOVED, isAdsRemoved)
        spe.apply()
        Log.d(TAG, "Ads is " + if (isAdsRemoved) "REMOVED" else "NOT REMOVED")
    }

    private fun verifyDeveloperPayload(p: Purchase): Boolean {
        val payload = p.developerPayload

        //Some code
        return true
    }

    var mPurchaseFinishedListener = OnIabPurchaseFinishedListener { result, info ->
        if (mHelper == null) return@OnIabPurchaseFinishedListener
        if (result.isFailure) return@OnIabPurchaseFinishedListener

        //if (!verifyDeveloperPayload(info)) return;
        if (info.sku == SKU_REMOVE_ADS) {
            Toast.makeText(activity, getString(R.string.message_billing_success),
                    Toast.LENGTH_SHORT).show()
            isAdsRemoved = true
            buyButton!!.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (mHelper == null) return
        if (!mHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHelper != null) {
            mHelper!!.dispose()
            mHelper = null
        }
    }

    override fun setUserVisibleHint(visible: Boolean) {
        super.setUserVisibleHint(visible)
        if (visible && isResumed) {
            onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!userVisibleHint) {
            return
        }

//        MainActivity mainActivity = (MainActivity) getActivity();
//        mainActivity.menuMultipleActions.setVisibility(View.INVISIBLE);
//        mainActivity.datePicker.setVisibility(View.INVISIBLE);
//
//        if (getResources().getDisplayMetrics().density > 2.0) {
//            mainActivity.title.setPadding(0, 40, 0, 0);
//        } else {
//            mainActivity.title.setPadding(0, 25, 0, 0);
//        }
        isAdsRemoved = requireActivity().getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false)
        if (isAdsRemoved) {
            adsStatus!!.text = getString(R.string.billing_ads_disable)
            buyButton!!.visibility = View.GONE
        } else {
            adsStatus!!.text = getString(R.string.billing_ads_enable)
            buyButton!!.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "DiaryNutrition"
        const val SKU_REMOVE_ADS = "com.ediger.removeads"
        const val PREF_ADS_REMOVED = "ads_removed"
        private const val PREF_FILE_PREMIUM = "premium_data"
    }

 */