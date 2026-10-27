package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.L2LifeDatabase
import com.example.shortcut.ShortcutHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object WidgetUpdateHelper {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun updateAllWidgets(context: Context) {
        scope.launch {
            try {
                val db = L2LifeDatabase.getInstance(context)
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now()
                val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                val epochDay = today.toEpochDay()

                // 1. Tasks count
                val allTasks = db.taskDao().observeTasksForToday(startOfDay, endOfDay).first()
                val completedTasks = allTasks.count { it.status == "COMPLETED" }
                val totalTasks = allTasks.size

                // 2. Habits count
                val habitLogs = db.habitDao().observeLogsForDate(epochDay).first()
                val habitsDone = habitLogs.count { it.completed }

                // 3. Focus minutes
                val focusSessions = db.focusDao().observeSessionsForDay(epochDay).first()
                val focusMinutes = focusSessions.sumOf { it.durationMinutes }

                updateDaySummaryWidgets(
                    context = context,
                    completedTasks = completedTasks,
                    totalTasks = totalTasks,
                    habitsDone = habitsDone,
                    focusMinutes = focusMinutes,
                    today = today
                )

                updateQuickCaptureWidgets(context)
            } catch (_: Exception) {
                // Ignore if DB not ready
            }
        }
    }

    private fun updateDaySummaryWidgets(
        context: Context,
        completedTasks: Int,
        totalTasks: Int,
        habitsDone: Int,
        focusMinutes: Int,
        today: LocalDate
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, DaySummaryWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val dateFormatted = today.format(
            DateTimeFormatter.ofPattern("EEE, d MMM", Locale("es", "ES"))
        ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_day_summary)
            views.setTextViewText(R.id.widget_date, dateFormatted)
            views.setTextViewText(R.id.widget_tasks_val, "$completedTasks / $totalTasks")
            views.setTextViewText(R.id.widget_habits_val, habitsDone.toString())
            views.setTextViewText(R.id.widget_focus_val, "${focusMinutes}m")

            // Button "Abrir L2 Life"
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                101,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open, openPendingIntent)

            // Button "+ Captura"
            val captureIntent = Intent(context, MainActivity::class.java).apply {
                action = ShortcutHelper.ACTION_QUICK_TASK
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val capturePendingIntent = PendingIntent.getActivity(
                context,
                102,
                captureIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_quick_capture, capturePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateQuickCaptureWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, QuickCaptureWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_capture)

            // Task Action
            val taskIntent = Intent(context, MainActivity::class.java).apply {
                action = ShortcutHelper.ACTION_QUICK_TASK
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_action_task,
                PendingIntent.getActivity(
                    context,
                    201,
                    taskIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Note Action
            val noteIntent = Intent(context, MainActivity::class.java).apply {
                action = ShortcutHelper.ACTION_QUICK_NOTE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_action_note,
                PendingIntent.getActivity(
                    context,
                    202,
                    noteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Focus Action
            val focusIntent = Intent(context, MainActivity::class.java).apply {
                action = ShortcutHelper.ACTION_FOCUS_TIMER
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_action_focus,
                PendingIntent.getActivity(
                    context,
                    203,
                    focusIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Diary Action
            val diaryIntent = Intent(context, MainActivity::class.java).apply {
                action = ShortcutHelper.ACTION_DIARY_TODAY
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_action_diary,
                PendingIntent.getActivity(
                    context,
                    204,
                    diaryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
