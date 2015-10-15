package ediger.diarynutrition.fragments.dialogs;

import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

import ediger.diarynutrition.R;

/**
 * Created by root on 04.06.15.
 */
public class DateDialog extends DialogFragment  {

    //implements View.OnClickListener
    DatePicker dp;
    int year_x,month_x,day_x;
    int[] arr = new int[3];
    /*ArrayList<Integer> arrList = new ArrayList<Integer>();
    Bundle bundle;
    Button btn = (Button) getActivity().findViewById(R.id.datePicker);*/

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        getDialog().setTitle("Выбор даты");
        View v = inflater.inflate(R.layout.date_dialog, null);
        //v.findViewById(R.id.btnChange).setOnClickListener(this);
       dp = (DatePicker) v.findViewById(R.id.datePicker);
        Calendar now = Calendar.getInstance();

        year_x = now.get(Calendar.YEAR);
        month_x = now.get(Calendar.MONTH);
        day_x = now.get(Calendar.DAY_OF_MONTH);
        /*arrList.clear();
        arrList.add(year_x);
        arrList.add(month_x);
        arrList.add(day_x);
        bundle.putIntegerArrayList("newDate", arrList);
        diary_fragment df = new diary_fragment();
        df.setArguments(bundle);*/

        dp.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        /*year_x = year;
                        month_x = monthOfYear;
                        day_x = dayOfMonth;*/

                        change(year, monthOfYear, dayOfMonth);

                        Intent intent = new Intent();
                        intent.putExtra("dateFromDialog", arr);
                        getTargetFragment().onActivityResult(1, 1, intent);//////////////////////
                        dismiss();

                    }
                });
        return v;
    }
    public void change(int year,int month,int day){
        arr[0] = year;
        arr[1] = month;
        arr[2] = day;
    }
    /*@Override
    public void onClick(View v) {

        /*bundle = new Bundle();
        arrList.clear();
        arrList.add(year_x);
        arrList.add(month_x);
        arrList.add(day_x);*/


        /*bundle.putIntegerArrayList("newDate", arrList);
        diary_fragment df = new diary_fragment();
        df.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contentFrame,df).commit();*/

     //   dismiss();
    //}
}
