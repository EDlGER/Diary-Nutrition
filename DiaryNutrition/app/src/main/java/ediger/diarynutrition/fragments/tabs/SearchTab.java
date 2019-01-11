package ediger.diarynutrition.fragments.tabs;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
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
import ediger.diarynutrition.fragments.dialogs.AddFoodDialog;
import ediger.diarynutrition.objects.AppContext;

public class SearchTab extends Fragment {

    View rootview;

    private static final int REQ_CODE_ADD_FOOD = 2;

    private int[] to = {
            R.id.txt_f_name,
            R.id.txt_f_cal,
            R.id.txt_f_carbo,
            R.id.txt_f_prot,
            R.id.txt_f_fat
    };

    private long addid;
    private ListView listFood;;
    private FoodAdapter foodAdapter;
    private Cursor cursor;
    private String[] from;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_tab_search, container, false);

        setHasOptionsMenu(true);

        //Данные для адаптера
        cursor = AppContext.getDbDiary().getAllFood();
        from = AppContext.getDbDiary().getListFood();

        foodAdapter = new FoodAdapter(getActivity(), R.layout.food_item1, cursor, from, to, 0);

        listFood = (ListView) rootview.findViewById(R.id.st_listFood);
        listFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addid = id;

                Intent addIntent = new Intent(getActivity(), AddActivity.class);
                addIntent.putExtra("FoodId", addid);
                startActivity(addIntent);
            }
        });
        listFood.setTextFilterEnabled(true);
        registerForContextMenu(listFood);
        //Поиск
        foodAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getFilterList(constraint);
            }
        });

        return rootview;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 3, 0, R.string.context_menu_favor);
        menu.add(0, 4 ,0 , R.string.context_menu_del);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        if (item.getItemId() == 3) {
            AppContext.getDbDiary().setFavor(acmi.id, 1);
            Snackbar snackbar = Snackbar
                    .make(rootview, getString(R.string.message_favorite), Snackbar.LENGTH_LONG);
            snackbar.show();

            return true;
        }

        if (item.getItemId() == 4) {
            AppContext.getDbDiary().delFood(acmi.id);
            Snackbar snackbar = Snackbar
                    .make(rootview, getString(R.string.message_food_del),
                            Snackbar.LENGTH_LONG);
            snackbar.show();

            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.action_add:
                DialogFragment a = new AddFoodDialog();
                a.setTargetFragment(SearchTab.this, REQ_CODE_ADD_FOOD);
                a.show(getFragmentManager(), "add_dialog");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo (searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);

        searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listFood.setAdapter(foodAdapter);
                SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter) listFood.getAdapter();
                filterAdapter.getFilter().filter(newText.toString());
                return true;
            }
        });
    }

    //Поиск по введенным буквам
    private Cursor getFilterList(CharSequence constraint) {
        String[] asColumnsToResult = AppContext.getDbDiary().getFilterFood();
        String selections = "usr = -3";
        //String orderBy = "food_name asc";

        if(constraint == null || constraint.length() == 0) {
            return AppContext.getDbDiary().getDb().query("food", asColumnsToResult, selections, null,
                    null, null, null);
        }
        else {
            String value = "%" +constraint.toString() + "%";
            String orderBy = "food_name = \"" + constraint.toString() + "\" desc, food_name LIKE \"" +
                    constraint.toString() + "%\" desc";
            return AppContext.getDbDiary().getDb().query("food",asColumnsToResult,"usr > -1 AND food_name LIKE ? ",
                    new String[]{value}, null, null, orderBy);
        }
    }
}
