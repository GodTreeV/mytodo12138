package com.app.mytodo

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity

class DoneTodoAdapter(
    activity: AppCompatActivity
) : TodoAdapter(activity) {

    init {
        listenFlows()
    }

    override fun readyToSubmitNewList(new: List<Todo>) {
        val filtered = new.filter { it.done }
        log { "(DoneTodoAdapter)filtered =  $filtered" }
        asyncListDiffer.submitList(filtered)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder.getBinding()) {
            checkbox.isChecked = true
            checkbox.isEnabled = false
            title.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            content.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            date.setTextColor(Color.GRAY)
            date.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        super.onBindViewHolder(holder, position)
    }
}