package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.L2LifeDatabase
import com.example.scheduler.ReminderScheduler
import com.example.shortcut.ShortcutHelper
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val db = L2LifeDatabase.getInstance(context)
                    val now = System.currentTimeMillis()
                    val tasks = db.taskDao().getPendingTasksWithReminders(now)

                    for (task in tasks) {
                        val reminder = task.scheduledDate ?: task.dueDate
                        if (reminder != null && reminder > now) {
                            ReminderScheduler.scheduleTaskReminder(context, task.id, reminder)
                        }
                    }

                    ReminderScheduler.scheduleDailyMidnightCheck(context)
                    WidgetUpdateHelper.updateAllWidgets(context)
                    ShortcutHelper.updateDynamicShortcuts(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
