package ediger.diarynutrition.diary

import android.animation.AnimatorInflater
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import ediger.diarynutrition.*
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.databinding.FragmentDiaryBinding
import ediger.diarynutrition.diary.adapters.RecordAdapter
import ediger.diarynutrition.diary.adapters.WaterFooterAdapter
import ediger.diarynutrition.diary.water.AddWaterDialog
import ediger.diarynutrition.diary.water.WaterActivity
import ediger.diarynutrition.food.FoodActivity
import ediger.diarynutrition.objects.SnackbarMessage.SnackbarObserver
import ediger.diarynutrition.util.SnackbarUtils
import ediger.diarynutrition.weight.AddWeightDialog
import ediger.diarynutrition.widgets.CalendarTitleView
import java.text.SimpleDateFormat
import java.util.*

class DiaryFragment : Fragment() {
    private lateinit var binding: FragmentDiaryBinding

    private val viewModel: DiaryViewModel by viewModels()

    private lateinit var adapter: RecordAdapter
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private lateinit var listItemManager: RecyclerViewExpandableItemManager

    // TODO: Delete (move logic into the viewModel)
    private val mChildBuf: MutableList<Record> = ArrayList()

    private lateinit var actionBar: ActionBar

    private var isCalendarExpanded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setupActionBar()
        setupHeaderSwitcher()
        setupList(savedInstanceState)
        setupFab()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Recycler
        viewModel.recordsList.observe(viewLifecycleOwner) { records ->
            adapter.setRecordList(records)
        }

        //Snackbar
        viewModel.snackbarMessage.observe(this,
                SnackbarObserver { snackbarMessageResourceId: Int ->
                    SnackbarUtils.showSnackbar(view, getString(snackbarMessageResourceId))
                }
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

    }

