package com.example.lifemap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
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
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(memoryId) {
        memory = viewModel.getMemoryById(memoryId)
    }

    val textColor = MaterialTheme.colorScheme.onBackground

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
                            tint = if (textColor == Color.White) Color.Black else Color.White
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
                                tint = if (currentMemory.isFavorite) Color(0xFFFFC107) else (if (textColor == Color.White) Color.Black else Color.White),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = if (textColor == Color.White) Color.Black else Color.White
                )
            )
        },

        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Elimina",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Elimina Ricordo",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->

        memory?.let { currentMemory ->

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    icon = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    title = {
                        Text(text = "Elimina ricordo")
                    },
                    text = {
                        Text(text = "Sei sicura di voler eliminare definitivamente questo ricordo? L'azione non può essere annullata.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteMemory(currentMemory)
                                navController.popBackStack()
                            }
                        ) {
                            Text("Elimina", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMemory.category.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = currentMemory.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = currentMemory.address,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                if (currentMemory.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Le tue note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = currentMemory.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
