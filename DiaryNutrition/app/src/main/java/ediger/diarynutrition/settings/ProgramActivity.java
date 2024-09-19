package ediger.diarynutrition.settings;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import android.view.MenuItem;

import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.ActivityProgramBinding;

public class ProgramActivity extends AppCompatActivity {

    private ActivityProgramBinding mBinding;

    private ProgramViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_program);

        mViewModel = new ViewModelProvider(this).get(ProgramViewModel.class);
        mBinding.setLifecycleOwner(this);
        mBinding.setViewModel(mViewModel);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.fab.setOnClickListener((v) -> {
            if (mBinding.edCal.getText().toString().equals("")) {
                Snackbar.make(v, R.string.message_dialog_cal, Snackbar.LENGTH_SHORT).show();
            } else if (mViewModel.getPercentSum() != 100) {
                Snackbar.make(v, R.string.message_program_perc, Snackbar.LENGTH_SHORT).show();
            } else {
                mViewModel.onFabClick();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
