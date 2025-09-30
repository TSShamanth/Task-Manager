package com.example.taskmanager.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.taskmanager.data.Task
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: Task) {
        task.reminder?.let {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("taskId", task.id)
                putExtra("title", task.title)
                putExtra("description", task.description)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                it,
                pendingIntent
            )
        }
    }

    fun cancel(task: Task) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", task.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}