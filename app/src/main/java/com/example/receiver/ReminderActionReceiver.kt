package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val database = AppDatabase.getInstance(context)

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                CoroutineScope(Dispatchers.IO).launch {
                    database.taskDao().setTaskCompleted(taskId, true)
                    NotificationHelper.dismissNotification(context, taskId)

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "🎉 Mantap! Tugas ditandai selesai.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ACTION_SNOOZE_TASK -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val task = database.taskDao().getTaskById(taskId)
                    if (task != null) {
                        val fifteenMinutesMillis = 15 * 60 * 1000L
                        val newDeadline = System.currentTimeMillis() + fifteenMinutesMillis
                        database.taskDao().snoozeTask(taskId, newDeadline)

                        // Re-trigger notification with updated snooze info
                        val updatedTask = task.copy(
                            deadlineEpochMillis = newDeadline,
                            snoozeCount = task.snoozeCount + 1
                        )
                        NotificationHelper.showPersistentReminderNotification(context, updatedTask)

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "⏰ Ditunda 15 menit. Awas jangan keterusan ya!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE_TASK = "com.aistudio.antimager.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.aistudio.antimager.ACTION_SNOOZE_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
