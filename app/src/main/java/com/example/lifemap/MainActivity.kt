package com.example.lifemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lifemap.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeMapTheme {
                val navController = rememberNavController()

                // Osserviamo dove si trova l'utente per decidere se mostrare la barra
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Lista delle pagine che vogliamo nella barra in basso
                val items = listOf(
                    Screen.Map,
                    Screen.List,
                    Screen.Profile,
                    Screen.Settings
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButtonPosition = FabPosition.Start,
                    // Mostriamo la barra solo se NON siamo nel Login
                    bottomBar = {
                        if (currentDestination?.route != Screen.Login.route) {
                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = {
                                            // Usiamo l'icona che abbiamo definito nella classe Screen
                                            Icon(screen.icon!!, contentDescription = screen.label)
                                        },
                                        label = { Text(screen.label!!) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                // Torna alla pagina iniziale della navigazione per non accumulare stack
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    // Il tasto "+" del mockup (Floating Action Button)
                    floatingActionButton = {
                        if (currentDestination?.route != Screen.Login.route) {
                            FloatingActionButton(onClick = {
                                /* Qui implementeremo l'aggiunta di un ricordo (Fotocamera/GPS) */ },
                                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aggiungi")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) { LoginScreen(navController) }
                        composable(Screen.Map.route) { MapScreen(navController) }
                        composable(Screen.List.route) { ListScreen(navController) }
                        composable(Screen.Profile.route) { ProfileScreen(navController) }
                        composable(Screen.Settings.route) { SettingsScreen(navController) }

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