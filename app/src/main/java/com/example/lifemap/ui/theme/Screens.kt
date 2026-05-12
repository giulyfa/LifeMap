package com.example.lifemap.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Schermata di Login (Vale fino a 6 punti!)")
        Button(onClick = {
            // Quando l'utente "accede", lo mandiamo alla mappa
            navController.navigate(Screen.Map.route) {
                // Questo trucco impedisce all'utente di tornare al login premendo "Indietro"
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }) {
            Text("Simula Accesso")
        }
    }
}

@Composable
fun MapScreen(navController: NavController) {
    // Impostiamo le coordinate iniziali della mappa (centro città)
    val startingLocation = LatLng(44.1391, 12.2432)

    // Configariamo la telecamera per partire da quelle coordinate con uno zoom adeguato
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startingLocation, 14f)
    }

    // Ecco il componente magico che disegna l'intera mappa a tutto schermo!
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Inseriamo un primo marker di prova
        Marker(
            state = MarkerState(position = startingLocation),
            title = "Piazza del Popolo",
            snippet = "Il nostro primo pin di prova!"
        )
    }
}

@Composable
fun ListScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "⚙️ Impostazioni e Tema Dark")
    }
}

@Composable
fun DetailScreen(memoryId: Int?, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔎 Dettaglio del ricordo numero: $memoryId")
        Button(onClick = { navController.popBackStack() }) { // popBackStack simula la freccia indietro
            Text("Torna Indietro")
        }
    }
}