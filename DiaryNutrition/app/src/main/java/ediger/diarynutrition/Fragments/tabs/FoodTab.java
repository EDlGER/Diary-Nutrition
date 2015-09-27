package ediger.diarynutrition.Fragments.tabs;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
/*import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;*/
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.concurrent.TimeUnit;

import ediger.diarynutrition.AddFoodDialog;
import ediger.diarynutrition.Fragments.ChangeFoodDialog;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.FoodAdapter;
import ediger.diarynutrition.adapters.RecordAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 05.09.15.
 */
public class FoodTab extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{
    View rootview;
    private ListView listFood;
    private Cursor cursor;
    private FoodAdapter foodAdapter;
    private EditText txtSearch;
    private Button btnAdd;

    String[] from;
    int[] to = {
            R.id.txt_f_name,
            R.id.txt_f_cal,
            R.id.txt_f_carbo,
            R.id.txt_f_prot,
            R.id.txt_f_fat
    };
    private static final int REQ_CODE_CHANGE = 1;
    private static final int REQ_CODE_ADD = 2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == 2){
                cursor = AppContext.getDbDiary().getUserFood();
                from = AppContext.getDbDiary().getListFood();
                foodAdapter = new FoodAdapter(getActivity(), R.layout.food_item1, cursor, from, to, 0);
                listFood.setAdapter(foodAdapter);
                listFood.setTextFilterEnabled(true);
                foodAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                        public Cursor runQuery(CharSequence constraint) {
                        return getFilterList(constraint);
                    }
                });
                getLoaderManager().getLoader(1).forceLoad();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.food_tab, container, false);

        txtSearch = (EditText) rootview.findViewById(R.id.fl_txtSearch);
        btnAdd = (Button) rootview.findViewById(R.id.fl_b_add);

        //Данные для адаптера
        cursor = AppContext.getDbDiary().getUserFood();
        from = AppContext.getDbDiary().getListFood();
        foodAdapter = new FoodAdapter(getActivity(), R.layout.food_item1, cursor, from, to, 0);

        listFood = (ListView) rootview.findViewById(R.id.fl_listFood);
        listFood.setAdapter(foodAdapter);
        listFood.setTextFilterEnabled(true);
        registerForContextMenu(listFood);
        //Поиск
        foodAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getFilterList(constraint);
            }
        });
        txtSearch.addTextChangedListener(TextChangedListener);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment a = new AddFoodDialog();
                a.setTargetFragment(FoodTab.this,REQ_CODE_ADD);
                a.show(getFragmentManager(),"add_dialog");
                getLoaderManager().getLoader(1).forceLoad();
            }
        });
        getLoaderManager().initLoader(1, null,this);
        return rootview;
    }

    //Поиск по введенным буквам
    public Cursor getFilterList(CharSequence constraint) {
        String[] asColumnsToResult = AppContext.getDbDiary().getFilterFood();
        String selections = "usr > 0";

        if(constraint == null || constraint.length() == 0){
            return AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections, null, null,
                    null, null);
        }
        else {
            String value = "%"+constraint.toString()+"%";
            return AppContext.getDbDiary().getDb().query("food",asColumnsToResult,"usr > 0 AND food_name like ? ",
                    new String[]{value},null,null,null);
        }
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,1,0,R.string.context_menu_change);
        menu.add(0,2,0,R.string.context_menu_del);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            DialogFragment c = new ChangeFoodDialog();
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            Bundle args = new Bundle();
            args.putLong("id", acmi.id);
            c.setArguments(args);
            c.setTargetFragment(FoodTab.this,REQ_CODE_CHANGE);
            c.show(getFragmentManager(),"change_dialog");
            getLoaderManager().getLoader(1).forceLoad();
            return true;
        }
        if(item.getItemId() == 2){
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            AppContext.getDbDiary().delFood(acmi.id);
            getLoaderManager().getLoader(1).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(),AppContext.getDbDiary());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        foodAdapter.swapCursor(data);
        listFood.setAdapter(foodAdapter);
        listFood.setTextFilterEnabled(true);
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
            Cursor cursor = db.getUserFood();
            /*try{
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e){
                e.printStackTrace();
            }*/
            return cursor;
        }
    }
}
