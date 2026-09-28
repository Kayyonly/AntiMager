package com.example.data.service

import android.content.Context
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import com.example.util.LocationReminderManager
import com.example.util.TaskReminderScheduler
import com.example.widget.AntiMagerWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WhatsAppSyncResult(
    val importedCount: Int,
    val message: String
)

object WhatsAppBridgeService {
    private const val PREFS = "antimager_whatsapp_bridge"
    private const val KEY_URL = "bridge_url"
    private const val DEFAULT_TOKEN = "antimager-local"

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    fun getBaseUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URL, "")
            .orEmpty()

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_URL, url.trim().trimEnd('/'))
            .apply()
    }

    suspend fun syncPendingTasks(
        context: Context,
        taskDao: TaskDao
    ): WhatsAppSyncResult = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(context)
        if (baseUrl.isBlank()) {
            return@withContext WhatsAppSyncResult(0, "Bridge belum diatur.")
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl/api/tasks")
                .addHeader("X-AntiMager-Token", DEFAULT_TOKEN)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext WhatsAppSyncResult(0, "Bridge merespons HTTP ${response.code}.")
            }

            val root = JSONObject(response.body?.string().orEmpty())
            val tasks = root.optJSONArray("tasks")
            var imported = 0

            if (tasks != null) {
                for (i in 0 until tasks.length()) {
                    val item = tasks.optJSONObject(i) ?: continue
                    val externalId = item.optString("id").trim()
                    if (externalId.isBlank()) continue

                    if (taskDao.getTaskByExternalId(externalId) != null) {
                        acknowledge(baseUrl, externalId)
                        continue
                    }

                    val deadline = item.optLong("deadlineEpochMillis", 0L)
                    if (deadline <= 0L) continue

                    val priority = item.optString("priority", "MEDIUM")
                        .uppercase()
                        .let { if (it in setOf("HIGH", "MEDIUM", "LOW")) it else "MEDIUM" }

                    val locationName = item.optString("locationName")
                        .takeIf { it.isNotBlank() && it != "null" }

                    val task = TaskEntity(
                        title = item.optString("title", "Tugas WhatsApp").ifBlank { "Tugas WhatsApp" },
                        subject = item.optString("subject", "Umum").ifBlank { "Umum" },
                        description = item.optString("description", ""),
                        deadlineEpochMillis = deadline,
                        estimatedMinutes = item.optInt("estimatedMinutes", 30).coerceIn(5, 240),
                        priority = priority,
                        isPersistent = item.optBoolean("isPersistent", true),
                        locationName = locationName,
                        locationTrigger = if (locationName == null) null else item.optString("locationTrigger", "ENTER"),
                        aiMotivationQuote = item.optString("aiAdvice").takeIf { it.isNotBlank() },
                        reminderMinutesBefore = item.optInt("reminderMinutesBefore", 0),
                        source = "WHATSAPP",
                        externalId = externalId
                    )

                    val localId = taskDao.insertTask(task)
                    val created = task.copy(id = localId)
                    TaskReminderScheduler.schedule(context, created)
                    if (!created.locationName.isNullOrBlank()) {
                        LocationReminderManager.refreshGeofencesIfActive(context)
                    }
                    acknowledge(baseUrl, externalId)
                    imported++
                }
            }

            if (imported > 0) {
                AntiMagerWidgetProvider.sendUpdateBroadcast(context)
            }

            WhatsAppSyncResult(
                imported,
                if (imported > 0) "Terhubung • $imported tugas WhatsApp masuk."
                else "Terhubung • tidak ada tugas baru."
            )
        } catch (e: Exception) {
            WhatsAppSyncResult(0, "Gagal terhubung: ${e.message ?: "cek Wi-Fi dan URL bridge"}")
        }
    }

    private fun acknowledge(baseUrl: String, externalId: String) {
        try {
            val body = "{}".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/api/tasks/$externalId/ack")
                .addHeader("X-AntiMager-Token", DEFAULT_TOKEN)
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (_: Exception) {
        }
    }
}
