package com.example.lifemap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifemap.ui.screens.DetailScreen
import com.example.lifemap.ui.screens.ListScreen
import com.example.lifemap.ui.screens.LoginScreen
import com.example.lifemap.ui.screens.map.MapScreen
import com.example.lifemap.ui.screens.ProfileScreen
import com.example.lifemap.ui.screens.RegistrationScreen
import com.example.lifemap.ui.Screen
import com.example.lifemap.ui.screens.SettingsScreen
import com.example.lifemap.ui.MemoryViewModel

@Composable
fun LifeMapNavGraph(
    navController: NavHostController,
    viewModel: MemoryViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }

        composable(Screen.Map.route) { MapScreen(navController, viewModel) }

        composable(Screen.List.route) { ListScreen(
            navController = navController,
            viewModel = viewModel
        ) }

        composable(Screen.Profile.route) { ProfileScreen(navController) }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        composable(
            route = "detail_screen/{memoryId}",
            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getInt("memoryId") ?: return@composable

            DetailScreen(
                memoryId = memoryId,
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screen.Registration.route) { RegistrationScreen(navController = navController)}
    }
}
