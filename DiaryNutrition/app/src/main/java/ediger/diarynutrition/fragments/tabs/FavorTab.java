package ediger.diarynutrition.fragments.tabs;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
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


import ediger.diarynutrition.activity.AddActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.FoodAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.dialogs.AddFoodDialog;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 05.09.15.
 */
public class FavorTab extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQ_CODE_ADD_FOOD = 2;
    private static final int LOADER_ID = -4;

    View rootview;

    public ListView listFood;
    private int[] to = {
            R.id.txt_f_name,
            R.id.txt_f_cal,
            R.id.txt_f_carbo,
            R.id.txt_f_prot,
            R.id.txt_f_fat
    };
    private long addid;
    private String[] from;
    private Cursor cursor;
    private FoodAdapter foodAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_tab_favor, container, false);

        setHasOptionsMenu(true);

        cursor = AppContext.getDbDiary().getFavorFood();
        from = AppContext.getDbDiary().getListFood();
        foodAdapter = new FoodAdapter(getActivity(), R.layout.food_item1, cursor, from, to, 0);

        listFood = (ListView) rootview.findViewById(R.id.ft_listFood);
        listFood.setEmptyView(rootview.findViewById(R.id.empty_ft_list));

        listFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addid = id;

                Intent addIntent = new Intent(getActivity(), AddActivity.class);
                addIntent.putExtra("FoodId", addid);
                startActivity(addIntent);
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

        cursor = AppContext.getDbDiary().getFavorFood();
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

    //Поиск по введенным буквам
    public Cursor getFilterList(CharSequence constraint) {
        String[] asColumnsToResult = AppContext.getDbDiary().getFilterFood();
        String selections = "favor = 1 AND usr > -1";
        String orderBy = "food_name asc";

        if(constraint == null || constraint.length() == 0){
            return AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections,
                    null, null, null, orderBy);
        }
        else {
            String value = "%"+constraint.toString()+"%";
            return AppContext.getDbDiary().getDb().query("food",asColumnsToResult,
                    "favor = 1 AND usr > -1 AND food_name like ? ",
                    new String[]{value},null,null,orderBy);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 4, 0, R.string.context_menu_del);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 4) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            AppContext.getDbDiary().setFavor(acmi.id,0);
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
            case R.id.action_add:
                DialogFragment a = new AddFoodDialog();
                a.setTargetFragment(FavorTab.this, REQ_CODE_ADD_FOOD);
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
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
            Cursor cursor = db.getFavorFood();
            return cursor;
        }
    }
}