package com.example.lifemap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.ui.MemoryViewModel
import com.example.lifemap.ui.theme.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemInDark = isSystemInDarkTheme()

            var isDarkTheme by remember { mutableStateOf(systemInDark) }

            LaunchedEffect(systemInDark) {
                isDarkTheme = systemInDark
            }

            LifeMapTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val database = AppDatabase.getDatabase(context)
                    val memoryDao = database.memoryDao()

                    val viewModel: MemoryViewModel = viewModel(
                        factory = MemoryViewModel.Factory(memoryDao)
                    )

                    LifeMapApp(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}
