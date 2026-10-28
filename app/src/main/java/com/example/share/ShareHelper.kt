package com.example.share

import android.content.Context
import android.content.Intent
import com.example.domain.model.WeeklyReviewItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ShareHelper {

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareWeeklyReview(context: Context, review: WeeklyReviewItem) {
        val title = "Revisión Semanal L2 Life"
        val sb = StringBuilder()
        sb.appendLine("📊 $title")
        sb.appendLine("─────────────────────────")
        sb.appendLine("• Energía: ${review.energyRating}/5 | Foco: ${review.productivityRating}/5")
        if (review.accomplishments.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("🏆 Logros y avances:")
            sb.appendLine(review.accomplishments)
        }
        if (review.challenges.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("⚠️ Retos y dificultades:")
            sb.appendLine(review.challenges)
        }
        if (review.nextWeekFocus.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("🎯 Prioridades próxima semana:")
            sb.appendLine(review.nextWeekFocus)
        }
        if (review.notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📝 Notas adicionales:")
            sb.appendLine(review.notes)
        }
        sb.appendLine()
        sb.appendLine("— Generado desde L2 Life")

        shareText(context, title, sb.toString())
    }

    fun shareDaySummary(
        context: Context,
        completedTasks: Int,
        totalTasks: Int,
        habitsDone: Int,
        focusMinutes: Int
    ) {
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy"))
        val title = "Resumen del Día ($dateStr) • L2 Life"
        val text = buildString {
            appendLine("🌟 $title")
            appendLine("• Tareas: $completedTasks / $totalTasks completadas")
            appendLine("• Hábitos: $habitsDone completados")
            appendLine("• Tiempo de concentración: $focusMinutes min")
            appendLine("— Enviado desde L2 Life")
        }
        shareText(context, title, text)
    }

    fun shareNote(context: Context, title: String, content: String) {
        val subject = "Nota: $title • L2 Life"
        val text = buildString {
            appendLine("📝 $title")
            appendLine("─────────────────")
            appendLine(content)
            appendLine("— L2 Life")
        }
        shareText(context, subject, text)
    }
}
