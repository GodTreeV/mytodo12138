package com.app.mytodo

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val appContext: Context,
    private val workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        perfromRemindering(inputData)
        return Result.success()
    }

    private fun perfromRemindering(inputData: Data) {

    }
}