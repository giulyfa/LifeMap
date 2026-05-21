package com.example.lifemap.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifemap.data.AppDatabase
import com.example.lifemap.R

class MemoryNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val database = AppDatabase.getDatabase(applicationContext)
        val userDao = database.userDao()
        val memoryDao = database.memoryDao()

        val loggedUser = userDao.getLoggedUser() ?: return Result.success()

        val favorites = memoryDao.getAllFavoriteMemories(loggedUser.email)
        val today = Calendar.getInstance()

        val prefs = applicationContext.getSharedPreferences("lifemap_settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", false)) return Result.success()

        favorites.forEach { memory ->
            val memoryDate = Calendar.getInstance().apply { timeInMillis = memory.date }

            // 1. Controllo Anniversario (stesso giorno e mese, ma anno successivo)
            val isAnniversary = today.get(Calendar.MONTH) == memoryDate.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == memoryDate.get(Calendar.DAY_OF_MONTH) &&
                    today.get(Calendar.YEAR) > memoryDate.get(Calendar.YEAR)

            // 2. Controllo Primo Mese (esattamente un mese dopo la data del ricordo)
            val oneMonthLater = (memoryDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            val isOneMonthLater = today.get(Calendar.YEAR) == oneMonthLater.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == oneMonthLater.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == oneMonthLater.get(Calendar.DAY_OF_MONTH)

            if (isAnniversary) {
                sendNotification(
                    "Anniversario di un ricordo!",
                    "Un anno fa salvavi: ${memory.title}",
                    memory.id
                )
            } else if (isOneMonthLater) {
                sendNotification(
                    "Il tempo vola",
                    "È già passato un mese da: ${memory.title}",
                    memory.id
                )
            }
        }
        return Result.success()
    }

    private fun sendNotification(title: String, message: String, memoryId: Int) {
        val channelId = "memories_anniversary"

        // 1. Creazione del canale di notifica (obbligatorio su Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ricordi e Anniversari",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifiche per gli anniversari dei ricordi salvati"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Costruzione della notifica
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Per dispositivi pre-Oreo
            .setAutoCancel(true)
            .build()

        // 3. Controllo dei permessi di notifica per Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(applicationContext).notify(memoryId, notification)
    }
}
