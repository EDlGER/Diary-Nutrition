package ediger.diarynutrition.food

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.databinding.DialogAddFoodBinding

class AddFoodDialog: DialogFragment() {

    private lateinit var binding: DialogAddFoodBinding
    private val viewModel: FoodViewModel by activityViewModels()
    private var foodId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddFoodBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        foodId = arguments?.get(FoodViewModel.FOOD_ID) as? Int
        foodId?.let {
            viewModel.getFood(foodId)?.observe(viewLifecycleOwner) {
                binding.food = it
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            hideKeyboard()
            dismiss()
        }

        binding.toolbar.setOnMenuItemClickListener {
            hideKeyboard()
            if (foodId == null) addFood() else updateFood()
            true
        }
    }

    private fun addFood() {
        with(binding) {
            val isSuccess = viewModel.addFood(
                    edFoodName.text?.toString(),
                    edCal.text?.toString()?.toFloatOrNull(),
                    edProt.text?.toString()?.toFloatOrNull(),
                    edFat.text?.toString()?.toFloatOrNull(),
                    edCarbo.text?.toString()?.toFloatOrNull(),
                    edGI.text?.toString()?.toIntOrNull()
            )
            if (isSuccess) dismiss()
        }
    }

    private fun updateFood() {
        with(binding) {
            val food = food
            ifLet(
                    edCal.text.toString().toFloatOrNull(),
                    edProt.text.toString().toFloatOrNull(),
                    edFat.text.toString().toFloatOrNull(),
                    edCarbo.text.toString().toFloatOrNull()
            ) { (cal, prot, fat, carbo) ->
                if (!edFoodName.text.isNullOrBlank()) food?.name = edFoodName.text.toString()
                food?.cal = cal
                food?.prot = prot
                food?.fat = fat
                food?.carbo = carbo
                if (edGI.text.toString().toIntOrNull() != null) food?.gi = edGI.text.toString().toInt()
            }
            viewModel.updateFood(food)
            dismiss()
        }
    }

    private fun hideKeyboard() {
        val view = requireView().findFocus()
        if (view != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

inline fun <T: Any> ifLet(vararg elements: T?, closure: (List<T>) -> Unit) {
    if (elements.all { it != null }) {
        closure(elements.filterNotNull())
    }
}