package com.example.lifemap.ui

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.lifemap.data.MemoryCategory
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
import androidx.biometric.BiometricPrompt

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Map : Screen("map", "Mappa", Icons.Default.Place)
    object List : Screen("list", "Lista", Icons.AutoMirrored.Filled.List)
    object Profile : Screen("profile", "Profilo", Icons.Default.Person)
    object Settings : Screen("settings", "Impostazioni", Icons.Default.Settings)

    object Detail : Screen("detail/{memoryId}") {
        fun createRoute(memoryId: Int) = "detail/$memoryId"
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Benvenuto su LifeMap",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(Screen.Map.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accedi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("oppure", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Login Google in arrivo!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accedi con Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = {
                authenticateWithBiometrics(context) {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = "Biometria", modifier = Modifier.padding(end = 8.dp))
            Text("Usa Dati Biometrici")
        }
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

private fun authenticateWithBiometrics(context: Context, onSuccess: () -> Unit) {
    // 2. USO DELLA FUNZIONE SICURA
    val activity = context.findFragmentActivity()
    if (activity == null) {
        Toast.makeText(context, "Errore: Impossibile avviare la biometria", Toast.LENGTH_SHORT).show()
        return
    }

    val biometricManager = BiometricManager.from(context)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    when (biometricManager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> { /* Tutto ok */ }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            Toast.makeText(context, "Nessun sensore biometrico", Toast.LENGTH_LONG).show()
            return
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            Toast.makeText(context, "Sensore non disponibile", Toast.LENGTH_LONG).show()
            return
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            Toast.makeText(context, "Nessuna impronta configurata", Toast.LENGTH_LONG).show()
            return
        }
        else -> {
            Toast.makeText(context, "Errore biometrico", Toast.LENGTH_SHORT).show()
            return
        }
    }

    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(context, "Accesso annullato", Toast.LENGTH_SHORT).show()
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context, "Impronta non riconosciuta", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Accesso a LifeMap")
        .setSubtitle("Usa l'impronta o il viso per entrare")
        .setNegativeButtonText("Usa password")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

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

@Composable
fun ListScreen(navController: NavController) {
    Column(
        modifier = Modifier.Companion.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "📜 Feed Cronologico")
        // Simuliamo l'apertura di un ricordo specifico (es. quello con ID 1)
        Button(onClick = { navController.navigate(Screen.Detail.createRoute(1)) }) {
            Text("Apri Dettaglio Ricordo #1")
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier.Companion.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "👤 Pagina Profilo utente")
        Button(onClick = { navController.navigate(Screen.Settings.route) }) {
            Text("Vai alle Impostazioni")
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier.Companion.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "⚙️ Impostazioni e Tema Dark")
    }
}

@Composable
fun DetailScreen(memoryId: Int?, navController: NavController) {
    Column(
        modifier = Modifier.Companion.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "🔎 Dettaglio del ricordo numero: $memoryId")
        Button(onClick = { navController.popBackStack() }) { // popBackStack simula la freccia indietro
            Text("Torna Indietro")
        }
    }
}