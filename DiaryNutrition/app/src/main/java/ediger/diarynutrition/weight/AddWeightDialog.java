package ediger.diarynutrition.weight;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Weight;

public class AddWeightDialog extends DialogFragment {

    public static final String ARG_DATE = "date";

    private WeightViewModel mViewModel;
    private TextInputEditText mEdWeight;
    private TextInputEditText mEdDate;

    private Calendar date = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WeightViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        View root = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_weight, null);

        builder.setView(root);
        builder.setTitle(getString(R.string.dialog_title_w));

        mEdWeight = root.findViewById(R.id.ed_weight);
        mEdDate = root.findViewById(R.id.ed_date);

        builder.setPositiveButton(R.string.dialog_add, null);
        builder.setNegativeButton(R.string.dialog_cancel_f, (dialog, id) -> dialog.dismiss());
        builder.setCancelable(false);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {

            long dateMillis = getArguments().getLong(ARG_DATE, 0);
            if (dateMillis != 0) date.setTimeInMillis(dateMillis);
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            mEdDate.setText(dateFormat.format(date.getTime()));
            mEdDate.setOnClickListener(v ->
                    new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) -> {
                        date.set(year,month,dayOfMonth);

                        mEdDate.setText(dateFormat.format(date.getTime())); },
                        date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
                    .show());

            mEdWeight.requestFocus();

            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (mEdWeight.getText().toString().matches("")
                        || mEdWeight.getText().toString().startsWith("0")) {
                    Toast.makeText(getActivity(), getString(R.string.message_dialog_weight),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mViewModel.addWeight(
                            new Weight(Float.parseFloat(mEdWeight.getText().toString()),
                                    date.getTimeInMillis()));
                    dialog.dismiss();
                }
            }) ;
        }
    }

}
