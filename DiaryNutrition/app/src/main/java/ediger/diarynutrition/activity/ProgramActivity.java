package ediger.diarynutrition.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ediger.diarynutrition.R;

public class ProgramActivity extends AppCompatActivity implements
        AppCompatSeekBar.OnSeekBarChangeListener {
    private boolean isMacroPerc = true;

    private SharedPreferences pref;

    private TextInputLayout tilCal;
    private EditText cal;
    private TextView calSum;

    private TextView prot;
    private TextView fat;
    private TextView carbo;

    private TextView protPerc;
    private TextView fatPerc;
    private TextView carboPerc;

    private TextView protGram;
    private TextView fatGram;
    private TextView carboGram;

    private TextView unitProt;
    private TextView unitFat;
    private TextView unitCarbo;

    private AppCompatSeekBar sbProtPerc;
    private AppCompatSeekBar sbFatPerc;
    private AppCompatSeekBar sbCarboPerc;

    private AppCompatSeekBar sbProtGram;
    private AppCompatSeekBar sbFatGram;
    private AppCompatSeekBar sbCarboGram;

    private LinearLayout linPerc;
    private LinearLayout linGram;
    private AppCompatSpinner spMacro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        unitProt = (TextView) findViewById(R.id.unit_prot);
        unitFat = (TextView) findViewById(R.id.unit_fat);
        unitCarbo = (TextView) findViewById(R.id.unit_carbo);

        tilCal = (TextInputLayout) findViewById(R.id.til_calories);
        cal = (EditText) findViewById(R.id.ed_cal);
        calSum = (TextView) findViewById(R.id.tv_cal_sum);
        prot = (TextView) findViewById(R.id.tv_prot);
        fat = (TextView) findViewById(R.id.tv_fat);
        carbo = (TextView) findViewById(R.id.tv_carbo);
        protPerc = (TextView) findViewById(R.id.tv_prot_percent);
        fatPerc = (TextView) findViewById(R.id.tv_fat_percent);
        carboPerc = (TextView) findViewById(R.id.tv_carbo_percent);
        protGram = (TextView) findViewById(R.id.tv_prot_gram);
        fatGram = (TextView) findViewById(R.id.tv_fat_gram);
        carboGram = (TextView) findViewById(R.id.tv_carbo_gram);
        sbProtPerc = (AppCompatSeekBar) findViewById(R.id.sb_prot_percent);
        sbFatPerc = (AppCompatSeekBar) findViewById(R.id.sb_fat_percent);
        sbCarboPerc = (AppCompatSeekBar) findViewById(R.id.sb_carbo_percent);
        sbProtGram = (AppCompatSeekBar) findViewById(R.id.sb_prot_gram);
        sbFatGram = (AppCompatSeekBar) findViewById(R.id.sb_fat_gram);
        sbCarboGram = (AppCompatSeekBar) findViewById(R.id.sb_carbo_gram);
        linPerc = (LinearLayout) findViewById(R.id.percent_layout);
        linGram = (LinearLayout) findViewById(R.id.gram_layout);
        spMacro = (AppCompatSpinner) findViewById(R.id.sp_macro);

        sbProtPerc.setOnSeekBarChangeListener(this);
        sbFatPerc.setOnSeekBarChangeListener(this);
        sbCarboPerc.setOnSeekBarChangeListener(this);
        sbProtGram.setOnSeekBarChangeListener(this);
        sbFatGram.setOnSeekBarChangeListener(this);
        sbCarboGram.setOnSeekBarChangeListener(this);

        cal.addTextChangedListener(calWatcher);

        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.program_macro_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMacro.setAdapter(adapter);
        spMacro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    linPerc.setVisibility(View.VISIBLE);
                    linGram.setVisibility(View.GONE);
                    tilCal.setEnabled(true);

                    calSum.setVisibility(View.VISIBLE);
                    Animation animation = new TranslateAnimation(0, 0, 300, 0);
                    animation.setDuration(1000);
                    animation.setFillAfter(true);
                    calSum.startAnimation(animation);

                    cal.setEnabled(true);
                    isMacroPerc = true;

                    setDefaultValues();

                    unitProt.setText(getString(R.string.elv_gram));
                    unitFat.setText(getString(R.string.elv_gram));
                    unitCarbo.setText(getString(R.string.elv_gram));

                } else if (position == 1) {
                    linPerc.setVisibility(View.GONE);
                    linGram.setVisibility(View.VISIBLE);
                    tilCal.setEnabled(false);

                    Animation animation = new TranslateAnimation(0, 0, 0, 300);
                    animation.setDuration(1000);
                    animation.setFillAfter(true);
                    calSum.startAnimation(animation);
                    calSum.setVisibility(View.INVISIBLE);

                    cal.setEnabled(false);
                    isMacroPerc = false;

                    setDefaultValues();

                    prot.setText(String.valueOf(pref.getInt("prot_pers", 1)));
                    fat.setText(String.valueOf(pref.getInt("fat_pers", 1)));
                    carbo.setText(String.valueOf(pref.getInt("carbo_pers", 1)));

                    unitProt.setText(getString(R.string.percent));
                    unitFat.setText(getString(R.string.percent));
                    unitCarbo.setText(getString(R.string.percent));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cal.getText().toString().equals("") || cal.getText().toString().equals("0")) {
                    Snackbar.make(v, R.string.message_dialog_cal, Snackbar.LENGTH_SHORT).show();
                } else if (Integer.parseInt(calSum.getText().toString()) != 100) {
                    Snackbar.make(v, R.string.message_program_perc, Snackbar.LENGTH_SHORT).show();
                } else {
                    if (isMacroPerc) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("calories", Integer.parseInt(cal.getText().toString()));
                        editor.putInt("prot", Integer.parseInt(prot.getText().toString()));
                        editor.putInt("fat", Integer.parseInt(fat.getText().toString()));
                        editor.putInt("carbo", Integer.parseInt(carbo.getText().toString()));
                        editor.putInt("prot_pers", Integer.parseInt(protPerc.getText().toString()));
                        editor.putInt("fat_pers", Integer.parseInt(fatPerc.getText().toString()));
                        editor.putInt("carbo_pers", Integer.parseInt(carboPerc.getText().toString()));
                        editor.apply();
                    } else {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("calories", Integer.parseInt(cal.getText().toString()));
                        editor.putInt("prot", Integer.parseInt(protGram.getText().toString()));
                        editor.putInt("fat", Integer.parseInt(fatGram.getText().toString()));
                        editor.putInt("carbo", Integer.parseInt(carboGram.getText().toString()));
                        editor.putInt("prot_pers", Integer.parseInt(prot.getText().toString()));
                        editor.putInt("fat_pers", Integer.parseInt(fat.getText().toString()));
                        editor.putInt("carbo_pers", Integer.parseInt(carbo.getText().toString()));
                        editor.apply();
                    }
                    finish();
                }
            }
        });

        setDefaultValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher calWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            setPerc();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void setMacro() {
        float cal;
        float protPerc;
        float fatPerc;
        float carboPerc;

        float prot = Float.parseFloat(this.protGram.getText().toString());
        float fat = Float.parseFloat(this.fatGram.getText().toString());
        float carbo = Float.parseFloat(this.carboGram.getText().toString());


        cal = (prot * 4) + (fat * 9) + (carbo * 4);
        this.cal.setText(String.valueOf((int) cal));

        if (cal == 0) {cal = 1;}

        protPerc = prot * 4 / cal * 100;
        fatPerc = fat * 9 / cal * 100;
        carboPerc = carbo * 4 / cal * 100;

        if(Math.round(protPerc) + Math.round(fatPerc) + Math.round(carboPerc) != 100) {
            carboPerc++;
        }

        //header
        this.prot.setText(String.valueOf(Math.round(protPerc)));
        this.fat.setText(String.valueOf(Math.round(fatPerc)));
        this.carbo.setText(String.valueOf(Math.round(carboPerc)));
    }

    private void setPerc() {
        float prot;
        float fat;
        float carbo;
        float cal;

        if (this.cal.getText().toString().equals("")) {
            cal = 1;
        } else {
            cal = Float.parseFloat(this.cal.getText().toString());
        }

        prot = Float.parseFloat(protPerc.getText().toString()) / 100
                * cal / 4;
        this.prot.setText(String.valueOf(Math.round(prot)));

        fat = Float.parseFloat(fatPerc.getText().toString()) / 100
                * cal / 9;
        this.fat.setText(String.valueOf(Math.round(fat)));

        carbo = Float.parseFloat(carboPerc.getText().toString()) / 100
                * cal / 4;
        this.carbo.setText(String.valueOf(Math.round(carbo)));
    }

    private void setDefaultValues() {
        int prot;
        int fat;
        int carbo;

        int cal = pref.getInt("calories", 1);
        this.cal.setText(String.valueOf(cal));

        //Проценты
        prot = pref.getInt("prot_pers", 1);
        fat = pref.getInt("fat_pers", 1);
        carbo = pref.getInt("carbo_pers", 1);

        sbProtPerc.setProgress(prot);
        sbFatPerc.setProgress(fat);
        sbCarboPerc.setProgress(carbo);

        protPerc.setText(String.valueOf(prot));
        fatPerc.setText(String.valueOf(fat));
        carboPerc.setText(String.valueOf(carbo));

        //Граммы
        prot = pref.getInt("prot", 1);
        fat = pref.getInt("fat", 1);
        carbo = pref.getInt("carbo", 1);

        if (isMacroPerc) {
            this.prot.setText(String.valueOf(prot));
            this.fat.setText(String.valueOf(fat));
            this.carbo.setText(String.valueOf(carbo));
        }

        sbProtGram.setProgress(prot);
        sbFatGram.setProgress(fat);
        sbCarboGram.setProgress(carbo);

        protGram.setText(String.valueOf(prot));
        fatGram.setText(String.valueOf(fat));
        carboGram.setText(String.valueOf(carbo));

        setCalSum();
    }

    private void setCalSum() {
        int sum = Integer.parseInt(protPerc.getText().toString())
                + Integer.parseInt(fatPerc.getText().toString())
                + Integer.parseInt(carboPerc.getText().toString());
        calSum.setText(String.valueOf(sum));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_prot_gram:
                protGram.setText(String.valueOf(progress));
                setMacro();
                break;
            case R.id.sb_fat_gram:
                fatGram.setText(String.valueOf(progress));
                setMacro();
                break;
            case R.id.sb_carbo_gram:
                carboGram.setText(String.valueOf(progress));
                setMacro();
                break;
            case R.id.sb_prot_percent:
                progress = (Math.round(progress / 5)) * 5;
                seekBar.setProgress(progress);
                protPerc.setText(String.valueOf(progress));
                setPerc();
                setCalSum();
                break;
            case R.id.sb_fat_percent:
                progress = (Math.round(progress / 5)) * 5;
                seekBar.setProgress(progress);
                fatPerc.setText(String.valueOf(progress));
                setPerc();
                setCalSum();
                break;
            case R.id.sb_carbo_percent:
                progress = (Math.round(progress / 5)) * 5;
                seekBar.setProgress(progress);
                carboPerc.setText(String.valueOf(progress));
                setPerc();
                setCalSum();
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
