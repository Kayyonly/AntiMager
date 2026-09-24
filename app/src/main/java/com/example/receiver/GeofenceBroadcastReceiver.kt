package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.util.LocationReminderManager
import com.example.util.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.aistudio.antimager.ACTION_GEOFENCE_EVENT"
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error code: ${geofencingEvent.errorCode} - $errorMessage")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val fenceId = geofence.requestId
            val area = LocationReminderManager.PRESET_LOCATIONS.firstOrNull { it.id == fenceId }
            val locationName = area?.name ?: fenceId

            Log.d(TAG, "Geofence triggered: $fenceId ($locationName), transition: $transitionType")

            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                // User entered target area in background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val matchingTasks = db.taskDao().getTasksWithLocation().filter { task ->
                            task.locationName?.contains(locationName, ignoreCase = true) == true ||
                            locationName.contains(task.locationName ?: "", ignoreCase = true) ||
                            fenceId.contains(task.locationName ?: "", ignoreCase = true)
                        }

                        if (matchingTasks.isNotEmpty()) {
                            NotificationHelper.showLocationAlertNotification(
                                context = context,
                                locationName = locationName,
                                taskCount = matchingTasks.size
                            )
                            // Show persistent reminder for the top urgent task
                            NotificationHelper.showPersistentReminderNotification(context, matchingTasks.first())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching tasks for geofence", e)
                    }
                }
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "User exited area $locationName")
            }
        }
    }
}
