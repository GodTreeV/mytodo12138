package com.app.mytodo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.mytodo.databinding.LayoutEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutEditBinding

    private var pickedTime = -1L

    private val newTodo = Todo(title = "", content = "")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            date.setOnClickListener {
                DateAndTimePickers.showDatePickerDialog(requireActivity()) {
                    val yearMonth = DateAndTimePickers.getFormatTimestamp(it, "yyyy/MM/dd")
                    DateAndTimePickers.showTimePickerDialog(requireActivity()) { h, m ->
                        val hourMin = "${if (h < 10) "0${h}" else "$h"}:${if (m < 10) "0${m}" else "$m"}:00"
                        val timeStr = "$yearMonth $hourMin"
                        val timeLong = DateAndTimePickers.getTimestamp(timeStr)
                        val tick = timeLong - System.currentTimeMillis()
                        if (tick <= 0L) {
                            Toast.makeText(
                                requireContext(),
                                "Please select a future time",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@showTimePickerDialog
                        }
                        date.text = timeStr + "(It's ${tick/3600000}h${(tick % 3600000) / 60000}m from now)"

                        pickedTime = timeLong
                    }
                }
            }
            commit.setOnClickListener {
                if (title.text?.toString()?.isEmpty() == true || content.text?.toString()?.isEmpty() == true || pickedTime == -1L) {
                    Toast.makeText(
                        requireContext(),
                        "Must input all information",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                newTodo.date = pickedTime
                newTodo.done = false
                newTodo.title = title.text!!.toString()
                newTodo.content = title.text!!.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    requireContext().getDao().insert(newTodo)
                    CoroutineScope(Dispatchers.Main).launch {
                        performPostReminderWork(newTodo)
                        pickedTime = -1L
                    }
                }
            }
        }
    }

    private fun performPostReminderWork(newTodo: Todo) {
        updateTodoReminderWorker(requireContext(), newTodo)
    }
}