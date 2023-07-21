package ediger.diarynutrition.settings

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogBackupBinding

class BackupDialog: DialogFragment() {

    private lateinit var binding: DialogBackupBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.DialogStyle).apply {
            binding = DialogBackupBinding.inflate(layoutInflater)
            

        }.create()
    }
}