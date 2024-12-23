package ediger.diarynutrition.usermeal

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogChangeMealBinding
import ediger.diarynutrition.util.showKeyboard

class ChangeMealDialog: DialogFragment() {

    private lateinit var binding: DialogChangeMealBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.DialogStyle).apply {
            binding = DialogChangeMealBinding.inflate(layoutInflater)

            setView(binding.root)
            setTitle(getString(R.string.dialog_title_change_meal))
            setPositiveButton(R.string.dialog_change) { dialog, _ ->
                val mealName = binding.edMeal.text?.toString()
                if (!mealName.isNullOrBlank()) {
                    NavHostFragment.findNavController(this@ChangeMealDialog)
                            .previousBackStackEntry?.savedStateHandle?.set(ARG_MEAL_NAME, mealName)
                } else {
                    Toast.makeText(requireContext(), R.string.message_meal, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    override fun onStart() {
        super.onStart()
        binding.apply {
            mealName = arguments?.getString(ARG_MEAL_NAME) ?: ""
            executePendingBindings()
            edMeal.post {
                requireActivity().showKeyboard(edMeal)
                edMeal.setSelection(edMeal.length())
            }
        }
    }

    companion object {
        const val ARG_MEAL_NAME = "meal_name"
    }

}