package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 14.09.15.
 */
public class AddFoodDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.add_f_dialog,null);
        builder.setView(relative);
        builder.setTitle(R.string.dialog_tittle_f);

        final EditText txtNameF = (EditText) relative.findViewById(R.id.txtNameF);
        final EditText txtCalF = (EditText) relative.findViewById(R.id.txtCalF);
        final EditText txtCarboF = (EditText) relative.findViewById(R.id.txtCarboF);
        final EditText txtProtF = (EditText) relative.findViewById(R.id.txtProtF);
        final EditText txtFatF = (EditText) relative.findViewById(R.id.txtFatF);

        builder.setPositiveButton(R.string.dialog_add_f,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast toast;

                        if (txtNameF.getText().toString().matches("") ||
                                txtCalF.getText().toString().matches("") ||
                                txtCarboF.getText().toString().matches("") ||
                                txtProtF.getText().toString().matches("") ||
                                txtFatF.getText().toString().matches("") ) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Не все поля заполнены",Toast.LENGTH_SHORT);
                            toast.show();
                        } else if ((txtCalF.getText().toString().compareTo(".") == 0) ||
                                (txtCarboF.getText().toString().compareTo(".") == 0) ||
                                (txtProtF.getText().toString().compareTo(".") == 0) ||
                                (txtFatF.getText().toString().compareTo(".") == 0)) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Одно или несколько полей содежат неправильный формат числа",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            AppContext.getDbDiary().addFood(txtNameF.getText().toString(),
                                    Float.parseFloat(txtCalF.getText().toString()),
                                    Float.parseFloat(txtCarboF.getText().toString()),
                                    Float.parseFloat(txtProtF.getText().toString()),
                                    Float.parseFloat(txtFatF.getText().toString()));
                            getTargetFragment().onActivityResult(2,1,null);
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
