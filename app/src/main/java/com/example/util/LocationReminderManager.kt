package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.data.local.AppDatabase
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

    /**
     * Simulasi Trigger Masuk Area (Sangat berguna untuk uji coba langsung di Emulator)
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
                        // Also trigger persistent notification for the most urgent task
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
