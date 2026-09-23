package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.AntiMagerApp
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.TaskEntity
import com.example.receiver.ReminderActionReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    fun showPersistentReminderNotification(context: Context, task: TaskEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Complete Task
        val completeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_COMPLETE_TASK
            putExtra(ReminderActionReceiver.EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id * 10 + 1).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze Task
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE_TASK
            putExtra(ReminderActionReceiver.EXTRA_TASK_ID, task.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
        val deadlineTime = sdf.format(Date(task.deadlineEpochMillis))
        val snoozeNotice = if (task.snoozeCount > 0) " (Ditunda ${task.snoozeCount}x 😴)" else ""

        val notification = NotificationCompat.Builder(context, AntiMagerApp.CHANNEL_PERSISTENT_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🚨 ${task.title}$snoozeNotice")
            .setContentText("Mapel: ${task.subject} • Estimasi: ${task.estimatedMinutes}m • Deadline: $deadlineTime")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "📌 Tugas: ${task.title}\n" +
                        "📚 Mata Pelajaran: ${task.subject}\n" +
                        "⏳ Estimasi Waktu: ${task.estimatedMinutes} menit\n" +
                        "⏰ Deadline: $deadlineTime WIB\n" +
                        (if (!task.locationName.isNullOrBlank()) "📍 Lokasi: ${task.locationName}\n" else "") +
                        "\n💡 Anti-mager: Kerjain sekarang biar bebas kepikiran!"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOngoing(task.isPersistent && !task.isCompleted) // Persistent: cannot be cleared until completed or snoozed
            .setAutoCancel(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "✅ Selesai", completePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Tunda 15 Mnt", snoozePendingIntent)
            .build()

        notificationManager.notify(task.id.toInt(), notification)
    }

    fun showLocationAlertNotification(context: Context, locationName: String, taskCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AntiMagerApp.CHANNEL_LOCATION_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("📍 Kamu sampai di $locationName!")
            .setContentText("Ada $taskCount tugas yang perlu kamu kerjakan di sini. Buka sekarang!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        notificationManager.notify(8888, notification)
    }

    fun dismissNotification(context: Context, taskId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.toInt())
    }
}
