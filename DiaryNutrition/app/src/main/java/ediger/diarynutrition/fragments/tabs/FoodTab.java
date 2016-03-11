package ediger.diarynutrition.fragments.tabs;

import android.app.Activity;
import android.app.SearchManager;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import ediger.diarynutrition.FoodActivity;
import ediger.diarynutrition.fragments.dialogs.AddDialog;
import ediger.diarynutrition.fragments.dialogs.AddFoodDialog;
import ediger.diarynutrition.fragments.dialogs.ChangeFoodDialog;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.FoodAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 05.09.15.
 */
public class FoodTab extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = -3;
    private static final int REQ_CODE_ADD = 1;
    private static final int REQ_CODE_ADD_FOOD = 2;
    private static final int REQ_CODE_CHANGE = 3;

    View rootview;
    public ListView listFood;
    private Cursor cursor;
    private FoodAdapter foodAdapter;
    private long addid;

    String[] from;
    int[] to = {
            R.id.txt_f_name,
            R.id.txt_f_cal,
            R.id.txt_f_carbo,
            R.id.txt_f_prot,
            R.id.txt_f_fat
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            FoodActivity foodActivity = (FoodActivity)getActivity();
            foodActivity.finish();
        } else if (requestCode == 2 || requestCode == 3){
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
                getLoaderManager().getLoader(LOADER_ID).forceLoad();
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();

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
        getLoaderManager().getLoader(LOADER_ID).forceLoad();
        //getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_tab_food, container, false);

        setHasOptionsMenu(true);

        //Данные для адаптера
        cursor = AppContext.getDbDiary().getUserFood();
        from = AppContext.getDbDiary().getListFood();
        foodAdapter = new FoodAdapter(getActivity(), R.layout.food_item1, cursor, from, to, 0);


        listFood = (ListView) rootview.findViewById(R.id.ft_listFood);

        listFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addid = id;
                Bundle bundle = new Bundle();
                bundle.putLong("id", addid);

                DialogFragment a = new AddDialog();
                a.setTargetFragment(FoodTab.this, REQ_CODE_ADD);
                a.setArguments(bundle);
                a.show(getFragmentManager(), "add_dialog");
            }
        });

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

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootview;
    }

    //Поиск по введенным буквам
    public Cursor getFilterList(CharSequence constraint) {
        String[] asColumnsToResult = AppContext.getDbDiary().getFilterFood();
        String selections = "usr > 0";

        if(constraint == null || constraint.length() == 0){
            return AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections,
                    null, null, null, null);
        }
        else {
            String value = "%"+constraint.toString()+"%";
            return AppContext.getDbDiary().getDb().query("food",asColumnsToResult,
                    "usr > 0 AND food_name like ? ",
                    new String[]{value},null,null,null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.action_add:
                DialogFragment a = new AddFoodDialog();
                a.setTargetFragment(FoodTab.this, REQ_CODE_ADD_FOOD);
                a.show(getFragmentManager(), "add_dialog");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.activity_add, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
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
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.context_menu_change);
        menu.add(0, 2, 0, R.string.context_menu_del);
        menu.add(0, 3, 0, R.string.context_menu_favor);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 1) {
            DialogFragment c = new ChangeFoodDialog();
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            Bundle args = new Bundle();
            args.putLong("id", acmi.id);
            c.setArguments(args);
            c.setTargetFragment(FoodTab.this,REQ_CODE_CHANGE);
            c.show(getFragmentManager(),"change_dialog");
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            return true;
        }
        if(item.getItemId() == 2) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            AppContext.getDbDiary().delFood(acmi.id);
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            return true;
        }
        if (item.getItemId() == 3) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();

            AppContext.getDbDiary().setFavor(acmi.id,1);
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
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
            return cursor;
        }
    }
}
