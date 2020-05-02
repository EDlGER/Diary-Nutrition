package ediger.diarynutrition.diary;


import android.animation.AnimatorInflater;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.databinding.FragmentDiaryBinding;
import ediger.diarynutrition.diary.adapters.RecordAdapter;
import ediger.diarynutrition.diary.adapters.WaterFooterAdapter;
import ediger.diarynutrition.diary.water.AddWaterDialog;
import ediger.diarynutrition.diary.water.WaterActivity;
import ediger.diarynutrition.objects.SnackbarMessage;
import ediger.diarynutrition.util.SnackbarUtils;
import ediger.diarynutrition.weight.AddWeightDialog;

import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;


public class DiaryFragment extends Fragment implements
        RecyclerViewExpandableItemManager.OnGroupExpandListener {

    private FragmentDiaryBinding mBinding;

    private DiaryViewModel mViewModel;

    private RecordAdapter mRecordAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    private Calendar mSelectedDate = Calendar.getInstance();

    private List<Record> mChildBuf = new ArrayList<>();

    private ActionBar mActionBar;

    //Calendar
    private float mCurrentRotation = 360.0f;
    private boolean isCalendarExpanded = false;

    //Header
    private boolean isRemaining = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_diary, container, false);

        setHasOptionsMenu(true);

        mBinding.setIsRemaining(isRemaining);

        setupActionBar();

        setupHeaderSwitcher();

        setupList(savedInstanceState);

        setupFab();

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Recycler
        mViewModel.getRecords().observe(getViewLifecycleOwner(), records -> mRecordAdapter.setRecordList(records));

        //Header
        mViewModel.getDaySummary().observe(getViewLifecycleOwner(), summary -> mBinding.setActual(summary));

        //Snackbar
        mViewModel.getSnackbarMessage().observe(this,
                (SnackbarMessage.SnackbarObserver) snackbarMessageResourceId ->
                        SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId)));
    }

    private void setupActionBar() {
        mActionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (mActionBar == null) {
            return;
        }
        mActionBar.setCustomView(R.layout.toolbar_subtitle);
        View customToolbar = mActionBar.getCustomView();
        final ImageView arrow = customToolbar.findViewById(R.id.img_arrow);
        final TextView subtitle = customToolbar.findViewById(R.id.txt_subtitle);
        final TextView title = customToolbar.findViewById(R.id.txt_title);
        title.setText(getString(R.string.title_diary));

        final RelativeLayout datePickerButton = customToolbar.findViewById(R.id.date_picker_button);

        setupCalendar(subtitle, datePickerButton);

        datePickerButton.setOnClickListener(v -> {
            if (!mBinding.calendarView.isAnimating()) {
                if (isCalendarExpanded) {
                    RotateAnimation anim = new RotateAnimation(mCurrentRotation,
                            mCurrentRotation + 180.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);

                    mCurrentRotation = (mCurrentRotation + 180.0f) % 360.0f;
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    anim.setDuration(300);
                    arrow.startAnimation(anim);
                    mBinding.calendarView.hideCalendar();
                    isCalendarExpanded = false;
                } else {
                    RotateAnimation anim = new RotateAnimation(mCurrentRotation,
                            mCurrentRotation - 180.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);

                    mCurrentRotation = (mCurrentRotation - 180.0f) % 360.0f;
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    anim.setDuration(300);
                    arrow.startAnimation(anim);
                    mBinding.calendarView.showCalendar();
                    isCalendarExpanded = true;
                }
            }
        });
    }

    private void setupCalendar(TextView subtitle, RelativeLayout dateButton) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        final Calendar currentDate = Calendar.getInstance();

        CompactCalendarView calendarView = mBinding.calendarView;
        calendarView.setLocale(TimeZone.getDefault(), Locale.getDefault());
        calendarView.setShouldDrawDaysHeader(true);
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateClicked);

                mViewModel.setDate(calendar);
                dateButton.performClick();

                if (calendar.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) {
                    subtitle.setText(getString(R.string.diary_date_today));
                } else {
                    subtitle.setText(dateFormat.format(dateClicked));
                }
                mSelectedDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                mSelectedDate.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(firstDayOfNewMonth);
                mViewModel.setDate(calendar);

                subtitle.setText(dateFormat.format(firstDayOfNewMonth));

                mSelectedDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                mSelectedDate.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
            }
        });
        calendarView.setCurrentDate(mSelectedDate.getTime());
        subtitle.setText(getString(R.string.diary_date_today));
    }

    private void setupList(Bundle savedInstanceState) {
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(savedInstanceState);
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this);

        mRecordAdapter = new RecordAdapter(mRecyclerViewExpandableItemManager, this);

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mRecordAdapter);

        if (PreferenceHelper.getValue(PreferenceHelper.KEY_PREF_UI_WATER_CARD, Boolean.class, true)) {
            setupWaterFooter();
        }

        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        mBinding.listRecords.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mBinding.listRecords.setLayoutManager(linearLayout);
        mBinding.listRecords.setHasFixedSize(false);

        //Animation
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mBinding.listRecords.setItemAnimator(animator);

        mRecyclerViewExpandableItemManager.attachRecyclerView(mBinding.listRecords);
        registerForContextMenu(mBinding.listRecords);
    }

    private void setupWaterFooter() {
        View.OnClickListener onWaterClickListener = view -> {
            Water water = mViewModel.water.getValue();
            if (water != null && water.getAmount() != 0) {
                int[] location = new int[2];
                mBinding.expansionView.getLocationInWindow(location);
                Intent intent = new Intent(getActivity(), WaterActivity.class);
                intent.putExtra(Consts.ARG_EXPANSION_LEFT_OFFSET, location[0]);
                intent.putExtra(Consts.ARG_EXPANSION_TOP_OFFSET, location[1]);
                intent.putExtra(Consts.ARG_EXPANSION_VIEW_WIDTH, mBinding.expansionView.getWidth());
                intent.putExtra(Consts.ARG_EXPANSION_VIEW_HEIGHT, mBinding.expansionView.getHeight());
                intent.putExtra(WaterActivity.ARG_DATE, mViewModel.getDate().getTimeInMillis());
                startActivity(intent);
            } else {
                SnackbarUtils.showSnackbar(getView(), getString(R.string.message_card_water));
            }
        };

        mWrappedAdapter = new WaterFooterAdapter(mWrappedAdapter, this, mViewModel, onWaterClickListener);
    }

    private void setupHeaderSwitcher() {
        mBinding.txtSwConsume.setFactory(() -> {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextSize(15);
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            return textView;
        });
        Animation inAnimation = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_out);
        mBinding.txtSwConsume.setInAnimation(inAnimation);
        mBinding.txtSwConsume.setOutAnimation(outAnimation);
    }

    private void setupFab() {
        //TODO Temporary code
        mBinding.fabActionFood.setOnClickListener(v -> {
            mViewModel.addRecord(new Record(
                    1, 1, 100, mSelectedDate.getTimeInMillis()));
        });
        mBinding.fabActionWater.setOnClickListener(v -> {
            mBinding.fabMenu.collapseImmediately();
            showWaterDialog();
        });
        mBinding.fabActionWeight.setOnClickListener(v -> {
            mBinding.fabMenu.collapseImmediately();
            showWeightDialog();
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Summary goalSummary = new Summary(
                PreferenceHelper.getValue(PreferenceHelper.KEY_PROGRAM_CAL, Float.class, 1f),
                PreferenceHelper.getValue(PreferenceHelper.KEY_PROGRAM_PROT, Float.class, 1f),
                PreferenceHelper.getValue(PreferenceHelper.KEY_PROGRAM_FAT, Float.class, 1f),
                PreferenceHelper.getValue(PreferenceHelper.KEY_PROGRAM_CARBO, Float.class, 1f));
        mBinding.setGoal(goalSummary);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) Objects.requireNonNull(getActivity())).appBar
                    .setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getActivity(), R.animator.appbar_unelevated_animator)
            );
        }
        if (mActionBar != null) {
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onPause() {
        if (((MainActivity) requireActivity()).navigationView.getMenu().findItem(R.id.nav_diary)
                .isChecked()) {
            super.onPause();
        } else {
            super.onPause();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((MainActivity) Objects.requireNonNull(getActivity())).appBar
                        .setStateListAnimator(AnimatorInflater
                                .loadStateListAnimator(getActivity(), R.animator.appbar_elevated_animator)
                        );
            }

            if (mActionBar != null) {
                mActionBar.setDisplayShowCustomEnabled(false);
                mActionBar.setDisplayShowTitleEnabled(true);
            }
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu,@NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        RecyclerView rv = RecyclerViewAdapterUtils.getParentRecyclerView(v);
        if (rv == null) {
            return;
        }
        RecyclerView.ViewHolder vh = rv.findContainingViewHolder(v);

        int rootPosition = vh.getAdapterPosition();
        if (rootPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.Adapter rootAdapter = rv.getAdapter();
        int localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter, mRecordAdapter, rootPosition);

        long expandablePosition = mRecyclerViewExpandableItemManager.getExpandablePosition(localFlatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
        int childCount = mRecordAdapter.getChildCount(groupPosition);

        if (childPosition == RecyclerView.NO_POSITION) {
            //group
            if (mRecyclerViewExpandableItemManager.isGroupExpanded(groupPosition) && childCount != 0) {
                menu.add(0, 1, 0, R.string.context_menu_copy).setActionView(v);
            }
            menu.add(0, 2, 0, R.string.context_menu_paste).setActionView(v);
        } else {
            //child
            menu.add(0, 3, 0, R.string.context_menu_del).setActionView(v);
            menu.add(0, 4, 0, R.string.context_menu_change).setActionView(v);
        }

        mRecordAdapter.setContextMenuState(true);
    }

    public void onContextMenuClosed() {
        mRecordAdapter.setContextMenuState(false);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        RecyclerView rv = RecyclerViewAdapterUtils.getParentRecyclerView(item.getActionView());
        if (rv == null) {
            return super.onContextItemSelected(item);
        }
        RecyclerView.ViewHolder vh = rv.findContainingViewHolder(item.getActionView());

        int rootPosition = vh.getAdapterPosition();
        if (rootPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        RecyclerView.Adapter rootAdapter = rv.getAdapter();
        int localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter, mRecordAdapter, rootPosition);

        long expandablePosition = mRecyclerViewExpandableItemManager.getExpandablePosition(localFlatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);

        int childCount = mRecordAdapter.getChildCount(groupPosition);

        switch (item.getItemId()) {
            //Meal copy
            case 1:
                for (int i = 0; i < childCount; i++) {
                    mChildBuf.add(mRecordAdapter.getChildRecordData(groupPosition, i));
                }
                SnackbarUtils.showSnackbar(getView(), getString(R.string.message_record_meal_copy));
                break;

            // TODO: It doesn't paste in the other day
            //Meal insert
            case 2:
                if (mChildBuf.size() == 0) {
                    SnackbarUtils.showSnackbar(getView(),
                            getString(R.string.message_record_meal_insert_fail));
                    break;
                }
                for (int i = 0; i < mChildBuf.size(); i++) {
                    Calendar datetime = Calendar.getInstance();
                    datetime.setTimeInMillis(mChildBuf.get(i).getDatetime());
                    datetime.set(Calendar.DAY_OF_YEAR, mSelectedDate.get(Calendar.DAY_OF_YEAR));
                    mChildBuf.get(i).setDatetime(datetime.getTimeInMillis());
                    mChildBuf.get(i).setMealId(groupPosition + 1);

                    mViewModel.addRecord(mChildBuf.get(i));
                }
                mViewModel.pasteChildren();
                mRecyclerViewExpandableItemManager.notifyGroupAndChildrenItemsChanged(groupPosition);
                mChildBuf.clear();
                break;

            //Record delete
            case 3:
                mViewModel.delRecord((int) mRecordAdapter.getChildId(groupPosition, childPosition));
                mRecordAdapter.deleteChild(groupPosition, childPosition);
                break;

              // TODO: Record changing
//            //Record changing
//            case 4:
//                id = child.getLong(0);
//                Intent intent = new Intent(getActivity(), AddActivity.class);
//                intent.putExtra("recordId", id);
//                startActivity(intent);
//                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,@NonNull MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_diary, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_switch) {
            isRemaining = !isRemaining;
            mBinding.setIsRemaining(isRemaining);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWaterDialog() {
        Bundle bundle = new Bundle();
        bundle.putLong(AddWaterDialog.ARG_DATE, mViewModel.getDate().getTimeInMillis());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_diary_to_add_water_dialog, bundle);
    }

    private void showWeightDialog() {
        Bundle bundle = new Bundle();
        bundle.putLong(AddWeightDialog.ARG_DATE, mViewModel.getDate().getTimeInMillis());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_diary_to_add_weight_dialog, bundle);
    }

    @Override
    public void onGroupExpand(int groupPosition, boolean fromUser, Object payload) {
        if (fromUser) {
            adjustScrollPositionOnGroupExpanded(groupPosition);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.list_item_height);
        int verticalMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 16);

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight,
                verticalMargin, verticalMargin);
    }

}
