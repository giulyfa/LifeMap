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
import androidx.work.*
import com.example.lifemap.ui.MemoryNotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workRequest = PeriodicWorkRequestBuilder<MemoryNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MemoryAnniversaryWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

//        val testWorkRequest = OneTimeWorkRequestBuilder<MemoryNotificationWorker>()
//            .setConstraints(
//                Constraints.Builder()
//                    .setRequiresBatteryNotLow(true)
//                    .build()
//            )
//            .build()
//
//        // Usiamo REPLACE in modo che sovrascriva qualsiasi tentativo precedente bloccato
//        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
//            "MemoryAnniversaryWork_TEST",
//            ExistingWorkPolicy.REPLACE,
//            testWorkRequest
//        )

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            val sharedPreferences = remember {
                context.getSharedPreferences("lifemap_settings", Context.MODE_PRIVATE)
            }

            val isSystemDark = isSystemInDarkTheme()

            var themePreference by remember {
                mutableIntStateOf(sharedPreferences.getInt("theme_pref", 0))
            }

            val isDarkTheme = when (themePreference) {
                1 -> false
                2 -> true
                else -> isSystemDark
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
                        themePreference = themePreference,
                        onThemeToggle = { newPref ->
                            themePreference = newPref
                            if (newPref == 0) {
                                sharedPreferences.edit { remove("theme_pref") }
                            } else {
                                sharedPreferences.edit { putInt("theme_pref", newPref) }
                            }
                        }
                    )
                }
            }
        }
    }
}
