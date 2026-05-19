package com.example.lifemap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifemap.data.Memory
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.Green2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    memoryId: Int,
    viewModel: MemoryViewModel,
    navController: NavController
) {
    var memory by remember { mutableStateOf<Memory?>(null) }

    LaunchedEffect(memoryId) {
        memory = viewModel.getMemoryById(memoryId)
    }

    // Formattazione data (se il ricordo esiste)
    val formattedDate = remember(memory?.date) {
        memory?.date?.let {
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dettaglio Ricordo",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    memory?.let { currentMemory ->
                        IconButton(onClick = {
                            viewModel.toggleFavorite(currentMemory)
                            memory = currentMemory.copy(isFavorite = !currentMemory.isFavorite)
                        }) {
                            Icon(
                                imageVector = if (currentMemory.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Preferito",
                                tint = if (currentMemory.isFavorite) Color(0xFFFFC107) else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Green2,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        memory?.let { currentMemory ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Sfondo dell'app
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Green2.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMemory.category.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green2
                    )
                }

                // TITOLO
                Text(
                    text = currentMemory.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // BLOCCO METADATI
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // DATA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Green2,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF424242)
                        )
                    }

                    // LUOGO
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = Green2,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = currentMemory.address,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF424242)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // DESCRIZIONE
                if (currentMemory.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Le tue note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFFFFFEF9)
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = currentMemory.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green2)
            }
        }
    }
}
