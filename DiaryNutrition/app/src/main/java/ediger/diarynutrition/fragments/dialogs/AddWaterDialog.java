package ediger.diarynutrition.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by ediger on 11.05.17.
 */

public class AddWaterDialog extends DialogFragment {

    public static final int REQ_WATER = 1;

    private static final String PREF_WATER_SERVING_1 = "water_serving_1";
    private static final String PREF_WATER_SERVING_2 = "water_serving_2";
    private static final String PREF_WATER_SERVING_3 = "water_serving_3";
    private static final String PREF_WATER_SERVING_4 = "water_serving_4";

    private long time;
    private int hour;
    private int minute;
    private int cardId;

    private SharedPreferences pref;

    private TextView txtWater1;
    private TextView txtWater2;
    private TextView txtWater3;
    private TextView txtWater4;

    private TextInputEditText edWater;
    private TextView txtTime;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());

    private TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);
            txtTime.setText(timeFormatter.format(calendar.getTime()));
            time = calendar.getTimeInMillis();
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_add_water, container, false);

        txtWater1 = (TextView) root.findViewById(R.id.txt_water_1);
        txtWater2 = (TextView) root.findViewById(R.id.txt_water_2);
        txtWater3 = (TextView) root.findViewById(R.id.txt_water_3);
        txtWater4 = (TextView) root.findViewById(R.id.txt_water_4);

        edWater = (TextInputEditText) root.findViewById(R.id.ed_water);
        edWater.requestFocus();

        CardView cardWater1 = (CardView) root.findViewById(R.id.card_water_1);
        cardWater1.setOnClickListener(onClick);
        cardWater1.setOnLongClickListener(onLongClick);

        CardView cardWater2 = (CardView) root.findViewById(R.id.card_water_2);
        cardWater2.setOnClickListener(onClick);
        cardWater2.setOnLongClickListener(onLongClick);

        CardView cardWater3 = (CardView) root.findViewById(R.id.card_water_3);
        cardWater3.setOnClickListener(onClick);
        cardWater3.setOnLongClickListener(onLongClick);

        CardView cardWater4 = (CardView) root.findViewById(R.id.card_water_4);
        cardWater4.setOnClickListener(onClick);
        cardWater4.setOnLongClickListener(onLongClick);

        txtTime = (TextView) root.findViewById(R.id.ed_time_water);
        txtTime.setText(timeFormatter.format(calendar.getTime()));
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(getActivity(), t,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar_dialog);
        toolbar.setTitle(getResources().getString(R.string.dialog_title_water));

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }
        setHasOptionsMenu(true);

        Cursor cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        time = cursor.getLong(0);
        cursor.close();

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(time);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hour, minute);
        time = calendar.getTimeInMillis();


        InputMethodManager im = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        return root;
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.card_water_1:
                    AppContext.getDbDiary().addWater(time, Integer.parseInt(txtWater1.getText().toString()));
                    hideKeyboard();
                    getTargetFragment().onActivityResult(REQ_WATER, Activity.RESULT_OK, null);
                    dismiss();
                    break;
                case R.id.card_water_2:
                    AppContext.getDbDiary().addWater(time, Integer.parseInt(txtWater2.getText().toString()));
                    hideKeyboard();
                    getTargetFragment().onActivityResult(REQ_WATER, Activity.RESULT_OK, null);
                    dismiss();
                    break;
                case R.id.card_water_3:
                    AppContext.getDbDiary().addWater(time, Integer.parseInt(txtWater3.getText().toString()));
                    hideKeyboard();
                    getTargetFragment().onActivityResult(REQ_WATER, Activity.RESULT_OK, null);
                    dismiss();
                    break;
                case R.id.card_water_4:
                    AppContext.getDbDiary().addWater(time, Integer.parseInt(txtWater4.getText().toString()));
                    hideKeyboard();
                    getTargetFragment().onActivityResult(REQ_WATER, Activity.RESULT_OK, null);
                    dismiss();
                    break;
            }
        }
    };

    View.OnLongClickListener onLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.card_water_1:
                    cardId = 0;
                    showDialog();
                    break;
                case R.id.card_water_2:
                    cardId = 1;
                    showDialog();
                    break;
                case R.id.card_water_3:
                    cardId = 2;
                    showDialog();
                    break;
                case R.id.card_water_4:
                    cardId = 3;
                    showDialog();
                    break;
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        waterValuesInit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_dialog_water, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                addWater();
                break;
            case android.R.id.home:
                hideKeyboard();
                dismiss();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addWater() {
        if (edWater.getText().toString().matches("") || edWater.getText().toString().startsWith("0")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.message_dialog_water),
                    Toast.LENGTH_SHORT).show();
        } else {
            AppContext.getDbDiary().addWater(time, Integer.parseInt(edWater.getText().toString()));
            hideKeyboard();
            getTargetFragment().onActivityResult(REQ_WATER, Activity.RESULT_OK, null);
            dismiss();
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void waterValuesInit() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        String[] consts = {
                PREF_WATER_SERVING_1,
                PREF_WATER_SERVING_2,
                PREF_WATER_SERVING_3,
                PREF_WATER_SERVING_4,
        };

        TextView[] txtViews = {txtWater1, txtWater2, txtWater3, txtWater4};

        for (int i = 0; i < consts.length; i++) {
            int value = pref.getInt(consts[i], 0);
            if (value != 0) {
                txtViews[i].setText(String.valueOf(value));
            }
        }
    }

    private void showDialog() {
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View root = getActivity().getLayoutInflater().inflate(R.layout.dialog_water_serving, null);

        builder.setView(root);
        builder.setTitle(getString(R.string.dialog_title_water_serving));

        final TextInputEditText edWater = (TextInputEditText) root.findViewById(R.id.ed_water_serving);

        builder.setPositiveButton(R.string.dialog_change, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edWater.getText().toString().matches("") || edWater.getText().toString()
                        .startsWith("0")) {
                    Toast.makeText(getActivity(), R.string.message_dialog_water,
                            Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = pref.edit();
                    switch (cardId) {
                        case 0:
                            txtWater1.setText(edWater.getText().toString());
                            editor.putInt(PREF_WATER_SERVING_1,
                                    Integer.parseInt(edWater.getText().toString()));
                            editor.apply();
                            break;
                        case 1:
                            txtWater2.setText(edWater.getText().toString());
                            editor.putInt(PREF_WATER_SERVING_2,
                                    Integer.parseInt(edWater.getText().toString()));
                            editor.apply();
                            break;
                        case 2:
                            txtWater3.setText(edWater.getText().toString());
                            editor.putInt(PREF_WATER_SERVING_3,
                                    Integer.parseInt(edWater.getText().toString()));
                            editor.apply();
                            break;
                        case 3:
                            txtWater4.setText(edWater.getText().toString());
                            editor.putInt(PREF_WATER_SERVING_4,
                                    Integer.parseInt(edWater.getText().toString()));
                            editor.apply();
                            break;
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

}