    private fun setupActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            actionBar = it
        }

        val customToolbar = CalendarTitleView(requireContext()).apply {
            setDateClickListener {
                if (!binding.calendarView.isAnimating) {
                    this.animateArrow()
                    isCalendarExpanded = if (isCalendarExpanded) {
                        binding.calendarView.hideCalendar()
                        false
                    } else {
                        binding.calendarView.showCalendar()
                        true
                    }
                }
            }
            viewModel.date?.let { setSubtitle(it) }
        }
        if (this::actionBar.isInitialized) {
            actionBar.customView = customToolbar
        }
        setupCalendar(customToolbar)
    }

    private fun setupCalendar(customToolbar: CalendarTitleView) {
        binding.calendarView.apply {
            setLocale(TimeZone.getDefault(), Locale.getDefault())
            setShouldDrawDaysHeader(true)
            setCurrentDate(viewModel.date?.time)
            setListener(object : CompactCalendarViewListener {
                override fun onDayClick(dateClicked: Date) {
                    viewModel.date = Calendar.getInstance().apply { time = dateClicked }.also {
                        customToolbar.setSubtitle(it)
                    }
                    customToolbar.datePickerClick()
                }

                override fun onMonthScroll(firstDayOfNewMonth: Date) {
                    viewModel.date = Calendar.getInstance().apply { time = firstDayOfNewMonth }.also {
                        customToolbar.setSubtitle(it)
                    }
                }
            })
        }
    }

    private fun setupList(savedInstanceState: Bundle?) {
        listItemManager = RecyclerViewExpandableItemManager(savedInstanceState)

        adapter = RecordAdapter(listItemManager, this)
        wrappedAdapter = listItemManager.createWrappedAdapter(adapter)

        listItemManager.setOnGroupExpandListener { groupPosition, fromUser, _ ->
            if (fromUser) {
                adjustScrollPositionOnGroupExpanded(groupPosition)
            }
        }

        if (PreferenceHelper.getValue(KEY_PREF_UI_WATER_CARD, Boolean::class.javaObjectType, true)) {
            setupWaterFooter()
        }
        binding.listRecords.adapter = wrappedAdapter // requires *wrapped* adapter
        binding.listRecords.setHasFixedSize(false)

        binding.listRecords.itemAnimator = RefactoredDefaultItemAnimator().apply { supportsChangeAnimations = false }

        listItemManager.attachRecyclerView(binding.listRecords)
        registerForContextMenu(binding.listRecords)
    }

    private fun setupWaterFooter() {
        val onWaterClickListener = View.OnClickListener {
            val water = viewModel.water.value
            if (water != null && water.amount != 0) {
                navigateToWaterActivity()
            } else {
                SnackbarUtils.showSnackbar(view, getString(R.string.message_card_water))
            }
        }
        wrappedAdapter = WaterFooterAdapter(wrappedAdapter, viewLifecycleOwner, viewModel, onWaterClickListener)
    }

    private fun setupHeaderSwitcher() = with(binding) {
        txtSwConsume.setFactory {
            TextView(activity).apply {
                gravity = Gravity.CENTER_VERTICAL
                textSize = 15f
                setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
            }
        }
        txtSwConsume.inAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
        txtSwConsume.outAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
    }

    private fun setupFab() {
        binding.fabActionFood.setOnClickListener {
            navigateToFoodActivity()
        }
        binding.fabActionWater.setOnClickListener {
            binding.fabMenu.collapseImmediately()
            showWaterDialog()
        }
        binding.fabActionWeight.setOnClickListener {
            binding.fabMenu.collapseImmediately()
            showWeightDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        val goalSummary = Summary(
                PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_PROT, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_FAT, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_CARBO, Float::class.javaObjectType, 1f))
        binding.goal = goalSummary
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (requireActivity() as MainActivity).appBar.stateListAnimator = AnimatorInflater
                    .loadStateListAnimator(activity, R.animator.appbar_unelevated_animator)
        }
        if (this::actionBar.isInitialized) {
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!(requireActivity() as MainActivity).navigationView.menu.findItem(R.id.nav_diary).isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (requireActivity() as MainActivity).appBar.stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(activity, R.animator.appbar_elevated_animator)
            }
            if (this::actionBar.isInitialized) {
                actionBar.setDisplayShowCustomEnabled(false)
                actionBar.setDisplayShowTitleEnabled(true)
            }
        }
    }

    // TODO: Refactor
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val rv = RecyclerViewAdapterUtils.getParentRecyclerView(v) ?: return
        val vh = rv.findContainingViewHolder(v)
        val rootPosition = vh!!.bindingAdapterPosition
        if (rootPosition == RecyclerView.NO_POSITION) {
            return
        }
        val rootAdapter = rv.adapter
        val localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter!!, adapter, rootPosition)
        val expandablePosition = listItemManager.getExpandablePosition(localFlatPosition)
        val groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition)
        val childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition)
        val childCount = adapter.getChildCount(groupPosition)
        if (childPosition == RecyclerView.NO_POSITION) {
            //group
            if (listItemManager.isGroupExpanded(groupPosition) && childCount != 0) {
                menu.add(0, 1, 0, R.string.context_menu_copy).actionView = v
            }
            menu.add(0, 2, 0, R.string.context_menu_paste).actionView = v
        } else {
            //child
            menu.add(0, 3, 0, R.string.context_menu_del).actionView = v
            menu.add(0, 4, 0, R.string.context_menu_change).actionView = v
        }
        adapter.setContextMenuState(true)
    }

    fun onContextMenuClosed() {
        adapter.setContextMenuState(false)
    }

    // TODO: Refactor
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val rv = RecyclerViewAdapterUtils.getParentRecyclerView(item.actionView)
                ?: return super.onContextItemSelected(item)
        val vh = rv.findContainingViewHolder(item.actionView)
        val rootPosition = vh!!.bindingAdapterPosition
        if (rootPosition == RecyclerView.NO_POSITION) {
            return false
        }
        val rootAdapter = rv.adapter
        val localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter!!, adapter, rootPosition)
        val expandablePosition = listItemManager.getExpandablePosition(localFlatPosition)
        val groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition)
        val childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition)
        val childCount = adapter.getChildCount(groupPosition)
        when (item.itemId) {
            1 -> {
                var i = 0
                while (i < childCount) {
                    mChildBuf.add(adapter.getChildRecordData(groupPosition, i))
                    i++
                }
                SnackbarUtils.showSnackbar(view, getString(R.string.message_record_meal_copy))
            }
            2 -> {
                if (mChildBuf.size == 0) {
                    SnackbarUtils.showSnackbar(view,
                            getString(R.string.message_record_meal_insert_fail))
                    return true
                }
                var i = 0
                while (i < mChildBuf.size) {
                    val datetime = Calendar.getInstance()
                    datetime.timeInMillis = mChildBuf[i].datetime
                    //datetime[Calendar.DAY_OF_YEAR] = mSelectedDate[Calendar.DAY_OF_YEAR]
                    mChildBuf[i].datetime = datetime.timeInMillis
                    mChildBuf[i].mealId = groupPosition + 1
                    viewModel.addRecord(mChildBuf[i])
                    i++
                }
                viewModel.pasteChildren()
                listItemManager.notifyGroupAndChildrenItemsChanged(groupPosition)
                mChildBuf.clear()
            }
            3 -> {
                viewModel.delRecord(adapter.getChildId(groupPosition, childPosition).toInt())
                adapter.deleteChild(groupPosition, childPosition)
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.fragment_diary, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_switch) {
            viewModel.switchIsRemaining()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToFoodActivity()  {
        startActivity(
                Intent(requireActivity(), FoodActivity::class.java).apply {
                    putExtra(ARG_DATE, viewModel.date!!.timeInMillis)
                }
        )
    }

    private fun navigateToWaterActivity() {
        val location = IntArray(2)
        binding.expansionView.getLocationInWindow(location)

        val intent = Intent(activity, WaterActivity::class.java).apply {
            putExtra(ARG_EXPANSION_LEFT_OFFSET, location[0])
            putExtra(ARG_EXPANSION_TOP_OFFSET, location[1])
            putExtra(ARG_EXPANSION_VIEW_WIDTH, binding.expansionView.width)
            putExtra(ARG_EXPANSION_VIEW_HEIGHT, binding.expansionView.height)
            putExtra(WaterActivity.ARG_DATE, viewModel.date?.timeInMillis)
        }
        startActivity(intent)
    }

    private fun showWaterDialog() {
        val bundle = Bundle().apply { putLong(AddWaterDialog.ARG_DATE, viewModel.date!!.timeInMillis) }
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_diary_to_add_water_dialog, bundle)
    }

    private fun showWeightDialog() {
        val bundle = Bundle().apply { putLong(AddWeightDialog.ARG_DATE, viewModel.date!!.timeInMillis) }
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_diary_to_add_weight_dialog, bundle)
    }

    private fun adjustScrollPositionOnGroupExpanded(groupPosition: Int) {
        val childItemHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.list_item_height)
        val verticalMargin = (requireActivity().resources.displayMetrics.density * 16).toInt()
        listItemManager.scrollToGroup(groupPosition, childItemHeight,
                verticalMargin, verticalMargin)
    }

}