package com.example.lifemap.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.ui.CategoryStat
import com.example.lifemap.ui.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─── Palette colori per le categorie ────────────────────────────────────────
private val categoryColors: Map<MemoryCategory, Color> = mapOf(
    MemoryCategory.VIAGGI to Color(0xFF66BB6A), // verde  hue 120°
    MemoryCategory.AMICI  to Color(0xFFF06292), // rosa   hue 340°
    MemoryCategory.CIBO   to Color(0xFFFFA726), // arancione hue 30°
    MemoryCategory.SPORT  to Color(0xFF42A5F5), // blu    hue 210°
    MemoryCategory.VARIE  to Color(0xFFAB47BC)  // viola  hue 270°
)

/** Colore di fallback per categorie non mappate */
private val fallbackColors = listOf(
    Color(0xFF64B5F6), Color(0xFFA5D6A7), Color(0xFFFFCC02),
    Color(0xFFCE93D8), Color(0xFFEF9A9A), Color(0xFF80DEEA)
)

private fun colorForCategory(category: MemoryCategory, index: Int): Color =
    categoryColors[category] ?: fallbackColors[index % fallbackColors.size]

// ─── Screen principale ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card info utente ─────────────────────────────────────────────
            UserInfoCard(
                fname = state.user?.nome ?: "User",
                lname = state.user?.cognome ?: "Sconosciuto",
                email = state.user?.email ?: "—",
            )

            // ── Card statistiche rapide ──────────────────────────────────────
            StatsCard(totalMemories = state.totalMemories)

            // ── Card grafico a torta ─────────────────────────────────────────
            if (state.categoryStats.isNotEmpty()) {
                PieChartCard(stats = state.categoryStats)
            } else {
                EmptyMemoriesCard()
            }
        }
    }
}

// ─── Card info utente ────────────────────────────────────────────────────────
@Composable
private fun UserInfoCard(fname: String, lname: String, email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = fname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = lname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ─── Card statistiche rapide ─────────────────────────────────────────────────
@Composable
private fun StatsCard(totalMemories: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "$totalMemories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (totalMemories == 1) "ricordo salvato" else "ricordi salvati",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Card grafico a torta ─────────────────────────────────────────────────────
@Composable
private fun PieChartCard(stats: List<CategoryStat>) {
    // Animazione di entrata del grafico
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Ricordi per categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Grafico a torta con Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    stats = stats,
                    animationProgress = animationProgress.value,
                    modifier = Modifier.size(200.dp)
                )
            }

            // Legenda
            Legend(stats = stats)
        }
    }
}

// ─── Grafico a torta ─────────────────────────────────────────────────────────
@Composable
private fun PieChart(
    stats: List<CategoryStat>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 48.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(
            x = center.x - radius,
            y = center.y - radius
        )
        val arcSize = Size(radius * 2, radius * 2)

        var startAngle = -90f // parte dall'alto
        val totalSweep = 360f * animationProgress
        var remaining = totalSweep

        stats.forEachIndexed { index, stat ->
            val sweep = minOf(360f * stat.percentage, remaining)
            remaining -= sweep

            drawArc(
                color = colorForCategory(stat.category, index),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

// ─── Legenda ─────────────────────────────────────────────────────────────────
@Composable
private fun Legend(stats: List<CategoryStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEachIndexed { index, stat ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorForCategory(stat.category, index))
                )
                Text(
                    text = stat.category.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${stat.count} (${(stat.percentage * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyMemoriesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = "Nessun ricordo ancora",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Aggiungi il tuo primo ricordo per vedere le statistiche",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}