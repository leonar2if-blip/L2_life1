package com.example.domain.model

enum class Priority(val labelEs: String, val level: Int) {
    LOW("Baja", 1),
    MEDIUM("Media", 2),
    HIGH("Alta", 3),
    URGENT("Urgente", 4);

    companion object {
        fun fromString(value: String?): Priority =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

enum class TaskStatus(val labelEs: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En progreso"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada");

    companion object {
        fun fromString(value: String?): TaskStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

enum class EnergyLevel(val labelEs: String) {
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta");

    companion object {
        fun fromString(value: String?): EnergyLevel =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

enum class HabitType(val labelEs: String) {
    BOOLEAN("Completar / No"),
    MEASURABLE("Medible");

    companion object {
        fun fromString(value: String?): HabitType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: BOOLEAN
    }
}

enum class HabitFrequency(val labelEs: String) {
    DAILY("Todos los días"),
    WEEKDAYS("Lunes a viernes"),
    WEEKENDS("Fines de semana"),
    CUSTOM("Personalizado");

    companion object {
        fun fromString(value: String?): HabitFrequency =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: DAILY
    }
}

enum class WellnessType(val labelEs: String, val defaultUnit: String) {
    WATER("Agua", "ml"),
    SLEEP("Sueño", "horas"),
    MOOD("Ánimo", "escala"),
    ENERGY("Energía", "escala"),
    EXERCISE("Ejercicio", "min"),
    NOTE("Nota de bienestar", "");

    companion object {
        fun fromString(value: String?): WellnessType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: WATER
    }
}

enum class ProjectStatus(val labelEs: String) {
    ACTIVE("Activo"),
    ON_HOLD("En pausa"),
    COMPLETED("Completado"),
    ARCHIVED("Archivado");

    companion object {
        fun fromString(value: String?): ProjectStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

enum class HomeSectionId(val titleEs: String, val descriptionEs: String) {
    DAILY_SUMMARY("Resumen del día", "Métricas en tiempo real de tareas, hábitos y bienestar"),
    TODAY_TASKS("Tareas de hoy", "Pendientes y prioridades del día actual"),
    TODAY_HABITS("Hábitos de hoy", "Progreso diario y registro rápido con un toque"),
    QUICK_WATER("Registro de agua", "Consumo de hidratación acumulada"),
    RECENT_NOTES("Notas e ideas", "Últimas anotaciones y pensamientos capturados"),
    MODULES_OVERVIEW("Centro de módulos", "Accesos directos a todas las áreas de vida");

    companion object {
        fun defaultSections(): List<HomeSectionId> = listOf(
            DAILY_SUMMARY,
            TODAY_TASKS,
            TODAY_HABITS,
            QUICK_WATER,
            RECENT_NOTES,
            MODULES_OVERVIEW
        )

        fun fromString(value: String?): HomeSectionId? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

enum class TimeOfDaySlot(val labelEs: String) {
    MORNING("Mañana"),
    WORK("Trabajo"),
    AFTERNOON("Tarde"),
    EVENING("Noche"),
    WEEKEND("Fin de semana"),
    CUSTOM("Personalizada");

    companion object {
        fun fromString(value: String?): TimeOfDaySlot =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MORNING
    }
}

enum class CalendarViewMode(val labelEs: String) {
    DAY("Día"),
    WEEK("Semana"),
    AGENDA("Agenda")
}

enum class GoalHorizon(val labelEs: String) {
    SHORT_TERM("Corto Plazo"),
    MEDIUM_TERM("Medio Plazo"),
    LONG_TERM("Largo Plazo");

    companion object {
        fun fromString(value: String?): GoalHorizon =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM_TERM
    }
}

enum class GoalStatus(val labelEs: String) {
    ACTIVE("En curso"),
    PAUSED("En pausa"),
    ACHIEVED("Logrado"),
    ABANDONED("Descartado");

    companion object {
        fun fromString(value: String?): GoalStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

enum class LearningStatus(val labelEs: String) {
    EXPLORING("Por iniciar"),
    IN_PROGRESS("En estudio"),
    MASTERED("Completado");

    companion object {
        fun fromString(value: String?): LearningStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: IN_PROGRESS
    }
}

enum class PersonCategory(val labelEs: String) {
    FAMILY("Familia"),
    FRIENDS("Amigos"),
    WORK("Trabajo"),
    MENTOR("Mentor"),
    OTHER("Contacto");

    companion object {
        fun fromString(value: String?): PersonCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

enum class FocusMode(val labelEs: String, val defaultMinutes: Int) {
    WORK("Enfoque", 25),
    SHORT_BREAK("Descanso Corto", 5),
    LONG_BREAK("Descanso Largo", 15);

    companion object {
        fun fromString(value: String?): FocusMode =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: WORK
    }
}

enum class FocusTimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}
