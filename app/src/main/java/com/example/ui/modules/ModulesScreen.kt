package com.example.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.home.HomeViewModel

data class ModuleItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isReady: Boolean,
    val stageTag: String,
    val countBadge: String? = null,
    val onClickAction: () -> Unit
)

@Composable
fun ModulesScreen(
    homeViewModel: HomeViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToPeople: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()

    val activeModules = listOf(
        ModuleItem(
            title = "Tareas y Pendientes",
            description = "Organización por prioridad, estado y persistencia Room",
            icon = Icons.Filled.FormatListBulleted,
            isReady = true,
            stageTag = "Activo (Etapa 1)",
            countBadge = "${state.summary.totalPendingTasks} pendientes",
            onClickAction = onNavigateToHome
        ),
        ModuleItem(
            title = "Proyectos",
            description = "Agrupación de tareas, hitos y cálculo real de avance",
            icon = Icons.Filled.Folder,
            isReady = true,
            stageTag = "Activo (Etapa 2)",
            countBadge = "En curso",
            onClickAction = onNavigateToProjects
        ),
        ModuleItem(
            title = "Calendario y Tiempo",
            description = "Vistas de día, semana y agenda con plazos y horas reservadas",
            icon = Icons.Filled.CalendarMonth,
            isReady = true,
            stageTag = "Activo (Etapa 2)",
            countBadge = "Día / Semana",
            onClickAction = onNavigateToCalendar
        ),
        ModuleItem(
            title = "Hábitos Positivos y a Reducir",
            description = "Registro diario determinista con un toque y métricas reales",
            icon = Icons.Filled.Repeat,
            isReady = true,
            stageTag = "Activo (Etapa 1)",
            countBadge = "${state.summary.completedHabitsToday}/${state.summary.activeHabitsCount} hoy",
            onClickAction = onNavigateToHome
        ),
        ModuleItem(
            title = "Rutinas Diarias",
            description = "Secuencias matutinas, vespertinas y de trabajo paso a paso",
            icon = Icons.Filled.HourglassBottom,
            isReady = true,
            stageTag = "Activo (Etapa 2)",
            countBadge = "Ejecutable",
            onClickAction = onNavigateToRoutines
        ),
        ModuleItem(
            title = "Objetivos e Hitos",
            description = "Métricas a corto, medio y largo plazo con seguimiento de hitos",
            icon = Icons.Filled.Flag,
            isReady = true,
            stageTag = "Activo (Etapa 3)",
            countBadge = "Estratégico",
            onClickAction = onNavigateToGoals
        ),
        ModuleItem(
            title = "Diario y Reflexión",
            description = "Reflexión diaria con borrador derivado de actividad real",
            icon = Icons.Filled.Book,
            isReady = true,
            stageTag = "Activo (Etapa 3)",
            countBadge = "Resumen real",
            onClickAction = onNavigateToDiary
        ),
        ModuleItem(
            title = "Aprendizaje y Conocimiento",
            description = "Seguimiento de estudio, temas y horas reales dedicadas",
            icon = Icons.Filled.School,
            isReady = true,
            stageTag = "Activo (Etapa 3)",
            countBadge = "Horas reales",
            onClickAction = onNavigateToLearning
        ),
        ModuleItem(
            title = "Personas y Relaciones",
            description = "Vínculos, fechas importantes y próximas acciones",
            icon = Icons.Filled.People,
            isReady = true,
            stageTag = "Activo (Etapa 3)",
            countBadge = "Relaciones",
            onClickAction = onNavigateToPeople
        ),
        ModuleItem(
            title = "Notas e Ideas",
            description = "Captura inmediata con categorías y búsqueda reactiva",
            icon = Icons.Filled.Notes,
            isReady = true,
            stageTag = "Activo (Etapa 1)",
            countBadge = "${state.summary.notesCount} notas",
            onClickAction = onNavigateToHome
        ),
        ModuleItem(
            title = "Bienestar / Hidratación",
            description = "Monitoreo rápido de agua y registros personales persistidos",
            icon = Icons.Filled.LocalDrink,
            isReady = true,
            stageTag = "Activo (Etapa 1)",
            countBadge = "${state.summary.waterIntakeMl} ml",
            onClickAction = onNavigateToHome
        ),
        ModuleItem(
            title = "Estadísticas y Revisiones",
            description = "Análisis de consistencia semanal, ritmo diario y revisiones estructuradas",
            icon = Icons.Filled.BarChart,
            isReady = true,
            stageTag = "Activo (Etapa 4)",
            countBadge = "Métricas reales",
            onClickAction = onNavigateToStats
        ),
        ModuleItem(
            title = "Enfoque / Pomodoro",
            description = "Bloques de trabajo profundo, descansos y vinculación a tareas o temas",
            icon = Icons.Filled.Alarm,
            isReady = true,
            stageTag = "Activo (Etapa 4)",
            countBadge = "Temporizador",
            onClickAction = onNavigateToFocus
        )
    )

    val plannedModules = listOf(
        ModuleItem(
            title = "Copias y Exportación",
            description = "Respaldos JSON restaurables y portabilidad de datos",
            icon = Icons.Filled.Backup,
            isReady = false,
            stageTag = "Etapa 6",
            onClickAction = {}
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Centro de Módulos",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ecosistema integral de L2 Life. Sin simulaciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = "MÓDULOS ACTIVOS (ETAPAS 1 Y 2)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            items(activeModules.size) { index ->
                val mod = activeModules[index]
                ModuleCard(
                    module = mod,
                    onClick = mod.onClickAction
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PRÓXIMAS ETAPAS (ETAPAS 3 A 6)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            items(plannedModules.size) { index ->
                val mod = plannedModules[index]
                ModuleCard(
                    module = mod,
                    onClick = mod.onClickAction
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: ModuleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("module_card_${module.title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (module.isReady) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (module.isReady) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (module.isReady) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = if (module.isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (module.isReady) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = if (module.isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = module.stageTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (module.isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (module.countBadge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = module.countBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
