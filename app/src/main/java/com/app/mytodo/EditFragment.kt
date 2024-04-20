package com.app.mytodo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.app.mytodo.databinding.LayoutEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun FragmentActivity.showEditFragment(editableTodo: Todo? = null) {
    EditFragment(editableTodo).show(supportFragmentManager, EditFragment::class.java.simpleName)
}

class EditFragment(
    private var editableTodo: Todo? = null
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutEditBinding

    private var pickedTime = -1L

    private var needUpdate = false

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

            editableTodo?.let {
                title.setText(it.title)
                content.setText(it.content)
                needUpdate = true
            } ?: run {
                editableTodo = Todo(title = "", content = "")
            }

            picktime.setOnClickListener {
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
                        //date.text = timeStr + "(It's ${tick/3600000}h${(tick % 3600000) / 60000}m from now)"

                        pickedTime = timeLong
                    }
                }
            }
            commit.setOnClickListener {
                if (title.text?.toString()?.isEmpty() == true || content.text?.toString()?.isEmpty() == true) {
                    requireContext().toast { "Must input all information" }
                    return@setOnClickListener
                }

                editableTodo!!.date = pickedTime
                editableTodo!!.done = false
                editableTodo!!.title = title.text!!.toString()
                editableTodo!!.content = content.text!!.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    if (needUpdate) {
                        requireContext().getDao().update(editableTodo!!)
                    } else {
                        requireContext().getDao().insert(editableTodo!!)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        performPostReminderWork(editableTodo!!)
                        pickedTime = -1L
                        requireContext().toast { "Todo saves succeed" }
                        dismiss()
                    }
                }
            }
        }
    }

    private fun performPostReminderWork(newTodo: Todo) {
        updateTodoReminderWorker(requireContext(), newTodo)
    }
}