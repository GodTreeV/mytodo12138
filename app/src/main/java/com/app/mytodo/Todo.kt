package com.app.mytodo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.PermissionChecker
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@Entity(tableName = "table_todo")
data class Todo(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    var title: String,
    var content: String,
    var date: Long = -1L,
    var done: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Todo

        if (id != other.id) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (date != other.date) return false
        if (done != other.done) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + done.hashCode()
        return result
    }

    fun getWorkerTag() = "todo@${id}"
}

@Dao
interface TodoDao {
    @Query("select * from table_todo")
    fun queryAllAsFlow(): Flow<List<Todo>>

    @Query("select * from table_todo where done == 0")
    fun queryAllDoneAsFlow(): Flow<List<Todo>>

    @Update
    fun update(todo: Todo)

    @Delete
    fun delete(todo: Todo)

    @Insert
    fun insert(todo: Todo)
}

@Database(
    entities = [Todo::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDb : RoomDatabase() {
    abstract fun dao(): TodoDao

    companion object {
        private var INSTANCE: TodoDb? = null

        fun getInstance(context: Context) = INSTANCE ?: kotlin.run {
            INSTANCE =
                Room.databaseBuilder(context.applicationContext, TodoDb::class.java, "todo_db")
                    .build()
            INSTANCE!!
        }
    }
}

fun Context.getDao() = TodoDb.getInstance(applicationContext).dao()

fun updateTodoReminderWorker(context: Context, todo: Todo) {
    if (todo.date - System.currentTimeMillis() <= 0L) return
    val wm = WorkManager.getInstance(context.applicationContext)
    wm.cancelAllWorkByTag(todo.getWorkerTag())

    if (todo.done.not()) {
        wm.enqueue(createNewWorkRequestFromTodo(todo))
    }
}

fun createNewWorkRequestFromTodo(todo: Todo): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<ReminderWorker>().apply {
        // need check tick
        setInitialDelay(todo.date - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        setInputData(workDataOf("id" to todo.id, "title" to todo.title, "content" to todo.content))
        addTag(todo.getWorkerTag())
    }.build()
}

fun Context.hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionChecker.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PermissionChecker.PERMISSION_GRANTED
    } else true
}