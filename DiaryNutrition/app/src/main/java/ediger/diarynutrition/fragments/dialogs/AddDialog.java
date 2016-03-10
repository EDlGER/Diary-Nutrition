package ediger.diarynutrition.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 07.02.16.
 */
public class AddDialog extends DialogFragment {

    private Calendar now = Calendar.getInstance();
    private int nowHour = now.get(Calendar.HOUR_OF_DAY);
    private int nowMin = now.get(Calendar.MINUTE);
    private TextInputLayout servLayout;
    private EditText servPicker;
    private NumberPicker hourPicker;
    private NumberPicker minPicker;
    private Spinner mealPicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View relative = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_rec, null);
        builder.setView(relative);

        servLayout = (TextInputLayout) relative.findViewById(R.id.servInputLayout);
        servPicker = (EditText) servLayout.findViewById(R.id.txtAddServ);
        hourPicker = (NumberPicker) relative.findViewById(R.id.hourPicker);
        minPicker = (NumberPicker) relative.findViewById(R.id.minutePicker);
        mealPicker = (Spinner) relative.findViewById(R.id.spMeal);


        servPicker.setText(R.string.dialog_serv_std);

        hourPicker.setMaxValue(23);
        hourPicker.setMinValue(0);
        hourPicker.setValue(nowHour);

        minPicker.setMaxValue(59);
        minPicker.setMinValue(0);
        minPicker.setValue(nowMin);

        Cursor meal = AppContext.getDbDiary().getMealData();
        String[] columns = AppContext.getDbDiary().getListMeal();
        int[] to = new int[] {android.R.id.text1};

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,meal,columns,to,0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealPicker.setAdapter(mAdapter);

        int hour = hourPicker.getValue();
        if (0 <= hour && hour < 10) mealPicker.setSelection(0);
        if (9 < hour && hour < 12) mealPicker.setSelection(1);
        if (11 < hour && hour < 15) mealPicker.setSelection(2);
        if (14 < hour && hour < 18) mealPicker.setSelection(3);
        if (17 < hour && hour < 24) mealPicker.setSelection(4);


        builder.setPositiveButton(R.string.dialog_add,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int addserv;
                        long cal;
                        long addtime;
                        long addid;
                        Calendar date;
                        Cursor cursor = AppContext.getDbDiary().getDate();

                        cursor.moveToFirst();
                        cal = cursor.getLong(0);
                        cursor.close();

                        date = Calendar.getInstance();
                        Bundle bundle = getArguments();

                        addid = bundle.getLong("id");


                        if (servPicker.getText().toString().matches("")) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.message_serving,Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            date.setTimeInMillis(cal);
                            addserv = Integer.parseInt(servPicker.getText().toString());
                            date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                                    date.get(Calendar.DAY_OF_MONTH),
                                    hourPicker.getValue(), minPicker.getValue());
                            addtime = date.getTimeInMillis();

                            int mealId = mealPicker.getSelectedItemPosition();
                            AppContext.getDbDiary().addRec(addid, addserv, addtime, mealId + 1);
                            getTargetFragment().onActivityResult(1,1,null);
                        }
                    }
                });
        builder.setNegativeButton(R.string.dialog_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        return builder.create();
    }
}
