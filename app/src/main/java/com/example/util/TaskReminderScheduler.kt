package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.entity.TaskEntity
import com.example.receiver.TaskReminderReceiver

object TaskReminderScheduler {

    private const val ACTION_TASK_REMINDER = "com.aistudio.antimager.ACTION_TASK_REMINDER"
    const val EXTRA_TASK_ID = "extra_task_id"

    fun schedule(context: Context, task: TaskEntity) {
        if (task.id <= 0L || task.isCompleted) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = task.deadlineEpochMillis.coerceAtLeast(System.currentTimeMillis() + 1_000L)
        val pendingIntent = pendingIntent(context, task.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, taskId: Long) {
        if (taskId <= 0L) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, taskId))
    }

    private fun pendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
