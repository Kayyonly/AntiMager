package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.AppDatabase
import com.example.util.LocationReminderManager
import com.example.util.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackerService : Service() {

    companion object {
        const val CHANNEL_ID = "antimager_location_tracking"
        const val NOTIFICATION_ID = 9001
        private const val TAG = "LocationTrackerService"

        fun startService(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastNotifiedAreaId: String? = null
    private var lastNotifyTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    checkLocationAgainstGeofences(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30_000L)
                .setMinUpdateIntervalMillis(15_000L)
                .setMinUpdateDistanceMeters(25f)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.d(TAG, "Background location updates started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    private fun checkLocationAgainstGeofences(currentLocation: Location) {
        val now = System.currentTimeMillis()
        for (area in LocationReminderManager.PRESET_LOCATIONS) {
            val distance = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                area.latitude,
                area.longitude,
                distance
            )

            if (distance[0] <= area.radiusMeters) {
                // Inside area! Check if recently notified
                if (lastNotifiedAreaId == area.id && (now - lastNotifyTime) < (15 * 60 * 1000L)) {
                    return // Don't spam notifications within 15 minutes
                }
                lastNotifiedAreaId = area.id
                lastNotifyTime = now

                Log.d(TAG, "User entered ${area.name} (distance: ${distance[0]}m)")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(applicationContext)
                        val matching = db.taskDao().getTasksWithLocation().filter {
                            it.locationName?.contains(area.name, ignoreCase = true) == true ||
                            area.name.contains(it.locationName ?: "", ignoreCase = true)
                        }

                        if (matching.isNotEmpty()) {
                            NotificationHelper.showLocationAlertNotification(
                                context = applicationContext,
                                locationName = area.name,
                                taskCount = matching.size
                            )
                            NotificationHelper.showPersistentReminderNotification(applicationContext, matching.first())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking matching tasks", e)
                    }
                }
                break
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemantau Lokasi Tugas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Memantau lokasi secara pasif di background untuk memicu pengingat tugas"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AntiMager Geofence Aktif 📍")
            .setContentText("Memantau tugas saat kamu tiba di sekolah, rumah, atau minimarket")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location tracking service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
