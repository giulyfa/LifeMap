package com.example.lifemap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifemap.ui.theme.*

@Composable
fun LifeMapApp() {
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
                LifeMapBottomBar(navController, currentDestination, items)
            }
        },
        // Il tasto "+" del mockup (Floating Action Button)
        floatingActionButton = {
            if (currentDestination?.route != Screen.Login.route) {
                FloatingActionButton(onClick = {
                    /* Qui implementeremo l'aggiunta di un ricordo (Fotocamera/GPS) */ },
                    containerColor = Purple2,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi")
                }
            }
        }
    ) { innerPadding ->
        LifeMapNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun LifeMapBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<Screen>
) {
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                label = { Text(screen.label!!) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
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