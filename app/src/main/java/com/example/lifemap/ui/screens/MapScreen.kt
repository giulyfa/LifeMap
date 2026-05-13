package com.example.lifemap.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.lifemap.data.MemoryCategory
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.Purple2
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import java.util.Locale

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
    val sheetState = rememberModalBottomSheetState()
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
        MapProperties(isMyLocationEnabled = hasLocationPermission)
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
                    .padding(bottom = 32.dp, end = 16.dp),
                containerColor = Purple2,
                contentColor = Color.Black
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

                                    // SALVIAMO LE COORDINATE E L'INDIRIZZO NEL VIEWMODEL
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(it.latitude, it.longitude, 1) { addresses ->
                                            val addressFound = addresses.firstOrNull()?.getAddressLine(0) ?: "Indirizzo sconosciuto"
                                            viewModel.updateLocation(it.latitude, it.longitude, addressFound)
                                            showBottomSheet = true
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                        val addressFound = addresses?.firstOrNull()?.getAddressLine(0) ?: "Indirizzo sconosciuto"
                                        viewModel.updateLocation(it.latitude, it.longitude, addressFound)
                                        showBottomSheet = true
                                    }
                                } catch (e: Exception) {
                                    viewModel.updateLocation(it.latitude, it.longitude, "Indirizzo non disponibile")
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
                    .padding(bottom = 32.dp, start = 16.dp),
                containerColor = Purple2,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Ricordo")
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nuovo Ricordo", style = MaterialTheme.typography.headlineSmall)

                    // MOSTRIAMO L'INDIRIZZO DAL VIEWMODEL
                    Text(
                        text = uiState.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TITOLO COLLEGATO AL VIEWMODEL
                    TextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Titolo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // DESCRIZIONE COLLEGATA AL VIEWMODEL
                    TextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Descrizione o appunti...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.category.name, // CATEGORIA DAL VIEWMODEL
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            MemoryCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.updateCategory(category) // AGGIORNA LA CATEGORIA NEL VIEWMODEL
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TASTO SALVA CHE CHIAMA IL DATABASE
                    Button(
                        onClick = {
                            viewModel.saveMemory()
                            showBottomSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple2)
                    ) {
                        Text("Salva Ricordo", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}