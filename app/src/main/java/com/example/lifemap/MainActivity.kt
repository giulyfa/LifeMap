package com.example.lifemap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            LifeMapTheme {
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val memoryDao = database.memoryDao()

                // Creiamo il ViewModel usando il Factory che abbiamo scritto
                val viewModel: MemoryViewModel = viewModel(
                    factory = MemoryViewModel.Factory(memoryDao)
                )
                LifeMapApp(viewModel = viewModel)
            }
        }
    }
}