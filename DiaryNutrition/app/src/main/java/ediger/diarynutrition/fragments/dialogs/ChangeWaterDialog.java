package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.SettingsFragment;

/**
 * Created by ediger on 27.05.17.
 */

public class ChangeWaterDialog extends DialogFragment {

    private SharedPreferences pref;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);
        builder.setView(relative);
        builder.setTitle(getString(R.string.dialog_title_water_edit));

        TextInputLayout til = (TextInputLayout) relative.findViewById(R.id.inputLayout);
        final EditText changeWater = (EditText) til.findViewById(R.id.edit);
        til.setHint(getString(R.string.dialog_amount_water));

        builder.setPositiveButton(R.string.dialog_change,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast;

                        if (changeWater.getText().toString().equals("") ||
                                changeWater.getText().toString().startsWith("0")) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.message_dialog_water),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            int water = Integer.parseInt(changeWater.getText().toString());

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt(SettingsFragment.KEY_PREF_WATER, water);
                            editor.apply();
                        }
                    }
                });

        builder.setNegativeButton(R.string.dialog_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(true);
        return builder.create();
    }

}
