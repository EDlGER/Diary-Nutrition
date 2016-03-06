package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
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

/**
 * Created by root on 26.02.16.
 */
public class AddWeightDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.add_w_dialog,null);
        builder.setView(relative);
        builder.setTitle("Введите Ваш вес");

        TextInputLayout til = (TextInputLayout) relative.findViewById(R.id.weightImputLayout);
        final EditText editWeight = (EditText) til.findViewById(R.id.editWeight);
        til.setHint("Вес");

        builder.setPositiveButton(R.string.dialog_add,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast;
                        long date;

                        Cursor cursor = AppContext.getDbDiary().getDate();

                        cursor.moveToFirst();
                        date = cursor.getLong(0);


                        if (editWeight.getText().toString().matches("")) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Вес не введен", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            cursor = AppContext.getDbDiary().getWeight(date);
                            if (cursor.moveToFirst()) {
                                AppContext.getDbDiary().setWeight(date,
                                        Float.parseFloat(editWeight.getText().toString()));
                            } else {
                                AppContext.getDbDiary().addWeight(date,
                                        Float.parseFloat(editWeight.getText().toString()));
                            }
                        }
                        cursor.close();
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
