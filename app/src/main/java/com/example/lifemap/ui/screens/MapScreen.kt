package com.example.lifemap.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.lifemap.R
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.Green2
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MemoryViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // LEGGIAMO LO STATO DAL VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()

    // Variabili per l'interfaccia (mostrare/nascondere)
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Variabili per i permessi e la mappa
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val startingLocation = LatLng(44.1391, 12.2432)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 14f)
    }
    val mapProperties = remember(hasLocationPermission) {
        MapProperties(isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }
    val uiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            Marker(
                state = rememberUpdatedMarkerState(position = startingLocation),
                title = "Piazza del Popolo",
                snippet = "Il nostro primo pin di prova!"
            )
        }

        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    val fineLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineLocationPermission) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val userLatLng = LatLng(it.latitude, it.longitude)
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                                    )
                                }
                            }
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 154.dp, end = 16.dp),
                containerColor = Green2,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centra GPS")
            }

            FloatingActionButton(
                onClick = {
                    val fineLocationPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineLocationPermission) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(
                                            it.latitude,
                                            it.longitude,
                                            1
                                        ) { addresses ->
                                            val addressFound =
                                                addresses.firstOrNull()?.getAddressLine(0)
                                                    ?: "Indirizzo sconosciuto"
                                            viewModel.updateLocation(
                                                it.latitude,
                                                it.longitude,
                                                addressFound
                                            )
                                            showBottomSheet = true
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val addresses =
                                            geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                        val addressFound =
                                            addresses?.firstOrNull()?.getAddressLine(0)
                                                ?: "Indirizzo sconosciuto"
                                        viewModel.updateLocation(
                                            it.latitude,
                                            it.longitude,
                                            addressFound
                                        )
                                        showBottomSheet = true
                                    }
                                } catch (e: Exception) {
                                    viewModel.updateLocation(
                                        it.latitude,
                                        it.longitude,
                                        "Indirizzo non disponibile"
                                    )
                                    showBottomSheet = true
                                }
                            }
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 154.dp, start = 16.dp),
                containerColor = Green2,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Ricordo")
            }
        }

        // BOTTOM SHEET
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                // Usiamo il colore 'surface' del tema: sarà bianco di giorno, scuro di notte
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // CHIP DELL'INDIRIZZO
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Posizione",
                                tint = Green2, // Manteniamo il tuo verde come accento
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.address,
                                // Colore del testo dinamico principale
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Nuovo ricordo",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        val currentTime = remember {
                            SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(Date())
                        }
                        Text(
                            text = "Oggi • $currentTime",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // CAMPO TITOLO
                    MemoryFormField(
                        label = "TITOLO",
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = "Dai un nome a questo momento"
                    )

                    // CAMPO DESCRIZIONE
                    MemoryFormField(
                        label = "NOTE",
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = "Cosa rende speciale questo posto?",
                        modifier = Modifier.height(100.dp),
                        singleLine = false
                    )

                    // CATEGORIA
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CATEGORIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
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

                            // Menu a tendina
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                MemoryCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name)},
                                        onClick = {
                                            viewModel.updateCategory(category)
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // PULSANTE SALVA
                    OutlinedButton(
                        onClick = {
                            viewModel.saveMemory()
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salva ricordo", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// FUNZIONI DI SUPPORTO PER IL DESIGN
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
                    // Testo dell'etichetta dinamico (es. grigio scuro di giorno)
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    modifier = modifier.fillMaxWidth(),
                    singleLine = singleLine,
                    shape = RoundedCornerShape(12.dp),
                    // CHIAMIAMO LA NUOVA FUNZIONE
                    colors = dynamicTextFieldColors()
                )
            }
        }

        @Composable
        fun dynamicTextFieldColors() = OutlinedTextFieldDefaults.colors(
            // Bordo quando ci clicchi (puoi usare il tuo Green2 se preferisci)
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            // Bordo normale
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            // Testo dinamico
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
