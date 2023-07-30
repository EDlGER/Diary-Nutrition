package ediger.diarynutrition.food.meal

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ediger.diarynutrition.*
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.databinding.FragmentMealBinding
import ediger.diarynutrition.food.FoodViewModel
import ediger.diarynutrition.util.hideKeyboard
import java.text.SimpleDateFormat
import java.util.*

class MealFragment : Fragment() {

    private lateinit var binding: FragmentMealBinding

    private val viewModel: MealViewModel by viewModels()

    private val foodViewModel: FoodViewModel by activityViewModels()

    private lateinit var adapter: MealAdapter

    private lateinit var bottomSheet: BottomSheetBehavior<ViewGroup>

    private var interstitialAd: InterstitialAd? = null

    private var isAdRemoved = false

    private val timeFormatter = SimpleDateFormat("kk:mm", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMealBinding.inflate(inflater, container, false)

        val pref = requireActivity().getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
        isAdRemoved = pref.getBoolean(PREF_PREMIUM, false)

        timeSelectionInit()

        listInit()

        if (!isAdRemoved) adInit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = BottomSheetBehavior.from(binding.root.parent as ViewGroup)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        navigationInit()

        subscribeUi()
    }

    fun submitFood(id: Int) {
        viewModel.getFood(id).observe(viewLifecycleOwner) {
            viewModel.foodSelected(it)
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun subscribeUi() {
        arguments?.getInt(FOOD_ID)?.let { id ->
            viewModel.getFood(id).observe(viewLifecycleOwner) { viewModel.foodSelected(it) }
        }

        viewModel.recordAndFoodList.observe(viewLifecycleOwner) { adapter.submitList(it.toList()) }

        viewModel.mealList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                viewModel.updateMealId()
                mealSelectionInit(it)
            }
        }
    }

    private fun navigationInit() = with(binding) {
        toolbar.navigationIcon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.onBarPrimary))
        }

        bottomSheet.apply {
            state = BottomSheetBehavior.STATE_EXPANDED

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            activity?.hideKeyboard(binding.root.findFocus())
                            isDraggable = true
                        }

                        BottomSheetBehavior.STATE_EXPANDED ->
                            isDraggable = false

                        else -> { }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset >= 0) {
                        motionLayout.progress = 1.0f - slideOffset
                    }
                }
            })
        }

        toolbar.setNavigationOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        fabAddFood.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        fabAddMeal.setOnClickListener {
            this@MealFragment.viewModel.addRecords()
            requireActivity().hideKeyboard(view)

            interstitialAd?.show(requireActivity())

            navigateToDiary()
        }
    }

    private fun listInit() {
        adapter = MealAdapter { id, serving ->
            val servingValue = when(serving) {
                "", "0" -> 100
                else -> serving.toInt()
            }
            viewModel.updateServing(id, servingValue)
            val holder = binding.list.findViewHolderForItemId(id.toLong()) as MealFoodViewHolder

            adapter.onBindViewHolder(holder, holder.bindingAdapterPosition)
        }
        binding.list.adapter = adapter
        binding.list.setHasFixedSize(false)
    }

    private fun adInit() {
        val adRequest = AdRequest.Builder().build()

        //TODO: getString(R.string.banner_ad_inter_id)
        InterstitialAd.load(
                requireActivity().applicationContext,
                "ca-app-pub-3940256099942544/1033173712",
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        interstitialAd = null
                    }

                    override fun onAdLoaded(p0: InterstitialAd) {
                        interstitialAd = p0
                    }
                }
        )

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
            }
        }
    }

    private fun mealSelectionInit(mealList: List<Meal>) {
        binding.edMeal.setText(
                mealList.findLast { it.id == viewModel.selectedMealId }?.name
        )

        binding.edMeal.setOnClickListener {
            if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
                showMealDialog(mealList)
            } else if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun timeSelectionInit() {
        viewModel.selectedTime = requireActivity().intent.getLongExtra(ARG_DATE, viewModel.selectedTime)
        binding.edTime.setText(timeFormatter.format(Date(viewModel.selectedTime)))
        binding.edTime.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = viewModel.selectedTime }
            TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        viewModel.selectedTime = calendar.timeInMillis
                        binding.edTime.setText(timeFormatter.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            ).show()
        }
    }

    private fun showMealDialog(mealList: List<Meal>) {
        AlertDialog.Builder(requireContext()).apply {
            setItems(
                    mealList.map { it.name }.toTypedArray()
            ) { _, which ->
                binding.edMeal.setText(mealList[which].name)
                viewModel.selectedMealId = mealList[which].id
            }
            create()
        }.show()
    }

    private fun navigateToDiary() {
        requireActivity().setResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra(ARG_DATE, viewModel.selectedTime) }
        )
        requireActivity().finish()
    }

    override fun onStart() {
        super.onStart()
        foodViewModel.isSheetActive.value = true
    }

    override fun onDestroyView() {
        foodViewModel.isSheetActive.value = false
        super.onDestroyView()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val clickedItemId = item.order
        return when (item.itemId) {
            R.integer.action_context_remove -> {
                viewModel.removeFood(clickedItemId)

                if (viewModel.recordAndFoodList.value?.size == 0) {
                    activity?.supportFragmentManager?.popBackStack()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    companion object {
        const val TAG = "MealFragment"
        const val FOOD_ID = "foodId"
    }

}