package com.example.lifemap.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MemoryViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val uiState by viewModel.uiState.collectAsState()
    val memories by viewModel.allMemories.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isPickingLocationMode by remember { mutableStateOf(false) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

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
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        )
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
            uiSettings = uiSettings,
            onMapClick = {
                if (isPickingLocationMode) {
                    val targetLocation = cameraPositionState.position.target

                    scope.launch {
                        try {
                            val addresses = geocoder.getFromLocation(targetLocation.latitude, targetLocation.longitude, 1)
                            val addressText = addresses?.firstOrNull()?.getAddressLine(0)
                                ?: "Posizione selezionata"

                            viewModel.updateLocation(targetLocation.latitude, targetLocation.longitude, addressText)

                            isPickingLocationMode = false
                            showBottomSheet = true
                        } catch (e: Exception) {
                            Log.e("MapScreen", "Errore geocoding", e)
                        }
                    }
                }
            }
        ) {
            memories.forEach { memory ->
                Marker(
                    state = rememberUpdatedMarkerState(position = LatLng(memory.latitude, memory.longitude)),
                    title = memory.title,
                    snippet = memory.description,
                    icon = getPinColorForCategory(memory.category),
                    anchor = Offset(0.5f, 1.0f)
                )
            }
        }

        AnimatedVisibility(
            visible = isPickingLocationMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SPOSTA LA MAPPA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Il punto al centro è il luogo del tuo ricordo.\nTocca la mappa per confermare.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        if (isPickingLocationMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Perno",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Ricordo")
            }
        }

        if (showBottomSheet) {
            AddMemoryBottomSheet(
                uiState = uiState,
                viewModel = viewModel,
                sheetState = sheetState,
                onSelectLocationOnMap = {
                    isPickingLocationMode = true
                    showBottomSheet = false
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

fun getPinColorForCategory(category: MemoryCategory): BitmapDescriptor {
    val hue = when (category) {
        MemoryCategory.VIAGGI -> 120f  // verde
        MemoryCategory.AMICI  -> 340f  // rosa
        MemoryCategory.CIBO   -> 30f   // arancione
        MemoryCategory.SPORT  -> 210f  // blu
        MemoryCategory.VARIE  -> 270f  // viola
    }
    return BitmapDescriptorFactory.defaultMarker(hue)
}
