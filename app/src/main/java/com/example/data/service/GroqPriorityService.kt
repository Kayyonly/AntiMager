package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.TaskEntity
import com.example.util.SmartPrioritySorter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AiPrioritizedTask(
    val task: TaskEntity,
    val rank: Int,
    val urgencyCategory: String, // CRITICAL, HIGH, MEDIUM, RELAXED
    val badgeLabel: String,
    val badgeColorHex: String,
    val aiRationale: String
)

data class AiPrioritySortResult(
    val isRealAi: Boolean,
    val modelName: String,
    val prioritizedTasks: List<AiPrioritizedTask>,
    val globalAdvice: String
)

object GroqPriorityService {

    private const val TAG = "GroqPriorityService"
    private const val DEFAULT_GROQ_MODEL = "llama-3.3-70b-versatile"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sortTasksWithGroq(
        tasks: List<TaskEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): AiPrioritySortResult = withContext(Dispatchers.IO) {
        val (pendingTasks, completedTasks) = tasks.partition { !it.isCompleted }

        if (pendingTasks.isEmpty()) {
            return@withContext AiPrioritySortResult(
                isRealAi = false,
                modelName = "Local",
                prioritizedTasks = emptyList(),
                globalAdvice = "Semua tugas sudah selesai! Waktunya istirahat."
            )
        }

        val apiKey = try {
            BuildConfig.GROQ_API_KEY
        } catch (_: Exception) {
            ""
        }
        val configuredModel = try {
            BuildConfig.GROQ_MODEL
        } catch (_: Exception) {
            ""
        }
        val model = configuredModel.ifBlank { DEFAULT_GROQ_MODEL }

        if (apiKey.isBlank() || apiKey == "MY_GROQ_API_KEY") {
            Log.w(TAG, "Groq API key is blank, falling back to smart heuristic")
            return@withContext fallbackHeuristic(tasks, nowMillis, "Mode offline: algoritma prioritas AntiMager")
        }

        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
            val currentDateTimeStr = sdf.format(Date(nowMillis))

            // Build task summary array for prompt
            val taskListJson = JSONArray()
            for (t in pendingTasks) {
                val remainingHours = (t.deadlineEpochMillis - nowMillis) / (1000.0 * 60.0 * 60.0)
                val taskObj = JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("subject", t.subject)
                    put("description", t.description)
                    put("estimatedMinutes", t.estimatedMinutes)
                    put("priority", t.priority)
                    put("snoozeCount", t.snoozeCount)
                    put("remainingHours", String.format(Locale.US, "%.1f", remainingHours))
                    put("deadlineDate", sdf.format(Date(t.deadlineEpochMillis)))
                }
                taskListJson.put(taskObj)
            }

