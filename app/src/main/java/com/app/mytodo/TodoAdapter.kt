package com.app.mytodo

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Operation
import androidx.work.WorkManager
import com.app.mytodo.databinding.LayoutItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

open class TodoAdapter(private val activity: AppCompatActivity) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(private val binding: LayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun getBinding() = binding
    }

    protected val asyncListDiffer = object : AsyncListDiffer<Todo>(this, object : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }) {

    }

    private val lock = Any()

    init {
        setHasStableIds(true)
        CoroutineScope(Dispatchers.IO).launch {
            val dao = activity.getDao()
            dao.queryAllAsFlow().flowWithLifecycle(activity.lifecycle, Lifecycle.State.CREATED).onEach {
                readyToSubmitNewList(it)
            }.launchIn(activity.lifecycleScope)
        }
    }

    open fun readyToSubmitNewList(new: List<Todo>) {
        asyncListDiffer.submitList(new.filter { it.done.not() })
    }

    override fun getItemId(position: Int): Long {
        return asyncListDiffer.currentList[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = asyncListDiffer.currentList[position]
        with(holder.getBinding()) {
            title.text = todo.title
            content.text = todo.content
            date.text = DateAndTimePickers.getFormatTimestamp(todo.date)
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = todo.done
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                todo.done = isChecked
                synchronized(lock) {
                    CoroutineScope(Dispatchers.IO).launch {
                        root.context.getDao().update(todo)
                        CoroutineScope(Dispatchers.Main).launch {
                            updateTodoReminderWorker(activity, todo)
                        }
                    }
                }
            }
            delete.setOnClickListener {
                synchronized(lock) {
                    CoroutineScope(Dispatchers.IO).launch {
                        root.context.getDao().delete(todo)
                        CoroutineScope(Dispatchers.Main).launch {
                            updateTodoReminderWorker(activity, todo)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}