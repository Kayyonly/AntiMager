package com.example.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.receiver.GeofenceBroadcastReceiver
import com.example.service.LocationTrackerService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeofenceArea(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 200f,
    val icon: String = "location"
)

object LocationReminderManager {

    private const val TAG = "LocationReminderManager"
    private const val PREFS_NAME = "antimager_location_prefs"
    private const val KEY_GEOFENCING_ACTIVE = "key_geofencing_active"

    val PRESET_LOCATIONS = listOf(
        GeofenceArea(
            id = "sekolah",
            name = "Sekolah / Kampus",
            description = "Area kegiatan belajar & kelas",
            latitude = -6.200000,
            longitude = 106.816666,
            radiusMeters = 250f
        ),
        GeofenceArea(
            id = "rumah",
            name = "Rumah / Kost",
            description = "Tempat istirahat & nugas santai",
            latitude = -6.210000,
            longitude = 106.820000,
            radiusMeters = 150f
        ),
        GeofenceArea(
            id = "minimarket",
            name = "Indomaret / Minimarket",
            description = "Beli perlengkapan, jajan & pulsa",
            latitude = -6.205000,
            longitude = 106.818000,
            radiusMeters = 100f
        ),
        GeofenceArea(
            id = "perpus",
            name = "Perpustakaan",
            description = "Zona hening buat cicil tugas berat",
            latitude = -6.208000,
            longitude = 106.822000,
            radiusMeters = 150f
        )
    )

    fun isGeofencingActive(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GEOFENCING_ACTIVE, false)
    }

    fun setGeofencingActive(context: Context, active: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_GEOFENCING_ACTIVE, active)
            .apply()
    }

    /**
     * Mendaftarkan Geofence asli ke Android System GeofencingClient & Memulai Pemantau Background
     */
    @SuppressLint("MissingPermission")
    fun startRealBackgroundGeofencing(context: Context, onResult: (Boolean, String) -> Unit) {
        try {
            val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

            // 1. Buat daftar geofence dari preset
            val geofenceList = PRESET_LOCATIONS.map { area ->
                Geofence.Builder()
                    .setRequestId(area.id)
                    .setCircularRegion(area.latitude, area.longitude, area.radiusMeters)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            }

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()

            val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
                action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 7001, intent, flags)

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Real Android Geofencing registered successfully for ${geofenceList.size} areas")
                    setGeofencingActive(context, true)
                    // Start complementary background tracker service
                    try {
                        LocationTrackerService.startService(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Foreground service start exception", e)
                    }
                    onResult(true, "Geofencing latar belakang aktif untuk ${geofenceList.size} area!")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to register system geofences", e)
                    // Even if Play Services geofence fails (e.g. play services outdated), start tracker service
                    try {
                        LocationTrackerService.startService(context)
                        setGeofencingActive(context, true)
                        onResult(true, "Pemantau lokasi aktif via Service Mandiri.")
                    } catch (ex: Exception) {
                        onResult(false, "Gagal mengaktifkan geofence: ${e.localizedMessage}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting geofences", e)
            onResult(false, "Error: ${e.localizedMessage}")
        }
    }

    /**
     * Mematikan Geofencing Background
     */
    fun stopRealBackgroundGeofencing(context: Context, onResult: (Boolean, String) -> Unit) {
        try {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 7001, intent, flags)

            geofencingClient.removeGeofences(pendingIntent)
            LocationTrackerService.stopService(context)
            setGeofencingActive(context, false)
            onResult(true, "Geofencing background telah dimatikan.")
        } catch (e: Exception) {
            onResult(false, "Gagal mematikan: ${e.localizedMessage}")
        }
    }

    /**
     * Simulasi Trigger Masuk Area (Fitur pengujian instan di emulator/demo)
     */
    fun simulateAreaEvent(context: Context, locationName: String, isEnter: Boolean, onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(context)
            val matchingTasks = database.taskDao().getTasksWithLocation().filter {
                it.locationName?.contains(locationName, ignoreCase = true) == true ||
                locationName.contains(it.locationName ?: "", ignoreCase = true)
            }

            withContext(Dispatchers.Main) {
                if (isEnter) {
                    if (matchingTasks.isNotEmpty()) {
                        NotificationHelper.showLocationAlertNotification(
                            context = context,
                            locationName = locationName,
                            taskCount = matchingTasks.size
                        )
                        val firstTask = matchingTasks.first()
                        NotificationHelper.showPersistentReminderNotification(context, firstTask)
                        onResult("📍 Masuk area $locationName! Ditemukan ${matchingTasks.size} tugas terkait. Notifikasi terkirim.")
                    } else {
                        onResult("📍 Masuk area $locationName, tapi belum ada tugas aktif untuk lokasi ini.")
                    }
                } else {
                    onResult("🚪 Keluar dari area $locationName.")
                }
            }
        }
    }

    /**
     * Memeriksa koordinat GPS terkini apakah berada di dalam radius salah satu geofence
     */
    @SuppressLint("MissingPermission")
    fun checkCurrentLocation(context: Context, onLocationFound: (String?) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    var detectedAreaName: String? = null
                    for (area in PRESET_LOCATIONS) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            location.latitude,
                            location.longitude,
                            area.latitude,
                            area.longitude,
                            results
                        )
                        if (results[0] <= area.radiusMeters) {
                            detectedAreaName = area.name
                            break
                        }
                    }
                    onLocationFound(detectedAreaName)
                } else {
                    onLocationFound(null)
                }
            }.addOnFailureListener {
                onLocationFound(null)
            }
        } catch (e: Exception) {
            onLocationFound(null)
        }
    }
}
