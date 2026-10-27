package com.example.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

object ShortcutHelper {

    const val ACTION_QUICK_TASK = "com.example.action.QUICK_TASK"
    const val ACTION_QUICK_NOTE = "com.example.action.QUICK_NOTE"
    const val ACTION_FOCUS_TIMER = "com.example.action.FOCUS_TIMER"
    const val ACTION_DIARY_TODAY = "com.example.action.DIARY_TODAY"

    fun updateDynamicShortcuts(context: Context) {
        val taskIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_QUICK_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val taskShortcut = ShortcutInfoCompat.Builder(context, "shortcut_quick_task")
            .setShortLabel(context.getString(R.string.shortcut_task_short))
            .setLongLabel(context.getString(R.string.shortcut_task_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_task))
            .setIntent(taskIntent)
            .build()

        val noteIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_QUICK_NOTE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val noteShortcut = ShortcutInfoCompat.Builder(context, "shortcut_quick_note")
            .setShortLabel(context.getString(R.string.shortcut_note_short))
            .setLongLabel(context.getString(R.string.shortcut_note_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_note))
            .setIntent(noteIntent)
            .build()

        val focusIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_FOCUS_TIMER
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val focusShortcut = ShortcutInfoCompat.Builder(context, "shortcut_focus")
            .setShortLabel(context.getString(R.string.shortcut_focus_short))
            .setLongLabel(context.getString(R.string.shortcut_focus_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_focus))
            .setIntent(focusIntent)
            .build()

        val diaryIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_DIARY_TODAY
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val diaryShortcut = ShortcutInfoCompat.Builder(context, "shortcut_diary")
            .setShortLabel(context.getString(R.string.shortcut_diary_short))
            .setLongLabel(context.getString(R.string.shortcut_diary_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_diary))
            .setIntent(diaryIntent)
            .build()

        try {
            ShortcutManagerCompat.setDynamicShortcuts(
                context,
                listOf(taskShortcut, noteShortcut, focusShortcut, diaryShortcut)
            )
        } catch (_: Exception) {
            // Handled if launcher doesn't support shortcuts
        }
    }
}
