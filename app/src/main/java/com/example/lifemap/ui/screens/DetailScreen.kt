package com.example.lifemap.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifemap.data.Memory
import com.example.lifemap.ui.MemoryViewModel

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memory?.category?.name ?: "") }, // Mostriamo la categoria in alto
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro")
                    }
                },
                // LE AZIONI IN ALTO A DESTRA
                actions = {
                    memory?.let { currentMemory ->
                        IconButton(onClick = {
                            viewModel.toggleFavorite(currentMemory)
                            memory = currentMemory.copy(isFavorite = !currentMemory.isFavorite)
                        }) {
                            Icon(
                                imageVector = if (currentMemory.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Preferito",
                                tint = if (currentMemory.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        // Se il ricordo è stato caricato, mostriamo i dettagli
        memory?.let { currentMemory ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TITOLO GRANDE
                Text(
                    text = currentMemory.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // INDIRIZZO
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentMemory.address,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // LA DESCRIZIONE
                if (currentMemory.description.isNotBlank()) {
                    Text(
                        text = "Le tue note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentMemory.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
