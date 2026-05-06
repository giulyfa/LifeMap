package com.example.lifemap.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

// 1. Le nostre rotte
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Map : Screen("map")
    object List : Screen("list")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{memoryId}") {
        fun createRoute(memoryId: Int) = "detail/$memoryId"
    }
}

// 2. Le Schermate
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📍 Qui ci sarà la Mappa interattiva!")
        Button(onClick = { navController.navigate(Screen.List.route) }) {
            Text("Vai alla Lista dei ricordi")
        }
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
        Text(text = "👤 Pagina Profilo utente (Vale 2 punti!)")
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