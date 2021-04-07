package ediger.diarynutrition.diary.water

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import ediger.diarynutrition.*
import ediger.diarynutrition.databinding.DialogAddWaterBinding
import ediger.diarynutrition.util.SnackbarUtils
import ediger.diarynutrition.util.hideKeyboard
import ediger.diarynutrition.util.showKeyboard
import java.text.SimpleDateFormat
import java.util.*

class AddWaterDialog : DialogFragment(), View.OnClickListener, OnLongClickListener {

    private lateinit var binding: DialogAddWaterBinding

    private val viewModel: WaterViewModel by viewModels()

    private val timeFormatter = SimpleDateFormat("kk:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DialogAddWaterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.addTime = Calendar.getInstance().apply {
            timeInMillis = arguments?.getLong(ARG_DATE, viewModel.addTime.timeInMillis) ?: viewModel.addTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, viewModel.addTime[Calendar.HOUR_OF_DAY])
            set(Calendar.MINUTE, viewModel.addTime[Calendar.MINUTE])
        }

        waterValuesInit()

        with(binding) {
            onClickListener = this@AddWaterDialog
            onLongClickListener = this@AddWaterDialog
            lifecycleOwner = viewLifecycleOwner

            toolbar.inflateMenu(R.menu.menu_dialog_water)
            toolbar.setNavigationOnClickListener {
                hideKeyboard()
                dismiss()
            }
            toolbar.setOnMenuItemClickListener {
                addWater(edWater.text.toString())
                true
            }

            edTime.setText(timeFormatter.format(viewModel.addTime.time))
            edTime.setOnClickListener {
                hideKeyboard()
                TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            viewModel.addTime[Calendar.HOUR_OF_DAY] = hourOfDay
                            viewModel.addTime[Calendar.MINUTE] = minute
                            binding.edTime.setText(timeFormatter.format(viewModel.addTime.time))
                        },
                        viewModel.addTime[Calendar.HOUR_OF_DAY],
                        viewModel.addTime[Calendar.MINUTE], true
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }

    override fun onClick(v: View) = with(binding) {
        val amount = when (v.id) {
            R.id.card_water_1 -> txtWater1.text.toString()
            R.id.card_water_2 -> txtWater2.text.toString()
            R.id.card_water_3 -> txtWater3.text.toString()
            R.id.card_water_4 -> txtWater4.text.toString()
            else -> ""
        }
        addWater(amount)
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.card_water_1 -> showDialog(0)
            R.id.card_water_2 -> showDialog(1)
            R.id.card_water_3 -> showDialog(2)
            R.id.card_water_4 -> showDialog(3)
        }
        return false
    }

    override fun onResume() {
        super.onResume()

        activity?.showKeyboard(binding.edWater)
    }

    private fun addWater(amount: String) {
        if (amount.isEmpty()
                || amount.startsWith("0")) {
            Toast.makeText(activity, resources.getString(R.string.message_dialog_water), Toast.LENGTH_LONG).show()
        } else {
            viewModel.addWater(amount.toInt())
            hideKeyboard()
            dismiss()
        }
    }

    private fun hideKeyboard() {
        activity?.hideKeyboard(binding.root.findFocus())
    }

    private fun waterValuesInit() {
        val servingKeys = arrayOf(
                PREF_WATER_SERVING_1,
                PREF_WATER_SERVING_2,
                PREF_WATER_SERVING_3,
                PREF_WATER_SERVING_4)
        val txtViews = arrayOf(binding.txtWater1, binding.txtWater2, binding.txtWater3, binding.txtWater4)
        for (i in servingKeys.indices) {
            val value = PreferenceHelper.getValue(servingKeys[i], Int::class.javaObjectType, 0)
            if (value != 0) {
                txtViews[i].text = value.toString()
            }
        }
    }

    private fun showDialog(cardId: Int) {
        AlertDialog.Builder(requireActivity(), R.style.DialogStyle).apply {
            val root = requireActivity().layoutInflater.inflate(R.layout.dialog_water_serving, null)
            setView(root)
            setTitle(getString(R.string.dialog_title_water_serving))

            val edWater: TextInputEditText = root.findViewById(R.id.ed_water_serving)
            edWater.requestFocus()

            setPositiveButton(R.string.dialog_change) { _, _ ->
                if (edWater.text.toString().isEmpty() || edWater.text.toString().startsWith("0")) {
                    SnackbarUtils.showSnackbar(view, getString(R.string.message_dialog_water))
                } else {
                    when (cardId) {
                        0 -> {
                            binding.txtWater1.text = edWater.text.toString()
                            PreferenceHelper.setValue(PREF_WATER_SERVING_1, edWater.text.toString().toInt())
                        }
                        1 -> {
                            binding.txtWater2.text = edWater.text.toString()
                            PreferenceHelper.setValue(PREF_WATER_SERVING_2, edWater.text.toString().toInt())
                        }
                        2 -> {
                            binding.txtWater3.text = edWater.text.toString()
                            PreferenceHelper.setValue(PREF_WATER_SERVING_3, edWater.text.toString().toInt())
                        }
                        3 -> {
                            binding.txtWater4.text = edWater.text.toString()
                            PreferenceHelper.setValue(PREF_WATER_SERVING_4, edWater.text.toString().toInt())
                        }
                    }
                }
            }

            setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        }.create().show()
    }

    companion object {
        const val ARG_DATE = "date"
    }
}