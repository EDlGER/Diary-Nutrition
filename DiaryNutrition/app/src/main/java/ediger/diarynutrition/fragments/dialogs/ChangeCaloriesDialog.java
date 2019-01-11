package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.SettingsFragment;

/**
 * Created by root on 06.12.16.
 */
public class ChangeCaloriesDialog extends DialogFragment {

    private SharedPreferences pref;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);
        builder.setView(relative);
        builder.setTitle(getString(R.string.dialog_title_c));

        TextInputLayout til = (TextInputLayout) relative.findViewById(R.id.inputLayout);
        final EditText changeCal = (EditText) til.findViewById(R.id.edit);
        til.setHint(getString(R.string.macro_cal));

        builder.setPositiveButton(R.string.dialog_change,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast;

                        if (changeCal.getText().toString().equals("") ||
                                changeCal.getText().toString().startsWith("0")) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.message_dialog_cal),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            int cal = Integer.parseInt(changeCal.getText().toString());

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt(SettingsFragment.KEY_PREF_CALORIES, cal);
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
