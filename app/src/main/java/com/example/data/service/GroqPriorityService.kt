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
    private const val GROQ_MODEL = "llama-3.3-70b-versatile"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GROQ_API_KEY") {
            Log.w(TAG, "Groq API key is blank, falling back to smart heuristic")
            return@withContext fallbackHeuristic(tasks, nowMillis, "Mode Offline: Menggunakan algoritma heuristik AntiMager")
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
                Kamu adalah AntiMager AI (Llama 3.3 70B), asisten produktivitas siswa & mahasiswa.
                Waktu sekarang: $currentDateTimeStr.
                
                Tugasmu menganalisis daftar tugas berikut dan mengurutkan berdasarkan prioritas psikologis & akademis terbaik (Prinsip Anti-Prokrastinasi, Eisenhower Matrix, Cognitive Load):
                - Sisa waktu deadline (urgent vs penting).
                - Durasi pengerjaan (quick wins vs deep work).
                - Tingkat snoozed (sering ditunda = butuh perhatian darurat).
                
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

            val candidateModels = com.example.data.ai.GroqModelManager.getCandidateModels()
            var responseString: String? = null
            var usedModel: String = candidateModels.firstOrNull() ?: "llama-3.3-70b-versatile"

            for (candidate in candidateModels) {
                val requestJson = JSONObject().apply {
                    put("model", candidate)
                    val messagesArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Urutkan daftar tugas ini sekarang sesuai prioritas terbaik.")
                        })
                    }
                    put("messages", messagesArray)
                    put("response_format", JSONObject().apply {
                        put("type", "json_object")
                    })
                    put("temperature", 0.1)
                }

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer $apiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    responseString = responseBody
                    usedModel = candidate
                    com.example.data.ai.GroqModelManager.markModelWorking(candidate)
                    break
                } else {
                    Log.w(TAG, "Groq candidate model $candidate returned HTTP ${response.code}: $responseBody")
                    // If error is model_not_found or access error (HTTP 404), try next candidate
                    if (response.code == 404 || responseBody.contains("model_not_found") || responseBody.contains("does not exist")) {
                        continue
                    } else {
                        // Some other HTTP error (e.g. rate limit / auth error)
                        return@withContext fallbackHeuristic(tasks, nowMillis, "Koneksi AI (${response.code}), menggunakan heuristik cerdas")
                    }
                }
            }

            if (responseString == null) {
                Log.e(TAG, "All Groq candidate models failed, falling back to heuristic")
                return@withContext fallbackHeuristic(tasks, nowMillis, "Model AI Groq tidak tersedia di akun ini, menggunakan heuristik cerdas")
            }

            val jsonRoot = JSONObject(responseString)
            val choices = jsonRoot.optJSONArray("choices") ?: return@withContext fallbackHeuristic(tasks, nowMillis, "Format respons AI tidak valid")
            if (choices.length() == 0) return@withContext fallbackHeuristic(tasks, nowMillis, "Tidak ada jawaban AI")

            val messageContent = choices.getJSONObject(0).optJSONObject("message")?.optString("content") ?: ""
            val parsedOutput = JSONObject(messageContent)
            val globalAdvice = parsedOutput.optString("globalAdvice", "Fokus selesaikan tugas paling mendesak hari ini!")
            val sortedTasksArray = parsedOutput.optJSONArray("sortedTasks") ?: JSONArray()

            val taskMap = pendingTasks.associateBy { it.id }
            val prioritizedList = mutableListOf<AiPrioritizedTask>()

            for (i in 0 until sortedTasksArray.length()) {
                val item = sortedTasksArray.getJSONObject(i)
                val taskId = item.optLong("id", -1L)
                val targetTask = taskMap[taskId] ?: continue

                prioritizedList.add(
                    AiPrioritizedTask(
                        task = targetTask,
                        rank = item.optInt("rank", i + 1),
                        urgencyCategory = item.optString("urgencyCategory", "MEDIUM"),
                        badgeLabel = item.optString("badgeLabel", "Prioritas AI"),
                        badgeColorHex = item.optString("badgeColorHex", "#5856D6"),
                        aiRationale = item.optString("aiRationale", "Disarankan oleh AntiMager AI")
                    )
                )
            }

            // Append any tasks missing from AI response
            val addedIds = prioritizedList.map { it.task.id }.toSet()
            val remainingTasks = pendingTasks.filter { it.id !in addedIds }
            for (rem in remainingTasks) {
                val urgencyInfo = SmartPrioritySorter.calculateUrgency(rem, nowMillis)
                prioritizedList.add(
                    AiPrioritizedTask(
                        task = rem,
                        rank = prioritizedList.size + 1,
                        urgencyCategory = "MEDIUM",
                        badgeLabel = urgencyInfo.badgeLabel,
                        badgeColorHex = urgencyInfo.badgeColorHex,
                        aiRationale = "Berdasarkan estimasi waktu dan urgensi deadline"
                    )
                )
            }

            AiPrioritySortResult(
                isRealAi = true,
                modelName = com.example.data.ai.GroqModelManager.getActiveDisplayName(),
                prioritizedTasks = prioritizedList,
                globalAdvice = globalAdvice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in Groq priority sorting", e)
            fallbackHeuristic(tasks, nowMillis, "Menggunakan heuristik cerdas (offline)")
        }
    }

    private fun fallbackHeuristic(
        tasks: List<TaskEntity>,
        nowMillis: Long,
        message: String
    ): AiPrioritySortResult {
        val (pendingTasks, _) = tasks.partition { !it.isCompleted }
        val sorted = SmartPrioritySorter.sortTasks(pendingTasks, com.example.util.SortMode.SMART_AI, nowMillis)

        val prioritized = sorted.mapIndexed { index, task ->
            val urgency = SmartPrioritySorter.calculateUrgency(task, nowMillis)
            val category = when {
                urgency.score >= 100 -> "CRITICAL"
                urgency.score >= 60 -> "HIGH"
                urgency.score >= 30 -> "MEDIUM"
                else -> "RELAXED"
            }
            AiPrioritizedTask(
                task = task,
                rank = index + 1,
                urgencyCategory = category,
                badgeLabel = urgency.badgeLabel,
                badgeColorHex = urgency.badgeColorHex,
                aiRationale = "Diurutkan otomatis: sisa ${urgency.humanTimeRemaining} (${task.estimatedMinutes} mnt)"
            )
        }

        return AiPrioritySortResult(
            isRealAi = false,
            modelName = "AntiMager Heuristic",
            prioritizedTasks = prioritized,
            globalAdvice = message
        )
    }
}
