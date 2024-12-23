package ediger.diarynutrition.settings

import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.DialogBackupBinding

class BackupDialog: DialogFragment() {

    private lateinit var binding: DialogBackupBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.DialogStyle).apply {
            binding = DialogBackupBinding.inflate(layoutInflater).apply {
                btnCheck.setOnClickListener {
                    try {
                        val intent = Intent().apply {
                            component = ComponentName(
                                "com.google.android.gms",
                                "com.google.android.gms.backup.component.BackupSettingsActivity"
                            )
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                       Toast.makeText(requireContext(), R.string.dialog_backup_button_failure, Toast.LENGTH_SHORT)
                           .show()
                    }
                }
            }
            setView(binding.root)
            setTitle(getString(R.string.dialog_backup_title))
            setPositiveButton(R.string.dialog_backup_positive) { dialog, _ ->
                NavHostFragment.findNavController(this@BackupDialog)
                    .previousBackStackEntry?.savedStateHandle?.set(ARG_PERFORM_BACKUP, true)
                dialog.dismiss()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
    }

    companion object {
        const val ARG_PERFORM_BACKUP = "performBackup"
    }

}