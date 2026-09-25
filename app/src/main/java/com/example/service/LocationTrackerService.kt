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
            context.stopService(Intent(context, LocationTrackerService::class.java))
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val insideAreaIds = mutableSetOf<String>()
    private val lastEventTime = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach(::checkLocationAgainstAreas)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                30_000L
            )
                .setMinUpdateIntervalMillis(15_000L)
                .setMinUpdateDistanceMeters(25f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Real-device location fallback started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
            stopSelf()
        }
    }

    private fun checkLocationAgainstAreas(currentLocation: Location) {
        val configuredAreas = LocationReminderManager.getConfiguredAreas(applicationContext)
            .filter { it.latitude != null && it.longitude != null }

        if (configuredAreas.isEmpty()) return

        val now = System.currentTimeMillis()
        val currentlyInside = mutableSetOf<String>()

        configuredAreas.forEach { area ->
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                area.latitude!!,
                area.longitude!!,
                results
            )

            if (results[0] <= area.radiusMeters) {
                currentlyInside.add(area.id)
                if (!insideAreaIds.contains(area.id)) {
                    dispatchTransition(area.id, area.name, "ENTER", now)
                }
            } else if (insideAreaIds.contains(area.id)) {
                dispatchTransition(area.id, area.name, "EXIT", now)
            }
        }

        insideAreaIds.clear()
        insideAreaIds.addAll(currentlyInside)
    }

    private fun dispatchTransition(areaId: String, areaName: String, transition: String, now: Long) {
        val eventKey = "$areaId:$transition"
        val last = lastEventTime[eventKey] ?: 0L
        if (now - last < 60_000L) return
        lastEventTime[eventKey] = now

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val matching = db.taskDao().getTasksWithLocation().filter { task ->
                    val nameMatches =
                        task.locationName?.equals(areaName, ignoreCase = true) == true ||
                            areaName.contains(task.locationName ?: "", ignoreCase = true) ||
                            (task.locationName?.contains(areaName, ignoreCase = true) == true)
                    val triggerMatches =
                        (task.locationTrigger ?: "ENTER").equals(transition, ignoreCase = true)
                    nameMatches && triggerMatches
                }

                if (matching.isNotEmpty()) {
                    NotificationHelper.showLocationAlertNotification(
                        context = applicationContext,
                        locationName = areaName,
                        taskCount = matching.size,
                        isEnter = transition == "ENTER"
                    )
                    NotificationHelper.showPersistentReminderNotification(
                        applicationContext,
                        matching.first()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling location transition", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemantau Lokasi AntiMager",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Dipakai saat Android perlu pemantauan lokasi cadangan untuk reminder."
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Reminder lokasi aktif")
            .setContentText("AntiMager memantau lokasi yang kamu simpan.")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
