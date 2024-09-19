package ediger.diarynutrition.usermeal

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogBottomMealOptionsBinding

class MealOptionsBottomDialog: BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomMealOptionsBinding

    private val viewModel: MealOptionsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBottomMealOptionsBinding.inflate(inflater, container, false)

        arguments?.getInt(ARG_MEAL_ID)?.let { mealId ->
            viewModel.setMealId(mealId)
        }

        binding.txtActionEdit.setOnClickListener { showChangeMealDialog() }
        binding.txtActionDelete.setOnClickListener { showDeleteAlertDialog() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMealNameChange()

        initUserMealsIconColor()

        viewModel.shouldDismiss.observe(viewLifecycleOwner) { isDismissRequested ->
            if (isDismissRequested) {
                NavHostFragment.findNavController(this)
                        .previousBackStackEntry?.savedStateHandle?.set(ARG_DIALOG_DISMISSED, true)
                dialog?.dismiss()
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun observeMealNameChange() {
        val currentEntry = NavHostFragment.findNavController(this)
            .getBackStackEntry(R.id.nav_dialog_meal_options)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START
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

    private fun initUserMealsIconColor() {
        viewModel.isUserMeal.observe(viewLifecycleOwner) { isUser ->
            val color = when(isUser) { true -> R.color.text_medium else -> R.color.text_inactive}

            binding.txtActionEdit.compoundDrawablesRelative.first()?.let { icon ->
                DrawableCompat.setTintList(
                        icon, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
                )
            }
            binding.txtActionDelete.compoundDrawablesRelative.first()?.let { icon ->
                DrawableCompat.setTintList(
                        icon, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
                )
            }
        }
    }

    private fun showChangeMealDialog() {
        viewModel.selectedMeal?.let { meal ->
            val bundle = bundleOf(ChangeMealDialog.ARG_MEAL_NAME to meal.name)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_nav_dialog_meal_options_to_nav_dialog_change_meal, bundle)
        }
    }

    private fun showDeleteAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_title_delete_alert))
                .setMessage(getString(R.string.dialog_message_delete_alert) + " " + getString(R.string.meal5))
                .setPositiveButton(getString(R.string.action_delete)) { _, _ -> viewModel.deleteSelectedMeal()}
                .setNegativeButton(getString(R.string.action_back)) { dialog, _ -> dialog.dismiss()}
                .show()
    }

    companion object {
        const val ARG_MEAL_ID = "meal_id"
        const val ARG_DIALOG_DISMISSED = "dialog_dismissed"
    }

}