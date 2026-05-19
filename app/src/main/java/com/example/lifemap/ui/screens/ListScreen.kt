package com.example.lifemap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.TurnedInNot
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifemap.data.Memory
import com.example.lifemap.ui.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListScreen(navController: NavController, viewModel: MemoryViewModel) {
    val memories by viewModel.allMemories.collectAsState()

    val categories = remember(memories) {
        listOf("Tutti") + memories.map { it.category.name }.distinct().sorted()
    }

    var selectedCategory by remember { mutableStateOf("Tutti") }

    val filteredMemories = remember(memories, selectedCategory) {
        if (selectedCategory == "Tutti") {
            memories
        } else {
            memories.filter { it.category.name == selectedCategory }
        }
    }

    val memoriesGroupedByMonth = remember(filteredMemories) {
        filteredMemories.groupBy { formatMonthYear(it.date) }
    }

    // Usiamo il trucco del colore del testo per capire lo stato del tema (Chiaro/Scuro)
    val isDarkTheme = MaterialTheme.colorScheme.onBackground == Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "I tuoi Ricordi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Dinamico: Verde o Gold
                    titleContentColor = if (isDarkTheme) Color.Black else Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = memories.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(text = category, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                // Colore di selezione: usa il secondary invertito o il primary del tema
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = if (isDarkTheme) Color.Black else Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(100),
                            border = null
                        )
                    }
                }
            }

            if (memories.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), // Alzato a 100.dp per stare sopra la BottomBar floating
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    memoriesGroupedByMonth.forEach { (monthYear, memoriesInMonth) ->
                        item(key = monthYear) {
                            Text(
                                text = monthYear,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground, // Dinamico: Bianco o Nero
                                modifier = Modifier
                                    .padding(start = 4.dp, bottom = 4.dp)
                            )
                        }

                        items(memoriesInMonth, key = { it.id }) { memory ->
                            MemoryCard(
                                memory = memory,
                                modifier = Modifier.animateItem(),
                                onClick = { navController.navigate("detail_screen/${memory.id}") },
                                onFavoriteClick = { viewModel.toggleFavorite(memory) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCard(
    memory: Memory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val formattedDate = remember(memory.date) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(memory.date))
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface // Adattivo: crema di giorno, antracite di notte
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = memory.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, // Dinamico: Nero o Bianco
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Badge Categoria Adattivo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = memory.category.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary // Diventa Gold o Verde
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.offset(x = 10.dp, y = (-10).dp)
                ) {
                    Icon(
                        imageVector = if (memory.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Preferito",
                        // Stella oro se selezionata, altrimenti colore neutro del tema
                        tint = if (memory.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            HorizontalDivider(Modifier, thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Icona coerente (Verde o Gold)
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatMonthYear(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.TurnedInNot,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nessun ricordo salvato",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Esplora la mappa e premi il tasto + per fissare il tuo primo momento speciale.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
