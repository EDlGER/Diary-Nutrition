package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.design.widget.TextInputLayout;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;

public class AddWeightDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_weight,null);
        builder.setView(relative);
        builder.setTitle(getString(R.string.dialog_tittle_w));

        TextInputLayout til = (TextInputLayout) relative.findViewById(R.id.weightInputLayout);
        final EditText editWeight = (EditText) til.findViewById(R.id.editWeight);
        til.setHint(getString(R.string.dialog_weight));

        builder.setPositiveButton(R.string.dialog_add,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast;
                        long date = AppContext.getDate();

                        if (editWeight.getText().toString().matches("")) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.message_dialog_weight), Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Cursor cursor = AppContext.getDbDiary().getWeight(date);
                            if (cursor.moveToFirst()) {
                                AppContext.getDbDiary().setWeight(date,
                                        Float.parseFloat(editWeight.getText().toString()));
                            } else {
                                AppContext.getDbDiary().addWeight(date,
                                        Float.parseFloat(editWeight.getText().toString()));
                            }
                            cursor.close();
                        }

                    }
                });

        builder.setNegativeButton(R.string.dialog_cancel_f,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(true);
        return builder.create();
    }
}
