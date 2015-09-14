package ediger.diarynutrition.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;
import ediger.diarynutrition.Fragments.tabs.FoodTab;

/**
 * Created by root on 06.09.15.
 */
public class ChangeFoodDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

        View relative1 = getActivity().getLayoutInflater().inflate(R.layout.add_f_dialog, null);
        builder1.setView(relative1);
        builder1.setTitle(R.string.dialog_tittle_f);

        final EditText txtNameF = (EditText) relative1.findViewById(R.id.txtNameF);
        final EditText txtCalF = (EditText) relative1.findViewById(R.id.txtCalF);
        final EditText txtCarboF = (EditText) relative1.findViewById(R.id.txtCarboF);
        final EditText txtProtF = (EditText) relative1.findViewById(R.id.txtProtF);
        final EditText txtFatF = (EditText) relative1.findViewById(R.id.txtFatF);

        long id = getArguments().getLong("id");
        String arg1 = ""+id;
        String[] asColumnsToResult = AppContext.getDbDiary().getListFood();
        String selections = "_id = "+id;
        Cursor c = AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections, null,
                null, null, null);
        c.moveToFirst();

        txtNameF.setText(c.getString(0));
        txtCalF.setText(c.getString(1));
        txtCarboF.setText(c.getString(2));
        txtProtF.setText(c.getString(3));
        txtFatF.setText(c.getString(4));

        builder1.setPositiveButton(R.string.context_menu_change,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (txtNameF.getText().toString().matches("") ||
                                txtCalF.getText().toString().matches("") ||
                                txtCarboF.getText().toString().matches("") ||
                                txtProtF.getText().toString().matches("") ||
                                txtFatF.getText().toString().matches("") ){
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    "Не все поля заполнены",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            long food_id = getArguments().getLong("id");
                            AppContext.getDbDiary().editFood(food_id, txtNameF.getText().toString(),
                                    Float.parseFloat(txtCalF.getText().toString()),
                                    Float.parseFloat(txtCarboF.getText().toString()),
                                    Float.parseFloat(txtProtF.getText().toString()),
                                    Float.parseFloat(txtFatF.getText().toString()));

                            getTargetFragment().onActivityResult(1,1,null);
                        }
                    }
                });
        builder1.setNegativeButton(R.string.dialog_cancel_f,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder1.setCancelable(false);
        return builder1.create();
    }

}
