package com.example.taskmanager.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: NotificationService

    override fun onReceive(context: Context?, intent: Intent?) {
        val taskId = intent?.getIntExtra("taskId", -1) ?: -1
        val title = intent?.getStringExtra("title") ?: ""
        val description = intent?.getStringExtra("description") ?: ""

        if (taskId != -1) {
            notificationService.showNotification(taskId, title, description)
        }
    }
}