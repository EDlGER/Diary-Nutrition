package ediger.diarynutrition.usermeal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogBottomMealOptionsBinding
import ediger.diarynutrition.diary.ChangeRecordDialog

class MealOptionsBottomDialog: BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomMealOptionsBinding

    private val viewModel: MealOptionsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogBottomMealOptionsBinding.inflate(inflater, container, false)

        arguments?.getInt(MEAL_ID)?.let { mealId ->
            viewModel.setMealId(mealId)
        }

        binding.txtActionEdit.setOnClickListener { showChangeMealDialog() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMealNameChange()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun observeMealNameChange() {
        val currentEntry = NavHostFragment.findNavController(this)
                .getBackStackEntry(R.id.nav_dialog_meal_options)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                    && currentEntry.savedStateHandle.contains(ChangeMealDialog.ARG_MEAL_NAME)) {
                currentEntry.savedStateHandle.get<String>(ChangeMealDialog.ARG_MEAL_NAME)?.let { mealName ->
                    viewModel.editMeal(mealName)
                    currentEntry.savedStateHandle.remove<String>(ChangeMealDialog.ARG_MEAL_NAME)
                }
            }
        }

        currentEntry.lifecycle.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                currentEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun showChangeMealDialog() {
        viewModel.selectedMeal?.let { meal ->
            val bundle = bundleOf(ChangeMealDialog.ARG_MEAL_NAME to meal.name)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_nav_dialog_meal_options_to_nav_dialog_change_meal, bundle)
        }
    }

    companion object {
        const val MEAL_ID = "meal_id"
    }

}