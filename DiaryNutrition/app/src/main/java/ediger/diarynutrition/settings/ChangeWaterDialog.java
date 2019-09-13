package ediger.diarynutrition.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;


public class ChangeWaterDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.DialogStyle);

        View relative = requireActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);
        builder.setView(relative);
        builder.setTitle(getString(R.string.dialog_title_water_edit));

        TextInputLayout til = relative.findViewById(R.id.inputLayout);
        final TextInputEditText changeWater = til.findViewById(R.id.edit);
        til.setHint(getString(R.string.dialog_amount_water));

        builder.setPositiveButton(R.string.dialog_change, (dialog, which) -> {
                    Toast toast;

                    if (changeWater.getText().toString().equals("") ||
                            changeWater.getText().toString().startsWith("0")) {
                        toast = Toast.makeText(requireContext(),
                                getString(R.string.message_dialog_water),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        int water = Integer.parseInt(changeWater.getText().toString());
                        PreferenceHelper.setValue(PreferenceHelper.KEY_PROGRAM_WATER, water);
                    }
                });

        builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        return builder.create();
    }

}
