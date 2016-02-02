package ediger.diarynutrition.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.FoodAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

public class AddActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final int DIALOG = 1;
    private final int DIALOG_ADD = 2;
    private ListView listFood;
    private Cursor cursor;
    private FoodAdapter foodAdapter;
    private Calendar now = Calendar.getInstance();
    private int nowHour = now.get(Calendar.HOUR_OF_DAY);
    private int nowMin = now.get(Calendar.MINUTE);
    private EditText servPicker;
    private NumberPicker hourPicker;
    private NumberPicker minPicker;
    private Spinner mealPicker;
    private long addtime;
    private long addid;
    private int addserv;


    private EditText txtNameF;
    private EditText txtCalF;
    private EditText txtCarboF;
    private EditText txtProtF;
    private EditText txtFatF;

    Calendar date = Calendar.getInstance();
    long cal;
    String[] from;
    int[] to = {
            R.id.txt_f_name,
            R.id.txt_f_cal,
            R.id.txt_f_carbo,
            R.id.txt_f_prot,
            R.id.txt_f_fat
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ADD);
            }
        });

        Intent intent = getIntent();
        cal = intent.getLongExtra("CurrentCal",date.getTimeInMillis());


        //Данные для адаптера
        cursor = AppContext.getDbDiary().getAllFood();
        from = AppContext.getDbDiary().getListFood();

        foodAdapter = new FoodAdapter(this, R.layout.food_item1, cursor, from, to, 0);

        listFood = (ListView) findViewById(R.id.listFood);
        listFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addid = id;
                showDialog(DIALOG);
            }
        });
        listFood.setTextFilterEnabled(true);
        listFood.setAdapter(foodAdapter);
        //Поиск
        foodAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getFilterList(constraint);
            }
        });

        getSupportLoaderManager().initLoader(-2, null, this);
    }


    //Поиск по введенным буквам
    public Cursor getFilterList(CharSequence constraint) {
        String[] asColumnsToResult = AppContext.getDbDiary().getFilterFood();
        String selections = "usr > -1";

        if(constraint == null || constraint.length() == 0){
            return AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections, null, null,
                    null, null);
        }
        else {
            String value = "%"+constraint.toString()+"%";
            return AppContext.getDbDiary().getDb().query("food",asColumnsToResult,"usr > -1 AND food_name like ? ",
                    new String[]{value},null,null,null);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cursor = AppContext.getDbDiary().getAllFood();
        foodAdapter = new FoodAdapter(this, R.layout.food_item1, cursor, from, to, 0);
        listFood.setAdapter(foodAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_add_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)listFood.getAdapter();
                filterAdapter.getFilter().filter(newText.toString());
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.home:{
                finish();
                return true;
            }
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher TextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)listFood.getAdapter();
            filterAdapter.getFilter().filter(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    //Диалоги
    @Override
    protected Dialog onCreateDialog(int id){
        switch (id){
            case DIALOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);

                View relative = getLayoutInflater().inflate(R.layout.add_dialog, null);
                builder.setView(relative);
                //builder.setTitle(R.string.dialog_title);
                servPicker = (EditText) relative.findViewById(R.id.txtAddServ);
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

                SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this,
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
                                if (servPicker.getText().toString().matches("")){
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Значение порции не введено",Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                else {
                                    date.setTimeInMillis(cal);
                                    addserv = Integer.parseInt(servPicker.getText().toString());
                                    date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                                            date.get(Calendar.DAY_OF_MONTH),
                                            hourPicker.getValue(), minPicker.getValue());
                                    addtime = date.getTimeInMillis();

                                    int mealId = mealPicker.getSelectedItemPosition();
                                    AppContext.getDbDiary().addRec(addid,addserv,addtime,mealId+1);
                                    finish();
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

            case DIALOG_ADD:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AddActivity.this);

                View relative1 = getLayoutInflater().inflate(R.layout.add_f_dialog, null);
                builder1.setView(relative1);
                builder1.setTitle(R.string.dialog_tittle_f);

                txtNameF = (EditText) relative1.findViewById(R.id.txtNameF);
                txtCalF = (EditText) relative1.findViewById(R.id.txtCalF);
                txtCarboF = (EditText) relative1.findViewById(R.id.txtCarboF);
                txtProtF = (EditText) relative1.findViewById(R.id.txtProtF);
                txtFatF = (EditText) relative1.findViewById(R.id.txtFatF);

                txtNameF.setText("");
                txtCalF.setText("");
                txtCarboF.setText("");
                txtProtF.setText("");
                txtFatF.setText("");

                builder1.setPositiveButton(R.string.dialog_add_f,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (txtNameF.getText().toString().matches("") ||
                                        txtCalF.getText().toString().matches("") ||
                                        txtCarboF.getText().toString().matches("") ||
                                        txtProtF.getText().toString().matches("") ||
                                        txtFatF.getText().toString().matches("") ){
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Не все поля заполнены",Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                else {
                                    AppContext.getDbDiary().addFood(txtNameF.getText().toString(),
                                            Float.parseFloat(txtCalF.getText().toString()),
                                            Float.parseFloat(txtCarboF.getText().toString()),
                                            Float.parseFloat(txtProtF.getText().toString()),
                                            Float.parseFloat(txtFatF.getText().toString()));
                                    onRestart();
                                }
                            }
                        });
                builder1.setNegativeButton(R.string.dialog_cancel_f,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder1.setCancelable(true);
                return builder1.create();

            default: return null;

        }
    }
    //Обновление данных
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this,AppContext.getDbDiary());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        foodAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class MyCursorLoader extends CursorLoader {
        DbDiary db;

        public MyCursorLoader(Context context, DbDiary db) {
            super(context);
            this.db = db;
        }

        @Override
        public  Cursor loadInBackground(){
            Cursor cursor = db.getAllFood();
            try{
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            return cursor;
        }
    }
}
