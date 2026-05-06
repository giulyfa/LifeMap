package com.example.lifemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lifemap.ui.theme.LifeMapTheme
import com.example.lifemap.ui.theme.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeMapTheme {
                // 1. Creiamo il controller che gestisce gli spostamenti
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 2. Il NavHost "ascolta" il controller e mostra la schermata giusta
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding) // Fondamentale!
                    ) {
                        composable(Screen.Login.route) { LoginScreen(navController) }
                        composable(Screen.Map.route) { MapScreen(navController) }
                        composable(Screen.List.route) { ListScreen(navController) }
                        composable(Screen.Profile.route) { ProfileScreen(navController) }
                        composable(Screen.Settings.route) { SettingsScreen(navController) }

                        // 3. Gestione della rotta dinamica per il dettaglio
                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val memoryId = backStackEntry.arguments?.getInt("memoryId")
                            DetailScreen(memoryId, navController)
                        }
                    }
                }
            }
        }
    }
}