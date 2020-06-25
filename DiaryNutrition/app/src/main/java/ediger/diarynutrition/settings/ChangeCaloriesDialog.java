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

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;

public class ChangeCaloriesDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        View relative = requireActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);
        builder.setView(relative);
        builder.setTitle(getString(R.string.dialog_title_c));

        TextInputLayout til = relative.findViewById(R.id.inputLayout);
        final TextInputEditText changeCal = til.findViewById(R.id.edit);
        til.setHint(getString(R.string.macro_cal));

        builder.setPositiveButton(R.string.dialog_change, (dialog, which) -> {
                    Toast toast;

                    if (changeCal.getText().toString().equals("")
                            || changeCal.getText().toString().startsWith("0")) {
                        toast = Toast.makeText(requireContext(),
                                getString(R.string.message_dialog_cal), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        float cal = Float.parseFloat(changeCal.getText().toString());
                        PreferenceHelper.setValue(Consts.KEY_PROGRAM_CAL, cal);
                    }
                });

        builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss());

        builder.setCancelable(true);
        return builder.create();
    }
}
