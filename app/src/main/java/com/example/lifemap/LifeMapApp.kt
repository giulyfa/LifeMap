package com.example.lifemap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.Screen

@Composable
fun LifeMapApp(viewModel: MemoryViewModel) {
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
        }
    ) { innerPadding ->
        LifeMapNavGraph(
            navController = navController,
            viewModel = viewModel,
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