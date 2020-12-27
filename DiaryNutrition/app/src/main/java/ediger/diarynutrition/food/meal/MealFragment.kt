package ediger.diarynutrition.food.meal

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.databinding.FragmentMealBinding
import java.text.SimpleDateFormat
import java.util.*

class MealFragment : Fragment() {

    private lateinit var binding: FragmentMealBinding

    private val viewModel: MealViewModel by viewModels()

    private lateinit var adapter: MealAdapter

    private val timeFormatter = SimpleDateFormat("kk:mm", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMealBinding.inflate(inflater, container, false)

        adapter = MealAdapter { foodId, serving ->
            if (serving.isNotBlank()) {
                viewModel.updateServing(foodId, serving.toInt())
            }
        }

        binding.list.adapter = adapter
        binding.list.setHasFixedSize(false)

        mealSelectionInit()

        timeSelectionInit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordAndFoodList.observe(viewLifecycleOwner) { adapter.submitList(it) }

        // TODO: Pass viewModel and lifecycleOwner to the binding

    }

    private fun mealSelectionInit() = with(binding) {
        val mealSelectionAdapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_list_item_1, viewModel.mealList.toTypedArray()
        )
        atvMeal.setAdapter(mealSelectionAdapter)
        atvMeal.setText(
                viewModel.mealList.findLast { it.id == viewModel.selectedMealId }?.name
        )
        atvMeal.setOnClickListener { (it as AutoCompleteTextView).showDropDown() }
        atvMeal.setOnItemClickListener { parent, _, position, _ ->
            val mealId = (parent.getItemAtPosition(position) as Meal).id
            viewModel.selectedMealId = mealId
        }
    }

    private fun timeSelectionInit() = with(binding) {
        edTime.setText(timeFormatter.format(Date(viewModel.selectedTime)))
        edTime.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = viewModel.selectedTime }
            TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        viewModel.selectedTime = calendar.timeInMillis
                        edTime.setText(timeFormatter.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            ).show()
        }
    }

}