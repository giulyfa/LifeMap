package com.example.lifemap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifemap.ui.DetailScreen
import com.example.lifemap.ui.ListScreen
import com.example.lifemap.ui.LoginScreen
import com.example.lifemap.ui.MapScreen
import com.example.lifemap.ui.ProfileScreen
import com.example.lifemap.ui.Screen
import com.example.lifemap.ui.SettingsScreen
import com.example.lifemap.ui.MemoryViewModel

@Composable
fun LifeMapNavGraph(
    navController: NavHostController,
    viewModel: MemoryViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }

        composable(Screen.Map.route) { MapScreen(navController, viewModel) }

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