            val systemPrompt = """
                Kamu adalah AntiMager AI, asisten produktivitas siswa & mahasiswa nomor 1.
                Waktu sekarang: $currentDateTimeStr.
                
                Tugasmu adalah menganalisis daftar tugas berikut dan mengurutkan berdasarkan prioritas psikologis & akademis terbaik (Prinsip Anti-Prokrastinasi, Eisenhower Matrix, Cognitive Load):
                - Pertimbangkan sisa waktu deadline (urgent vs penting).
                - Pertimbangkan durasi pengerjaan (quick wins vs deep work).
                - Pertimbangkan tingkat snoozed (sering ditunda = butuh perhatian darurat).
                
                Daftar Tugas Pending:
                ${taskListJson.toString(2)}
                
                Kembalikan HANYA format JSON valid berikut:
                {
                  "globalAdvice": "Pesan motivasi 1 kalimat singkat dan tajam untuk pengguna hari ini.",
                  "sortedTasks": [
                    {
                      "id": 1,
                      "rank": 1,
                      "urgencyCategory": "CRITICAL",
                      "badgeLabel": "🔥 Kerjakan Sekarang",
                      "badgeColorHex": "#FF3B30",
                      "aiRationale": "Alasan singkat 1 kalimat mengapa tugas ini harus dikerjakan duluan."
                    }
                  ]
                }
                Kategori urgencyCategory yang diizinkan: CRITICAL, HIGH, MEDIUM, RELAXED.
                Gunakan badgeColorHex iOS: CRITICAL (#FF3B30), HIGH (#FF9500), MEDIUM (#5856D6), RELAXED (#34C759).
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("model", model)
                put("temperature", 0.2)
                put("response_format", JSONObject().apply { put("type", "json_object") })
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put(
                            "content",
                            "Kamu adalah mesin prioritas AntiMager. Urutkan tugas berdasarkan deadline, durasi, prioritas manual, dan riwayat snooze. Balas hanya JSON valid."
                        )
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", systemPrompt)
                    })
                })
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Groq API failed HTTP ${response.code}: $errorBody")
                return@withContext fallbackHeuristic(tasks, nowMillis, "Gagal koneksi AI (${response.code}), menggunakan heuristik cerdas")
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val textOutput = jsonRoot.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                .orEmpty()

            if (textOutput.isBlank()) {
                return@withContext fallbackHeuristic(tasks, nowMillis, "Format respon kosong dari Groq")
            }

            val parsedJson = JSONObject(textOutput)
            val globalAdvice = parsedJson.optString("globalAdvice", "Fokus selesaikan tugas paling mendesak satu per satu!")
            val sortedArray = parsedJson.optJSONArray("sortedTasks") ?: JSONArray()

            val taskMap = pendingTasks.associateBy { it.id }
            val prioritizedList = mutableListOf<AiPrioritizedTask>()
            val processedIds = mutableSetOf<Long>()

            for (i in 0 until sortedArray.length()) {
                val item = sortedArray.optJSONObject(i) ?: continue
                val id = item.optLong("id")
                val task = taskMap[id] ?: continue

                processedIds.add(id)
                prioritizedList.add(
                    AiPrioritizedTask(
                        task = task,
                        rank = item.optInt("rank", i + 1),
                        urgencyCategory = item.optString("urgencyCategory", "MEDIUM"),
                        badgeLabel = item.optString("badgeLabel", "⚡ Rekomendasi AI"),
                        badgeColorHex = item.optString("badgeColorHex", "#007AFF"),
                        aiRationale = item.optString("aiRationale", "Prioritas optimal berdasarkan deadline dan durasi.")
                    )
                )
            }

            // Append any tasks that might have been skipped by AI
            for (t in pendingTasks) {
                if (!processedIds.contains(t.id)) {
                    val info = SmartPrioritySorter.calculateUrgency(t, nowMillis)
                    prioritizedList.add(
                        AiPrioritizedTask(
                            task = t,
                            rank = prioritizedList.size + 1,
                            urgencyCategory = "MEDIUM",
                            badgeLabel = info.badgeLabel,
                            badgeColorHex = info.badgeColorHex,
                            aiRationale = "Sisa waktu ${info.humanTimeRemaining}."
                        )
                    )
                }
            }

            Log.d(TAG, "Successfully sorted ${prioritizedList.size} tasks with Groq")

            AiPrioritySortResult(
                isRealAi = true,
                modelName = "Groq • $model",
                prioritizedTasks = prioritizedList,
                globalAdvice = globalAdvice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Groq Priority Sorting", e)
            fallbackHeuristic(tasks, nowMillis, "Error memanggil Groq (${e.localizedMessage}), menggunakan heuristik lokal")
        }
    }

    private fun fallbackHeuristic(
        tasks: List<TaskEntity>,
        nowMillis: Long,
        advice: String
    ): AiPrioritySortResult {
        val (pending, _) = tasks.partition { !it.isCompleted }
        val sortedPending = pending.sortedByDescending { SmartPrioritySorter.calculateUrgency(it, nowMillis).score }

        val list = sortedPending.mapIndexed { index, task ->
            val info = SmartPrioritySorter.calculateUrgency(task, nowMillis)
            AiPrioritizedTask(
                task = task,
                rank = index + 1,
                urgencyCategory = if (info.score >= 100) "CRITICAL" else if (info.score >= 60) "HIGH" else "RELAXED",
                badgeLabel = info.badgeLabel,
                badgeColorHex = info.badgeColorHex,
                aiRationale = "Algoritma prioritas AntiMager: Sisa waktu ${info.humanTimeRemaining}."
            )
        }

        return AiPrioritySortResult(
            isRealAi = false,
            modelName = "Smart Heuristic",
            prioritizedTasks = list,
            globalAdvice = advice
        )
    }
}
