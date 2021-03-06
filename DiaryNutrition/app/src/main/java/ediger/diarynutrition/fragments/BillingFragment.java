package ediger.diarynutrition.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.util.IabHelper;
import ediger.diarynutrition.util.IabResult;
import ediger.diarynutrition.util.Inventory;
import ediger.diarynutrition.util.Purchase;

/**
 * Created by ediger on 15.01.17.
 */

public class BillingFragment extends Fragment {

    static final String TAG = "DiaryNutrition";
    static final String SKU_REMOVE_ADS = "com.ediger.removeads";
    static final String PREF_ADS_REMOVED = "ads_removed";

    private static final String PREF_FILE_PREMIUM = "premium_data";

    IabHelper mHelper;
    String payload = "123";

    private boolean isAdsRemoved;
    private TextView adsStatus;
    private Button buyButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_billing, container, false);

        buyButton = (Button) rootview.findViewById(R.id.btn_buy);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buyButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        } else {
            buyButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_semi_transparent));
        }

        isAdsRemoved = getActivity().getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false);

        adsStatus = (TextView) rootview.findViewById(R.id.txt_ads_status);
        if (isAdsRemoved) {
            adsStatus.setText(getString(R.string.billing_ads_disable));
            buyButton.setVisibility(View.GONE);
        } else {
            adsStatus.setText(getString(R.string.billing_ads_enable));
            buyButton.setVisibility(View.VISIBLE);
        }

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhuPm/qMEeS+CaCZfsFnMc8J5b6fkSlFihv" +
                        "ZwE7HlAC/hOHUinZLX3aG3m5sU3zMMTEVoCgR50/wgJt6BMFXri5V3+z+xgK4hkSHaX+L4" +
                        "djcoHgECyLjb73WwR1YbEOksQNaVM/MLIfBhtWfJuVaMwe0teAVRzGpxAPmTMB2jNYTxxt" +
                        "R9SMUwWt3VoU9lU1BJH8zd8TtPfqtSxTJbeNxe2i9/l2Ew4P2/J/BdeK1ZNnjeBM2Kz+S8" +
                        "5TuIuKhYle7x8DRf2CogSiIgZXf2Jnl+kU+vwr+Qw0QOYPwdLcakErx0ienUga3qmFTsCT" +
                        "sTS88zAFRXdFYDPip+CBrkpzbPhwIDAQAB";

        mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
        mHelper.enableDebugLogging(false);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                    return;
                }

                if(mHelper == null) {
                    return;
                }

                Log.d(TAG, "In-app Billing setup is OK");
                mHelper.queryInventoryAsync(mReceivedInventoryListener);
            }
        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelper.launchPurchaseFlow(getActivity(), SKU_REMOVE_ADS, 10001,
                        mPurchaseFinishedListener, payload);
            }
        });

        return rootview;
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            Log.d(TAG, "Query inventory finished");

            if (mHelper == null)
                return;

            if (result.isFailure()) {
                return;
            }

            Log.d(TAG, "Query inventory was successful");

            Purchase removeAdsPurchase = inv.getPurchase(SKU_REMOVE_ADS);
            isAdsRemoved = (removeAdsPurchase != null);
            //isAdsRemoved = (removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase));
            //inv.hasPurchase(SKU_REMOVE_ADS);

            SharedPreferences.Editor spe = getActivity().getSharedPreferences(PREF_FILE_PREMIUM,
                    Context.MODE_PRIVATE).edit();
            spe.putBoolean(PREF_ADS_REMOVED, isAdsRemoved);
            spe.apply();

            Log.d(TAG, "Ads is " + (isAdsRemoved ? "REMOVED" : "NOT REMOVED"));

        }
    };

    private boolean verifyDeveloperPayload (Purchase p) {
        String payload = p.getDeveloperPayload();

        //Some code

        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (mHelper == null) return;

            if (result.isFailure()) return;

            //if (!verifyDeveloperPayload(info)) return;

            if (info.getSku().equals(SKU_REMOVE_ADS)) {
                Toast.makeText(getActivity(), getString(R.string.message_billing_success),
                        Toast.LENGTH_SHORT).show();
                isAdsRemoved = true;
                buyButton.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }

    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!getUserVisibleHint()) {
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.menuMultipleActions.setVisibility(View.INVISIBLE);
        mainActivity.datePicker.setVisibility(View.INVISIBLE);

        if (getResources().getDisplayMetrics().density > 2.0) {
            mainActivity.title.setPadding(0, 40, 0, 0);
        } else {
            mainActivity.title.setPadding(0, 25, 0, 0);
        }

        isAdsRemoved = getActivity().getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false);

        if (isAdsRemoved) {
            adsStatus.setText(getString(R.string.billing_ads_disable));
            buyButton.setVisibility(View.GONE);
        } else {
            adsStatus.setText(getString(R.string.billing_ads_enable));
            buyButton.setVisibility(View.VISIBLE);
        }
    }
}
