package com.app.mytodo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderWorker(
    private val appContext: Context,
    private val workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {

    private val notificationManager by lazy { appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun doWork(): Result {
        perfromRemindering(inputData)
        return Result.success()
    }

    private fun perfromRemindering(inputData: Data) {
        CoroutineScope(Dispatchers.Main).launch {

            createNotificationChannel()

            val builder = NotificationCompat.Builder(appContext, "ReminderWorker")
                .setSmallIcon(R.drawable.baseline_delete_24)
                .setContentText(inputData.getString("content"))
                .setContentTitle(inputData.getString("title"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (appContext.hasNotificationPermission().not()) {
                log { "No POST_NOTIFICATION permission" }
            } else {
                notificationManager.notify(9527, builder.build())
            }

        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val name = "ReminderWorker Channel"
        val descriptionText = "reminderworker channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("ReminderWorker", name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }
}