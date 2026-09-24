package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.receiver.GeofenceBroadcastReceiver
import com.example.service.LocationTrackerService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

data class GeofenceArea(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 150f,
    val icon: String = "location"
)

object LocationReminderManager {

    private const val TAG = "LocationReminderManager"
    private const val PREFS_NAME = "antimager_location_prefs"
    private const val KEY_GEOFENCING_ACTIVE = "key_geofencing_active"

    val PRESET_LOCATIONS = listOf(
        GeofenceArea(
            id = "sekolah",
            name = "Sekolah",
            description = "Simpan posisi sekolah saat kamu sedang berada di sana",
            radiusMeters = 180f
        ),
        GeofenceArea(
            id = "rumah",
            name = "Rumah",
            description = "Simpan posisi rumah dari GPS HP",
            radiusMeters = 140f
        ),
        GeofenceArea(
            id = "indomaret",
            name = "Indomaret",
            description = "Simpan minimarket yang biasa kamu datangi",
            radiusMeters = 100f
        ),
        GeofenceArea(
            id = "perpustakaan",
            name = "Perpustakaan",
            description = "Simpan posisi perpustakaan yang ingin dipakai",
            radiusMeters = 130f
        )
    )

    fun isGeofencingActive(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_GEOFENCING_ACTIVE, false)
    }

    private fun setGeofencingActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(KEY_GEOFENCING_ACTIVE, active).apply()
    }

    fun getConfiguredAreas(context: Context): List<GeofenceArea> {
        val preferences = prefs(context)
        return PRESET_LOCATIONS.map { preset ->
            val lat = preferences.getString("${preset.id}_lat", null)?.toDoubleOrNull()
            val lng = preferences.getString("${preset.id}_lng", null)?.toDoubleOrNull()
            preset.copy(latitude = lat, longitude = lng)
        }
    }

    fun getAreaForName(context: Context, locationName: String?): GeofenceArea? {
        if (locationName.isNullOrBlank()) return null
        return getConfiguredAreas(context).firstOrNull { area ->
            area.name.equals(locationName, ignoreCase = true) ||
                area.name.contains(locationName, ignoreCase = true) ||
                locationName.contains(area.name, ignoreCase = true)
        }
    }

    fun isAreaConfigured(context: Context, areaId: String): Boolean {
        return getConfiguredAreas(context).any {
            it.id == areaId && it.latitude != null && it.longitude != null
        }
    }

    @SuppressLint("MissingPermission")
    fun saveCurrentLocationForArea(
        context: Context,
        areaId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onResult(false, "Izin lokasi presisi belum diberikan.")
            return
        }

        val preset = PRESET_LOCATIONS.firstOrNull { it.id == areaId }
        if (preset == null) {
            onResult(false, "Area tidak ditemukan.")
            return
        }

        val tokenSource = CancellationTokenSource()
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    onResult(false, "Lokasi belum terbaca. Aktifkan GPS lalu coba lagi.")
                    return@addOnSuccessListener
                }

                prefs(context).edit()
                    .putString("${areaId}_lat", location.latitude.toString())
                    .putString("${areaId}_lng", location.longitude.toString())
                    .apply()

                onResult(
                    true,
                    "${preset.name} disimpan dari lokasi HP saat ini."
                )
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to save current location for $areaId", error)
                onResult(false, "Gagal membaca GPS: ${error.localizedMessage ?: "coba lagi"}")
            }
    }

    @SuppressLint("MissingPermission")
    fun startRealBackgroundGeofencing(context: Context, onResult: (Boolean, String) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            setGeofencingActive(context, false)
            onResult(false, "Izin lokasi presisi diperlukan.")
            return
        }

        val configured = getConfiguredAreas(context)
            .filter { it.latitude != null && it.longitude != null }

        if (configured.isEmpty()) {
            setGeofencingActive(context, false)
            onResult(false, "Belum ada lokasi yang disimpan. Set lokasi Rumah/Sekolah dulu.")
            return
        }

        val hasBackgroundPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasBackgroundPermission) {
            try {
                LocationTrackerService.startService(context)
                setGeofencingActive(context, true)
                onResult(
                    true,
                    "Pemantau lokasi aktif. Untuk geofence penuh saat app tertutup, izinkan lokasi 'Selalu' di pengaturan Android."
                )
            } catch (e: Exception) {
                setGeofencingActive(context, false)
                onResult(false, "Gagal memulai pemantau lokasi: ${e.localizedMessage}")
            }
            return
        }

        try {
            val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
            val geofenceList = configured.map { area ->
                Geofence.Builder()
                    .setRequestId(area.id)
                    .setCircularRegion(area.latitude!!, area.longitude!!, area.radiusMeters)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                    )
                    .build()
            }

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent(context))
                .addOnSuccessListener {
                    setGeofencingActive(context, true)
                    try {
                        LocationTrackerService.stopService(context)
                    } catch (_: Exception) {
                    }
                    onResult(true, "Geofence aktif untuk ${geofenceList.size} lokasi tersimpan.")
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "System geofencing failed, using foreground fallback", error)
                    try {
                        LocationTrackerService.startService(context)
                        setGeofencingActive(context, true)
                        onResult(true, "Geofence sistem gagal, pemantau lokasi cadangan aktif.")
                    } catch (fallbackError: Exception) {
                        setGeofencingActive(context, false)
                        onResult(false, "Gagal mengaktifkan lokasi: ${fallbackError.localizedMessage}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting geofences", e)
            setGeofencingActive(context, false)
            onResult(false, "Gagal mengaktifkan geofence: ${e.localizedMessage}")
        }
    }

    fun refreshGeofencesIfActive(context: Context) {
        if (!isGeofencingActive(context)) return
        startRealBackgroundGeofencing(context) { _, _ -> }
    }

    fun stopRealBackgroundGeofencing(context: Context, onResult: (Boolean, String) -> Unit) {
        try {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            geofencingClient.removeGeofences(geofencePendingIntent(context))
            LocationTrackerService.stopService(context)
            setGeofencingActive(context, false)
            onResult(true, "Pemantau lokasi dimatikan.")
        } catch (e: Exception) {
            setGeofencingActive(context, false)
            onResult(false, "Gagal mematikan lokasi: ${e.localizedMessage}")
        }
    }

    fun simulateAreaEvent(
        context: Context,
        locationName: String,
        isEnter: Boolean,
        onResult: (String) -> Unit
    ) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val database = com.example.data.local.AppDatabase.getInstance(context)
            val trigger = if (isEnter) "ENTER" else "EXIT"
            val matchingTasks = database.taskDao().getTasksWithLocation().filter {
                val nameMatches =
                    it.locationName?.equals(locationName, ignoreCase = true) == true ||
                        locationName.contains(it.locationName ?: "", ignoreCase = true) ||
                        (it.locationName?.contains(locationName, ignoreCase = true) == true)
                val triggerMatches = (it.locationTrigger ?: "ENTER").equals(trigger, ignoreCase = true)
                nameMatches && triggerMatches
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (matchingTasks.isNotEmpty()) {
                    NotificationHelper.showLocationAlertNotification(
                        context = context,
                        locationName = locationName,
                        taskCount = matchingTasks.size,
                        isEnter = isEnter
                    )
                    NotificationHelper.showPersistentReminderNotification(context, matchingTasks.first())
                    onResult("Event $trigger $locationName: ${matchingTasks.size} tugas ditemukan.")
                } else {
                    onResult("Tidak ada tugas aktif untuk event $trigger di $locationName.")
                }
            }
        }
    }

    private fun geofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, 7001, intent, flags)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
