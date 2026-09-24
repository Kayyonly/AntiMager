package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppShareHelper {

    fun formatShareMessage(task: TaskEntity): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale.forLanguageTag("id-ID"))
        val deadlineStr = sdf.format(Date(task.deadlineEpochMillis))
        val locStr = if (!task.locationName.isNullOrBlank()) "📍 *Lokasi:* ${task.locationName}\n" else ""
        val descStr = if (task.description.isNotBlank()) "📝 *Catatan:* ${task.description}\n" else ""

        return buildString {
            append("🚨 *PENGINGAT ANTI-MAGER* 🚨\n\n")
            append("📌 *Tugas:* ${task.title}\n")
            append("📚 *Mata Pelajaran:* ${task.subject}\n")
            append("⏰ *Deadline:* $deadlineStr WIB\n")
            append("⏳ *Estimasi Pengerjaan:* ${task.estimatedMinutes} Menit\n")
            append(locStr)
            append(descStr)
            if (task.snoozeCount > 0) {
                append("😴 *Status Mager:* Sudah ditunda ${task.snoozeCount}x\n")
            }
            append("\n_Pesan otomatis dari AntiMager: Stop scrolling, gas kerjain sekarang biar santuy nanti malam!_ 🚀🔥")
        }
    }

    fun shareTask(context: Context, task: TaskEntity) {
        shareToWhatsApp(context, task)
    }

    fun shareToWhatsApp(context: Context, task: TaskEntity) {
        val message = formatShareMessage(task)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            sendIntent.setPackage("com.whatsapp")
            context.startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            // Jika WhatsApp tidak terpasang (misal di emulator), buka app chooser umum
            try {
                sendIntent.setPackage(null)
                val chooser = Intent.createChooser(sendIntent, "Bagikan Pengingat via:")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "Tidak ada aplikasi untuk membagikan pesan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
