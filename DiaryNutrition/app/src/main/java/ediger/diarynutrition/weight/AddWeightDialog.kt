package ediger.diarynutrition.weight

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Weight
import ediger.diarynutrition.databinding.DialogAddWeightBinding
import ediger.diarynutrition.util.hideKeyboard
import ediger.diarynutrition.util.showKeyboard
import java.text.SimpleDateFormat
import java.util.*

class AddWeightDialog : DialogFragment() {

    private val viewModel: WeightViewModel by viewModels()

    private lateinit var binding: DialogAddWeightBinding

    private val date = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity(), R.style.DialogStyle).apply {
            binding = DialogAddWeightBinding.inflate(LayoutInflater.from(requireActivity()))

            setView(binding.root)
            setTitle(getString(R.string.dialog_title_weight))
            setPositiveButton(getString(R.string.dialog_add)) { dialog, _ ->
                val weightAmount = binding.edWeight.text.toString()

                if (weightAmount.isBlank() || weightAmount.startsWith("0")) {
                    Toast.makeText(activity, getString(R.string.message_dialog_weight),
                            Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addWeight(
                            Weight(weightAmount.toFloat(), date.timeInMillis)
                    )
                }
                activity?.hideKeyboard(binding.root.findFocus())
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                activity?.hideKeyboard(binding.root.findFocus())
                dialog.dismiss()
            }
        }.create()
    }

    override fun onStart() {
        super.onStart()

        dateSelectionInit()
        binding.edWeight.requestFocus()
        activity?.showKeyboard(binding.edWeight)
    }

    private fun dateSelectionInit() {
        date.timeInMillis = arguments?.getLong(ARG_DATE) ?: date.timeInMillis

        binding.edDate.setText(dateFormat.format(date.time))
        binding.edDate.setOnClickListener {
            DatePickerDialog(
                    requireActivity(),
                    { _, year, month, dayOfMonth ->
                        date[Calendar.YEAR] = year
                        date[Calendar.MONTH] = month
                        date[Calendar.DAY_OF_MONTH] = dayOfMonth
                        binding.edDate.setText(dateFormat.format(date.time))
                    },
                    date[Calendar.YEAR],
                    date[Calendar.MONTH],
                    date[Calendar.DAY_OF_MONTH]
            ).show()
        }
    }

    companion object {
        const val ARG_DATE = "date"
    }
}