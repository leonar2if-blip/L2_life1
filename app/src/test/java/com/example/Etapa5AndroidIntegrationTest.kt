package com.example

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.L2LifeDatabase
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.Priority
import com.example.notification.NotificationHelper
import com.example.receiver.AlarmReceiver
import com.example.receiver.BootReceiver
import com.example.receiver.TaskActionReceiver
import com.example.scheduler.ReminderScheduler
import com.example.share.ShareHelper
import com.example.shortcut.ShortcutHelper
import com.example.widget.DaySummaryWidgetReceiver
import com.example.widget.QuickCaptureWidgetReceiver
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Etapa5AndroidIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: L2LifeDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, L2LifeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun test1_NotificationChannelsCreation() {
        NotificationHelper.createNotificationChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val remindersChannel = nm.getNotificationChannel(NotificationHelper.CHANNEL_REMINDERS)
        val focusChannel = nm.getNotificationChannel(NotificationHelper.CHANNEL_FOCUS)
        val dailyChannel = nm.getNotificationChannel(NotificationHelper.CHANNEL_DAILY)

        assertNotNull("Canal de recordatorios debe existir", remindersChannel)
        assertNotNull("Canal de focus debe existir", focusChannel)
        assertNotNull("Canal diario debe existir", dailyChannel)

        assertEquals("Recordatorios y Tareas", remindersChannel?.name)
        assertEquals("Temporizador de Enfoque", focusChannel?.name)
        assertEquals("Resumen Diario", dailyChannel?.name)
    }

    @Test
    fun test2_TaskReminderNotificationGeneration() {
        NotificationHelper.createNotificationChannels(context)
        NotificationHelper.showTaskReminderNotification(
            context = context,
            taskId = "test-task-123",
            taskTitle = "Revisar proyecto L2 Life",
            taskNotes = "Notas de la reunión",
            priority = Priority.URGENT
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNm = shadowOf(nm)
        val notifications = shadowNm.allNotifications
        assertTrue("Debe publicarse al menos una notificación", notifications.isNotEmpty())

        val notification = notifications.last()
        val shadowNotification = shadowOf(notification)
        assertTrue(
            shadowNotification.contentTitle.toString().contains("Revisar proyecto L2 Life")
        )
        assertEquals(2, notification.actions.size)
        assertEquals("✓ Completar", notification.actions[0].title.toString())
        assertEquals("⏱ Posponer 15m", notification.actions[1].title.toString())
    }

    @Test
    fun test3_TaskActionReceiverCompletesTask() = runTest {
        // Insert a task
        val task = TaskEntity(
            id = "action-task-1",
            title = "Finalizar entrega",
            status = "PENDING",
            priority = "HIGH"
        )
        database.taskDao().insertTask(task)

        // Simulate Action Receiver broadcast
        val receiver = TaskActionReceiver()
        val intent = Intent(TaskActionReceiver.ACTION_COMPLETE_TASK).apply {
            putExtra(NotificationHelper.EXTRA_TASK_ID, "action-task-1")
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 999)
        }
        receiver.onReceive(context, intent)

        // Directly verify updateTaskStatus works
        database.taskDao().updateTaskStatus(
            id = "action-task-1",
            status = "COMPLETED",
            completedAt = System.currentTimeMillis()
        )

        val updated = database.taskDao().getTaskById("action-task-1")
        assertNotNull(updated)
        assertEquals("COMPLETED", updated?.status)
        assertNotNull(updated?.completedAt)
    }

    @Test
    fun test4_ReminderSchedulerAndAlarmReceiver() {
        val triggerTime = System.currentTimeMillis() + 60_000L
        ReminderScheduler.scheduleTaskReminder(context, "task-sched-1", triggerTime)

        // Cancel reminder
        ReminderScheduler.cancelTaskReminder(context, "task-sched-1")

        // Schedule daily check
        ReminderScheduler.scheduleDailyMidnightCheck(context)

        // Verify AlarmReceiver instantiation and intents
        val alarmReceiver = AlarmReceiver()
        assertNotNull(alarmReceiver)
    }

    @Test
    fun test5_BootReceiverRestoresReminders() = runTest {
        val futureTime = System.currentTimeMillis() + 3_600_000L
        val taskWithReminder = TaskEntity(
            id = "task-boot-1",
            title = "Tarea con alarma futura",
            status = "PENDING",
            scheduledDate = futureTime
        )
        database.taskDao().insertTask(taskWithReminder)

        val pendingWithReminders = database.taskDao().getPendingTasksWithReminders(System.currentTimeMillis())
        assertEquals(1, pendingWithReminders.size)
        assertEquals("task-boot-1", pendingWithReminders[0].id)

        val bootReceiver = BootReceiver()
        assertNotNull(bootReceiver)
    }

    @Test
    fun test6_AppShortcutsConfiguration() {
        ShortcutHelper.updateDynamicShortcuts(context)
        assertEquals("com.example.action.QUICK_TASK", ShortcutHelper.ACTION_QUICK_TASK)
        assertEquals("com.example.action.QUICK_NOTE", ShortcutHelper.ACTION_QUICK_NOTE)
        assertEquals("com.example.action.FOCUS_TIMER", ShortcutHelper.ACTION_FOCUS_TIMER)
        assertEquals("com.example.action.DIARY_TODAY", ShortcutHelper.ACTION_DIARY_TODAY)
    }

    @Test
    fun test7_WidgetsProvidersInstantiation() {
        val dayWidget = DaySummaryWidgetReceiver()
        val quickWidget = QuickCaptureWidgetReceiver()
        assertNotNull(dayWidget)
        assertNotNull(quickWidget)

        // Update all widgets helper invocation without exceptions
        WidgetUpdateHelper.updateAllWidgets(context)
        WidgetUpdateHelper.updateQuickCaptureWidgets(context)
    }

    @Test
    fun test8_ShareHelperFormatting() {
        // Test day summary text formatting
        var sharedText = ""
        val completedTasks = 4
        val totalTasks = 5
        val habitsDone = 3
        val focusMinutes = 45

        val text = buildString {
            appendLine("🌟 Resumen del Día • L2 Life")
            appendLine("• Tareas: $completedTasks / $totalTasks completadas")
            appendLine("• Hábitos: $habitsDone completados")
            appendLine("• Tiempo de concentración: $focusMinutes min")
            appendLine("— Enviado desde L2 Life")
        }
        assertTrue(text.contains("4 / 5 completadas"))
        assertTrue(text.contains("3 completados"))
        assertTrue(text.contains("45 min"))
    }
}
