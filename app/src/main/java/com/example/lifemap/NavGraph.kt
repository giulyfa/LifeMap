package com.example.lifemap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifemap.ui.theme.*

@Composable
fun LifeMapNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
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