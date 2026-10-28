package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.L2LifeDatabase
import com.example.domain.model.Priority
import com.example.notification.NotificationHelper
import com.example.scheduler.ReminderScheduler
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.action.TASK_REMINDER"
        const val ACTION_DAILY_CHECK = "com.example.action.DAILY_CHECK"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_TASK_REMINDER -> {
                val taskId = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_ID) ?: return
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val db = L2LifeDatabase.getInstance(context)
                        val taskEntity = db.taskDao().getTaskById(taskId)
                        if (taskEntity != null && taskEntity.status != "COMPLETED") {
                            val priority = try {
                                Priority.valueOf(taskEntity.priority)
                            } catch (_: Exception) {
                                Priority.MEDIUM
                            }
                            NotificationHelper.showTaskReminderNotification(
                                context = context,
                                taskId = taskEntity.id,
                                taskTitle = taskEntity.title,
                                taskNotes = taskEntity.description,
                                priority = priority
                            )
                        }
                    } finally {
                        pendingResult?.finish()
                    }
                }
            }

            ACTION_DAILY_CHECK -> {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        WidgetUpdateHelper.updateAllWidgets(context)
                        ReminderScheduler.scheduleDailyMidnightCheck(context)
                    } finally {
                        pendingResult?.finish()
                    }
                }
            }
        }
    }
}
