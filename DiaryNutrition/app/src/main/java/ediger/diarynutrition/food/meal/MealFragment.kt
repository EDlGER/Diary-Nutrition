package ediger.diarynutrition.food.meal

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.databinding.FragmentMealBinding
import java.text.SimpleDateFormat
import java.util.*

class MealFragment : Fragment() {

    private lateinit var binding: FragmentMealBinding

    private val viewModel: MealViewModel by viewModels()

    private lateinit var adapter: MealAdapter

    private val timeFormatter = SimpleDateFormat("kk:mm", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMealBinding.inflate(inflater, container, false)

        adapter = MealAdapter { foodId, serving, position ->
            val servingValue = when(serving) { "" -> 100 else -> serving.toInt() }
            viewModel.updateServing(foodId, servingValue)

            val holder = binding.list.findViewHolderForAdapterPosition(position) as MealFoodViewHolder
            adapter.onBindViewHolder(holder, position)
        }
        binding.toolbar.navigationIcon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.onBarPrimary) )
        }

        // TODO: Clicking behavior
        binding.toolbar.setNavigationOnClickListener {
            Log.d("MealFragment", "Nav icon was clicked")
        }
        binding.list.adapter = adapter
        binding.list.setHasFixedSize(false)

        timeSelectionInit()

        val id = arguments?.getInt(FOOD_ID)
        id?.let {
            viewModel.getFood(id).observe(viewLifecycleOwner) { viewModel.foodSelected(it) }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.recordAndFoodList.observe(viewLifecycleOwner) { adapter.submitList(it.toList()) }
        viewModel.mealList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mealSelectionInit(it)
            }
        }
    }

    private fun mealSelectionInit(mealList: List<Meal>) {
        binding.edMeal.setText(
                mealList.findLast { it.id == viewModel.selectedMealId }?.name
        )

        binding.edMeal.setOnClickListener {
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
    }

    private fun timeSelectionInit() {
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

    companion object {
        const val TAG = "MealFragment"
        const val FOOD_ID = "foodId"
    }

}