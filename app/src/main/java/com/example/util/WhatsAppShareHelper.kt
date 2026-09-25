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

    fun formatDailySummaryMessage(tasks: List<TaskEntity>): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val todayStr = sdf.format(Date())
        val (completed, pending) = tasks.partition { it.isCompleted }

        return buildString {
            append("📋 *RINGKASAN TUGAS HARIAN ANTI-MAGER*\n")
            append("📅 *$todayStr*\n\n")
            append("📊 Total: ${tasks.size} | Tertunda: ${pending.size} | Selesai: ${completed.size}\n\n")

            if (pending.isNotEmpty()) {
                append("⏳ *Tugas Yang Harus Dibereskan:*\n")
                pending.forEachIndexed { index, t ->
                    val timeSdf = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
                    val timeStr = timeSdf.format(Date(t.deadlineEpochMillis))
                    append("${index + 1}. [ ] *${t.title}* (${t.subject}) - Deadline: $timeStr\n")
                }
                append("\n")
            }

            if (completed.isNotEmpty()) {
                append("✅ *Tugas Sudah Selesai:*\n")
                completed.forEachIndexed { index, t ->
                    append("${index + 1}. [✓] ~${t.title}~ (${t.subject})\n")
                }
                append("\n")
            }

            append("_AntiMager: Bangun dan selesaikan, jangan biarkan prokrastinasi menang!_ 💪⚡")
        }
    }

    fun formatChecklistMessage(tasks: List<TaskEntity>): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val todayStr = sdf.format(Date())

        return buildString {
            append("✅ *CHECKLIST TUGAS AKADEMIS ($todayStr)*\n\n")
            tasks.forEach { t ->
                val status = if (t.isCompleted) "[✓] Selesai" else "[ ] Belum"
                append("$status • *${t.title}* [${t.subject}]\n")
                if (t.description.isNotBlank()) {
                    append("   ↳ ${t.description}\n")
                }
            }
            append("\n_Dibuat dengan AntiMager App_ 📱")
        }
    }

    fun shareTask(context: Context, task: TaskEntity) {
        val message = formatShareMessage(task)
        sendShareIntent(context, message, "Bagikan Pengingat via:")
    }

    fun shareToWhatsApp(context: Context, task: TaskEntity) {
        shareTask(context, task)
    }

    fun shareDailySummary(context: Context, tasks: List<TaskEntity>) {
        val message = formatDailySummaryMessage(tasks)
        sendShareIntent(context, message, "Bagikan Ringkasan Harian:")
    }

    fun shareChecklist(context: Context, tasks: List<TaskEntity>) {
        val message = formatChecklistMessage(tasks)
        sendShareIntent(context, message, "Bagikan Checklist Tugas:")
    }

    private fun sendShareIntent(context: Context, message: String, chooserTitle: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            sendIntent.setPackage("com.whatsapp")
            context.startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            try {
                sendIntent.setPackage(null)
                val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "Tidak ada aplikasi untuk membagikan pesan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
