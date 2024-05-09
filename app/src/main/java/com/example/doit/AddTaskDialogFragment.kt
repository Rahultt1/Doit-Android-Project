package com.example.doit

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText

class AddTaskDialogFragment : DialogFragment() {

    private lateinit var callback: Callback

    interface Callback {
        fun onTaskAdded(task: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_task, null)
        val inputField = view.findViewById<TextInputEditText>(R.id.et_new_task)

        builder.setView(view)
            .setTitle("Add Task")
            .setPositiveButton("Add") { _, _ ->
                val task = inputField.text.toString().trim()
                if (task.isNotEmpty()) {
                    callback.onTaskAdded(task)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }
}
