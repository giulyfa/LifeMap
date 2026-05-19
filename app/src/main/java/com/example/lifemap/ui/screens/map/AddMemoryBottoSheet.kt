package com.example.lifemap.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.ui.MemoryUiState
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.Gold
import com.example.lifemap.ui.theme.Green2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryBottomSheet(
    uiState: MemoryUiState,
    viewModel: MemoryViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFEF9),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Green2.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Posizione",
                        tint = Green2,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.address,
                        color = Color.Black,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // HEADER
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuovo ricordo",
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    IconToggleButton(
                        checked = uiState.isFavorite,
                        onCheckedChange = { viewModel.updateFavorite(it) }
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Preferito",
                            tint = if (uiState.isFavorite) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
                Text(
                    text = "Oggi • $currentTime",
                    color = Color(0xFF555555),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // TITOLO
            MemoryFormField(
                label = "TITOLO",
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = "Dai un nome a questo momento..."
            )

            // DESCRIZIONE
            MemoryFormField(
                label = "NOTE",
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                placeholder = "Cosa rende speciale questo posto?",
                modifier = Modifier.height(100.dp),
                singleLine = false
            )

            // CATEGORIA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CATEGORIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF555555),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.category.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = dynamicTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        containerColor = Color(0xFFFFFEF9)
                    ) {
                        MemoryCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    viewModel.updateCategory(category)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PULSANTE SALVA
            Button(
                onClick = {
                    viewModel.saveMemory()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salva ricordo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@Composable
fun MemoryFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF555555),
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.7f)) },
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            shape = RoundedCornerShape(12.dp),
            colors = dynamicTextFieldColors()
        )
    }
}

@Composable
fun dynamicTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Green2,
    focusedLabelColor = Green2,
    cursorColor = Green2,
    unfocusedBorderColor = Color(0xFFE0E0E0),
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
