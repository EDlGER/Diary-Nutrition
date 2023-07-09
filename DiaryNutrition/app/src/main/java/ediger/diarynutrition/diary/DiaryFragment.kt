package ediger.diarynutrition.diary

import android.animation.AnimatorInflater
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ediger.diarynutrition.*
import ediger.diarynutrition.data.source.entities.Summary
import ediger.diarynutrition.databinding.FragmentDiaryBinding
import ediger.diarynutrition.diary.adapters.RecordAdapter
import ediger.diarynutrition.diary.adapters.WaterFooterAdapter
import ediger.diarynutrition.diary.water.AddWaterDialog
import ediger.diarynutrition.diary.water.WaterActivity
import ediger.diarynutrition.food.FoodActivity
import ediger.diarynutrition.objects.SnackbarMessage
import ediger.diarynutrition.util.SnackbarUtils
import ediger.diarynutrition.weight.AddWeightDialog
import ediger.diarynutrition.widgets.CalendarTitleView
import java.util.*

class DiaryFragment : Fragment() {
    private lateinit var binding: FragmentDiaryBinding

    private val viewModel: DiaryViewModel by viewModels()

    private lateinit var adapter: RecordAdapter
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private lateinit var listItemManager: RecyclerViewExpandableItemManager

    private var actionBar: ActionBar? = null

    private var isCalendarExpanded = false

