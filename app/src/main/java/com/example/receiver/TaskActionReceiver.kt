package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.L2LifeDatabase
import com.example.notification.NotificationHelper
import com.example.scheduler.ReminderScheduler
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TaskActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.action.COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.example.action.SNOOZE_TASK"
        private const val SNOOZE_DURATION_MILLIS = 15 * 60 * 1000L // 15 minutes
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getStringExtra(NotificationHelper.EXTRA_TASK_ID) ?: return
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, taskId.hashCode())

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val db = L2LifeDatabase.getInstance(context)
                        val now = System.currentTimeMillis()
                        db.taskDao().updateTaskStatus(
                            id = taskId,
                            status = "COMPLETED",
                            completedAt = now,
                            updatedAt = now
                        )
                        NotificationHelper.cancelNotification(context, notificationId)
                        ReminderScheduler.cancelTaskReminder(context, taskId)
                        WidgetUpdateHelper.updateAllWidgets(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE_TASK -> {
                NotificationHelper.cancelNotification(context, notificationId)
                val snoozeTime = System.currentTimeMillis() + SNOOZE_DURATION_MILLIS
                ReminderScheduler.scheduleTaskReminder(context, taskId, snoozeTime)
            }
        }
    }
}
