package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.util.LocationReminderManager
import com.example.util.TaskReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = AppDatabase.getInstance(context).taskDao().getIncompleteTasks()
                tasks.forEach { TaskReminderScheduler.schedule(context, it) }
                LocationReminderManager.refreshGeofencesIfActive(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