    private val foodActivityContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        val date = it.getLongExtra(ARG_DATE, -1)
                        if (date != -1L) {
                            val calendar = Calendar.getInstance().apply { timeInMillis = date }
                            viewModel.date = calendar
                            binding.calendarView.setCurrentDate(calendar.time)
                            (actionBar?.customView as? CalendarTitleView)?.setSubtitle(calendar)
                        }
                    }
                }
            }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setupHeaderSwitcher()
        setupList(savedInstanceState)
        setupFab()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordsList.observe(viewLifecycleOwner) { records ->
            adapter.setRecordList(records)
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner,
                SnackbarMessage.SnackbarObserver { snackbarMessageResourceId: Int ->
                    SnackbarUtils.showSnackbar(view, getString(snackbarMessageResourceId))
                }
        )

        observeServingChange()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun setupActionBar() {
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
        actionBar?.customView = customToolbar
        setupCalendar(customToolbar)
    }

    private fun setupCalendar(customToolbar: CalendarTitleView) {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val currentLocale = appLocales[0] ?: Locale.getDefault()

        binding.calendarView.apply {
            setLocale(TimeZone.getDefault(), currentLocale)
            setShouldDrawDaysHeader(true)
            setCurrentDate(viewModel.date?.time)
            setListener(object : CompactCalendarViewListener {
                override fun onDayClick(dateClicked: Date) {
                    viewModel.date = Calendar.getInstance().apply { time = dateClicked }.also {
                        customToolbar.setSubtitle(it)
                    }
                    listItemManager.collapseAll()
                    customToolbar.datePickerClick()
                }

                override fun onMonthScroll(firstDayOfNewMonth: Date) {
                    viewModel.date = Calendar.getInstance().apply { time = firstDayOfNewMonth }.also {
                        customToolbar.setSubtitle(it)
                    }
                    listItemManager.collapseAll()
                }
            })
        }
    }

    private fun setupList(savedInstanceState: Bundle?) {
        listItemManager = RecyclerViewExpandableItemManager(savedInstanceState)

        adapter = RecordAdapter(listItemManager)
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
            viewModel.date?.let {
                if (isCalendarExpanded) {
                    hideCalendar()
                }
                binding.fabMenu.collapseImmediately()
                foodActivityContract.launch(FoodActivity.getIntent(requireActivity(), it.timeInMillis))
            }
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

    private fun observeServingChange() {
        val currentEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.nav_diary)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                    && currentEntry.savedStateHandle.contains(ChangeRecordDialog.ARG_SERVING)) {
                currentEntry.savedStateHandle.get<Int>(ChangeRecordDialog.ARG_SERVING)?.let { serving ->
                    viewModel.updateRecord(serving)
                    currentEntry.savedStateHandle.remove<Int>(ChangeRecordDialog.ARG_SERVING)
                }
            }
        }

        currentEntry.getLifecycle().addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                currentEntry.getLifecycle().removeObserver(observer)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val goalSummary = Summary(
                PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_PROT, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_FAT, Float::class.javaObjectType, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_CARBO, Float::class.javaObjectType, 1f))
        binding.goal = goalSummary

        actionBar = (context as AppCompatActivity).supportActionBar
        setupActionBar()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (requireActivity() as MainActivity).appBar.stateListAnimator = AnimatorInflater
                    .loadStateListAnimator(activity, R.animator.appbar_unelevated_animator)
        }
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)

        viewModel.updateDate()
    }

    override fun onPause() {
        super.onPause()
        if (isCalendarExpanded) {
            hideCalendar()
        }
        if (!(requireActivity() as MainActivity).navigationView.menu.findItem(R.id.nav_diary).isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (requireActivity() as MainActivity).appBar.stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(activity, R.animator.appbar_elevated_animator)
            }
            actionBar?.setDisplayShowCustomEnabled(false)
            actionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    fun onContextMenuClosed() {
        adapter.setContextMenuState(false)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val groupPosition = item.groupId
        val childPosition = item.order
        //val viewHolder = binding.listRecords.findContainingViewHolder(item.actionView) as? ExpandableItemViewHolder

        when (item.itemId) {
            R.integer.action_context_delete -> {
                val childId = adapter.getChildId(groupPosition, childPosition).toInt()
                viewModel.delRecord(childId)
                adapter.deleteChild(groupPosition, childPosition)
            }
            R.integer.action_context_change_serving -> {
                adapter.getChildRecordData(groupPosition, childPosition)?.let { record ->
                    viewModel.selectedRecordId = record.id
                    showServingChangeDialog(record.serving)
                }
            }
            R.integer.action_context_change_time -> {
                adapter.getChildRecordData(groupPosition, childPosition)?.let { record ->
                    showTimeChangeDialog(record.datetime) { datetime ->
                        viewModel.selectedRecordId = record.id
                        viewModel.updateRecordTime(datetime)
                    }
                }
            }
            R.integer.action_context_copy -> {
                viewModel.copyMeal(
                        mealId = adapter.getGroupId(groupPosition).toInt()
                )
            }
            R.integer.action_context_paste -> {
                viewModel.pasteMeal(
                        mealId = adapter.getGroupId(groupPosition).toInt()
                )
                listItemManager.notifyGroupAndChildrenItemsChanged(groupPosition)

            }
            else -> return super.onContextItemSelected(item)
        }
        return true
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
        viewModel.date?.let { date ->
            val bundle = bundleOf(AddWaterDialog.ARG_DATE to date.timeInMillis)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_diary_to_add_water_dialog, bundle)
        }
    }

    private fun showWeightDialog() {
        viewModel.date?.let { date ->
            val bundle = bundleOf(AddWeightDialog.ARG_DATE to date.timeInMillis)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_diary_to_add_weight_dialog, bundle)
        }
    }

    private fun showServingChangeDialog(initialServing: Int) {
        val bundle = bundleOf(ChangeRecordDialog.ARG_SERVING to initialServing)
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_diary_to_change_record_dialog, bundle)
    }

    private fun showTimeChangeDialog(initialDatetime: Long, onTimeSetListener: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialDatetime }
        TimePickerDialog(requireActivity(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    onTimeSetListener.invoke(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show()
    }

    private fun hideCalendar() {
        binding.calendarView.hideCalendar()
        (actionBar?.customView as CalendarTitleView).animateArrow()
        isCalendarExpanded = false
    }

    private fun adjustScrollPositionOnGroupExpanded(groupPosition: Int) {
        val childItemHeight = requireActivity().resources.getDimensionPixelSize(R.dimen.list_item_height)
        val verticalMargin = (requireActivity().resources.displayMetrics.density * 16).toInt()
        listItemManager.scrollToGroup(groupPosition, childItemHeight,
                verticalMargin, verticalMargin)
    }

}