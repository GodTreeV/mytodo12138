package com.app.mytodo

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Date
import java.util.Locale

object DateAndTimePickers {
    /**
     * Default format pattern
     */
    private const val PATTERN = "yyyy/MM/dd HH:mm:ss"

    fun showTimePickerDialog(
        fragmentActivity: FragmentActivity,
        dialogTitle: String = "Select time",
        confirmText: String = "OK",
        cancelText: String = "Cancel",
        result: (Int, Int) -> Unit
    ): MaterialTimePicker {
        val c = Calendar.getInstance()
        return MaterialTimePicker.Builder()
            .setTimeFormat(
                if (android.text.format.DateFormat.is24HourFormat(fragmentActivity))
                    TimeFormat.CLOCK_24H
                else
                    TimeFormat.CLOCK_12H
            )
            .setHour(c[Calendar.HOUR_OF_DAY])
            .setMinute(c[Calendar.MINUTE])
            .setTitleText(dialogTitle)
            .setPositiveButtonText(confirmText)
            .setNegativeButtonText(cancelText)
            .build().also { p ->
                p.addOnPositiveButtonClickListener {
                    result(p.hour, p.minute)
                }
                p.show(fragmentActivity.supportFragmentManager, "TimePicker")
            }
    }

    fun showDatePickerDialog(
        fragmentActivity: FragmentActivity,
        result: (Long) -> Unit
    ): MaterialDatePicker<Long> {
        return MaterialDatePicker.Builder.datePicker()
            .build().also {
                it.addOnPositiveButtonClickListener {
                    result(it)
                }
                it.show(fragmentActivity.supportFragmentManager, "DatePicker")
            }
    }

    /**
     * Get the format time value
     *
     * @param timeStr   eg.2023/01/30/ 13:03:00
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getTimestamp(timeStr: String, pattern: String = PATTERN): Long {
        return SimpleDateFormat(pattern, Locale.getDefault()).parse(timeStr)!!.time
    }

    /**
     * Get format time string from time value
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getFormatTimestamp(time: Long, pattern: String = PATTERN): String {
        return SimpleDateFormat(pattern).format(Date(time))
    }
}