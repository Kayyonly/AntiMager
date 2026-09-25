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
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        if (
            transitionType != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transitionType != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            return
        }

        val trigger = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) "ENTER" else "EXIT"
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val areas = LocationReminderManager.getConfiguredAreas(context)
                val db = AppDatabase.getInstance(context)

                for (geofence in triggeringGeofences) {
                    val area = areas.firstOrNull { it.id == geofence.requestId } ?: continue
                    val matchingTasks = db.taskDao().getTasksWithLocation().filter { task ->
                        val nameMatches =
                            task.locationName?.equals(area.name, ignoreCase = true) == true ||
                                area.name.contains(task.locationName ?: "", ignoreCase = true) ||
                                (task.locationName?.contains(area.name, ignoreCase = true) == true)
                        val triggerMatches =
                            (task.locationTrigger ?: "ENTER").equals(trigger, ignoreCase = true)
                        nameMatches && triggerMatches
                    }

                    if (matchingTasks.isNotEmpty()) {
                        NotificationHelper.showLocationAlertNotification(
                            context = context,
                            locationName = area.name,
                            taskCount = matchingTasks.size,
                            isEnter = trigger == "ENTER"
                        )
                        NotificationHelper.showPersistentReminderNotification(
                            context,
                            matchingTasks.first()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling geofence event", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
