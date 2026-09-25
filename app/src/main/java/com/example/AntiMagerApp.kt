package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.util.LocationReminderManager
import com.example.util.TaskReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiMagerApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        createNotificationChannels()

        // Reconcile persisted tasks with Android alarms whenever the app process starts.
        CoroutineScope(Dispatchers.IO).launch {
            database.taskDao().getIncompleteTasks().forEach {
                TaskReminderScheduler.schedule(this@AntiMagerApp, it)
            }
            LocationReminderManager.refreshGeofencesIfActive(this@AntiMagerApp)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Persistent reminder channel (High priority, vibration)
            val persistentChannel = NotificationChannel(
                CHANNEL_PERSISTENT_REMINDER,
                "Pengingat Anti-Mager (Persisten)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi tugas mendesak yang muncul terus sampai ditandai selesai"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            // Location reminder channel
            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION_REMINDER,
                "Pengingat Lokasi Otomatis",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi saat kamu tiba atau keluar dari lokasi tertentu (Sekolah, Rumah, dll)"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(persistentChannel)
            notificationManager.createNotificationChannel(locationChannel)
        }
    }

    companion object {
        const val CHANNEL_PERSISTENT_REMINDER = "antimager_persistent_channel"
        const val CHANNEL_LOCATION_REMINDER = "antimager_location_channel"

        lateinit var instance: AntiMagerApp
            private set
    }
}
