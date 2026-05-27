package com.example.lifemap

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.Screen
@Composable
fun LifeMapApp(
    viewModel: MemoryViewModel,
    isDarkTheme: Boolean,
    themePreference: Int,
    onThemeToggle: (Int) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    val items = listOf(
        Screen.Map,
        Screen.List,
        Screen.Profile,
        Screen.Settings
    )

    val route = currentDestination?.route ?: ""
    val showBottomBar = route !in listOf(Screen.Login.route, Screen.Registration.route) && !route.startsWith("detail")

    Box(modifier = Modifier.fillMaxSize()) {

        LifeMapNavGraph(
            navController = navController,
            viewModel = viewModel,
            isDarkTheme = isDarkTheme,
            themePreference = themePreference,
            onThemeToggle = onThemeToggle,
            modifier = Modifier.fillMaxSize()
        )

        if (showBottomBar) {
            LifeMapBottomBar(
                navController = navController,
                currentDestination = currentDestination,
                items = items,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun LifeMapBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<Screen>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
        shadowElevation = 20.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { screen ->
                val selected =
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = screen.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            text = screen.label!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
