package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.AppContext;

public class ChangeFoodDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View relative1 = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_food_old, null);
        builder.setView(relative1);
        builder.setTitle(getString(R.string.dialog_title_change));

        final EditText txtNameF = (EditText) relative1.findViewById(R.id.txtNameF);
        final EditText txtCalF = (EditText) relative1.findViewById(R.id.txtCalF);
        final EditText txtCarboF = (EditText) relative1.findViewById(R.id.txtCarboF);
        final EditText txtProtF = (EditText) relative1.findViewById(R.id.txtProtF);
        final EditText txtFatF = (EditText) relative1.findViewById(R.id.txtFatF);

        long id = getArguments().getLong("id");
        String[] asColumnsToResult = AppContext.Companion.getDbDiary().getListFood();
        String selections = "_id = "+id;

        //TODO mock
        Cursor c = null;
        /*Cursor c = AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections, null,
                null, null, null);*/
        c.moveToFirst();

        txtNameF.setText(c.getString(0));
        txtCalF.setText(c.getString(1));
        txtCarboF.setText(c.getString(2));
        txtProtF.setText(c.getString(3));
        txtFatF.setText(c.getString(4));

        c.close();

        builder.setPositiveButton(R.string.context_menu_change,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast toast;

                        if (txtNameF.getText().toString().matches("") ||
                                txtCalF.getText().toString().matches("") ||
                                txtCarboF.getText().toString().matches("") ||
                                txtProtF.getText().toString().matches("") ||
                                txtFatF.getText().toString().matches("")) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.message_dialog_food_empty), Toast.LENGTH_SHORT);
                            toast.show();
                        } else if ((txtCalF.getText().toString().compareTo(".") == 0) ||
                                (txtCarboF.getText().toString().compareTo(".") == 0) ||
                                (txtProtF.getText().toString().compareTo(".") == 0) ||
                                (txtFatF.getText().toString().compareTo(".") == 0)) {
                            toast = Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.message_dialog_food_point),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            long food_id = getArguments().getLong("id");
                            AppContext.Companion.getDbDiary().editFood(food_id, txtNameF.getText().toString(),
                                    Float.parseFloat(txtCalF.getText().toString()),
                                    Float.parseFloat(txtCarboF.getText().toString()),
                                    Float.parseFloat(txtProtF.getText().toString()),
                                    Float.parseFloat(txtFatF.getText().toString()));

                            getTargetFragment().onActivityResult(3, 1, null);
                        }
                    }
                });
        builder.setNegativeButton(R.string.dialog_cancel_f,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        return builder.create();
    }

}
