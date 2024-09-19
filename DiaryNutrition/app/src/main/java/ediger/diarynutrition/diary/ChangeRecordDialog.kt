package ediger.diarynutrition.diary

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogChangeRecordBinding
import ediger.diarynutrition.util.showKeyboard

class ChangeRecordDialog: DialogFragment() {

    private lateinit var binding: DialogChangeRecordBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.DialogStyle).apply {
            binding = DialogChangeRecordBinding.inflate(layoutInflater)

            setView(binding.root)
            setTitle(getString(R.string.dialog_title_change_serving))
            setPositiveButton(R.string.dialog_change) { dialog, _ ->
                val serving = binding.edServing.text?.toString()
                if (!serving.isNullOrBlank()) {
                    NavHostFragment.findNavController(this@ChangeRecordDialog)
                            .previousBackStackEntry?.savedStateHandle?.set(ARG_SERVING, serving.toInt())
                } else {
                    Toast.makeText(requireContext(), R.string.message_serving, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    override fun onStart() {
        super.onStart()
        binding.apply {
            serving = arguments?.getInt(ARG_SERVING) ?: 100
            executePendingBindings()
            edServing.post {
                requireActivity().showKeyboard(edServing)
                edServing.setSelection(edServing.length())
            }
        }
    }

    companion object {
        const val ARG_SERVING = "serving"
    }

}