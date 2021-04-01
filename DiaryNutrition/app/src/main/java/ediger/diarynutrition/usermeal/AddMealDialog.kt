package ediger.diarynutrition.usermeal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogAddMealBinding
import ediger.diarynutrition.util.hideKeyboard
import ediger.diarynutrition.util.showKeyboard

class AddMealDialog: DialogFragment() {

    private lateinit var binding: DialogAddMealBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.DialogStyle).apply {
            binding = DialogAddMealBinding.inflate(LayoutInflater.from(requireActivity()))

            setView(binding.root)
            setTitle(getString(R.string.dialog_title_meal_add))
            setPositiveButton(R.string.dialog_add) { dialog, _ ->
                val mealName = binding.edMeal.text?.toString()
                if (!mealName.isNullOrBlank()) {
                    NavHostFragment.findNavController(this@AddMealDialog)
                            .previousBackStackEntry?.savedStateHandle?.set(ARG_MEAL_NAME_NEW, mealName)
                } else {
                    Toast.makeText(requireContext(), R.string.message_meal, Toast.LENGTH_SHORT).show()
                }
                activity?.hideKeyboard(binding.root.findFocus())
                dialog.dismiss()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                activity?.hideKeyboard(binding.root.findFocus())
                dialog.dismiss()
            }
        }.create()
    }

    override fun onStart() {
        super.onStart()
        binding.edMeal.requestFocus()
        activity?.showKeyboard(binding.edMeal)
    }

    companion object {
        const val ARG_MEAL_NAME_NEW = "meal_name_new"
    }

}