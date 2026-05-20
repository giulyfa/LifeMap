package com.example.lifemap

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.*
import androidx.core.content.edit

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            val sharedPreferences = remember {
                context.getSharedPreferences("lifemap_settings", Context.MODE_PRIVATE)
            }

            val systemInDark = isSystemInDarkTheme()

            var isDarkTheme by remember {
                mutableStateOf(
                    if (sharedPreferences.contains("is_dark_theme")) {
                        sharedPreferences.getBoolean("is_dark_theme", systemInDark)
                    } else {
                        systemInDark
                    }
                )
            }

            LaunchedEffect(systemInDark) {
                if (!sharedPreferences.contains("is_dark_theme")) {
                    isDarkTheme = systemInDark
                }
            }

            LifeMapTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val database = AppDatabase.getDatabase(context)
                    val memoryDao = database.memoryDao()
                    val userDao = database.userDao()

                    val viewModel: MemoryViewModel = viewModel(
                        factory = MemoryViewModel.Factory(memoryDao, userDao)
                    )

                    LifeMapApp(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { targetDark ->
                            isDarkTheme = targetDark
                            sharedPreferences.edit { putBoolean("is_dark_theme", targetDark) }
                        }
                    )
                }
            }
        }
    }
}
