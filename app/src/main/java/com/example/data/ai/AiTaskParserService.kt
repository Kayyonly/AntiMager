package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedTaskResult(
    val title: String,
    val subject: String,
    val description: String,
    val deadlineMillis: Long,
    val deadlineFormatted: String,
    val estimatedMinutes: Int,
    val priority: String, // HIGH, MEDIUM, LOW
    val locationTag: String? = null,
    val aiAdvice: String = "",
    val isAmbiguous: Boolean = false,
    val clarificationQuestion: String? = null
)

object AiTaskParserService {

    private const val TAG = "AiTaskParserService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Groq (Llama 3.3 70B) natural language parser with structured fallback
     */
    suspend fun parseWithExternalApi(userStory: String, conversationContext: String? = null): ParsedTaskResult? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GROQ_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (apiKey.isBlank() || apiKey == "MY_GROQ_API_KEY") return@withContext null

        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
            val nowStr = sdf.format(Date())

            val contextNote = if (!conversationContext.isNullOrBlank()) {
                "Konteks pesan sebelumnya dari percakapan: \"$conversationContext\"."
            } else ""

            val systemPrompt = """
                Kamu adalah parser tugas AntiMager AI berbahasa Indonesia.
                Waktu sekarang: $nowStr (WIB).
                $contextNote
                
                Instruksi:
                1. Jika input SANGAT AMBIGU (contoh: "besok kerjain tugas", "nanti ingetin tugas", "ada tugas besok") dan nama/judul tugas TIDAK DISEBUTKAN:
                   Set "isAmbiguous": true, "clarificationQuestion": "Tugas apa yang mau dikerjakan? Ceritakan judul atau mata pelajarannya ya!"
                2. Jika informasi tugas JELAS (contoh: "PR IPS besok", "Kerjain matematika jam 8 malam", "Jumat kumpul tugas IPA", "Nanti sore ingetin beli buku", "Besok sebelum sekolah bawa seragam olahraga"):
                   Set "isAmbiguous": false, "clarificationQuestion": null.
                   Ekstrak atribut tugas berikut:
                   - "title": Judul tugas ringkas tanpa kata waktu/perintah (contoh: "PR IPS", "Kerjain Matematika", "Kumpul Tugas IPA", "Beli Buku", "Bawa Seragam Olahraga")
                   - "subject": Kategori/Mapel (Matematika/IPA/Biologi/Fisika/Kimia/IPS/B. Indonesia/B. Inggris/Sejarah/Penjasorkes/Belanja/Rumah/Umum)
                   - "estimatedMinutes": Perkiraan durasi menit (15 - 120)
                   - "priority": "HIGH" (jika mendesak/penting/hari ini), "MEDIUM", atau "LOW"
                   - "locationTag": Tempat terkait (Sekolah/Indomaret/Rumah/Perpustakaan/null)
                   - "deadlineIso": Format "yyyy-MM-dd HH:mm"
                   - "aiAdvice": 1 kalimat tips psikologis anti-prokrastinasi dalam bahasa santai & ramah
                
                Kembalikan HANYA JSON murni format:
                {
                  "isAmbiguous": false,
                  "clarificationQuestion": null,
                  "title": "PR IPS",
                  "subject": "IPS",
                  "estimatedMinutes": 35,
                  "priority": "HIGH",
                  "locationTag": "Sekolah",
                  "deadlineIso": "2026-09-25 08:00",
                  "aiAdvice": "Cicil 1 nomor sekarang biar besok pagi gak panik!"
                }
            """.trimIndent()

            val candidateModels = GroqModelManager.getCandidateModels()
            var body: String? = null

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
                            put("content", userStory)
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

                val response = httpClient.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    body = responseStr
                    GroqModelManager.markModelWorking(candidate)
                    break
                } else {
                    Log.w(TAG, "Groq model $candidate error ${response.code}: $responseStr")
                    if (response.code == 404 || responseStr.contains("model_not_found") || responseStr.contains("does not exist")) {
                        continue
                    } else {
                        return@withContext null
                    }
                }
            }

            if (body == null) return@withContext null
            val root = JSONObject(body)
            val choices = root.optJSONArray("choices") ?: return@withContext null
            if (choices.length() == 0) return@withContext null
            val text = choices.getJSONObject(0).optJSONObject("message")?.optString("content") ?: return@withContext null

            val parsed = JSONObject(text)
            val isAmbiguous = parsed.optBoolean("isAmbiguous", false)
            val clarification = parsed.optString("clarificationQuestion", null)

            if (isAmbiguous) {
                return@withContext ParsedTaskResult(
                    title = "",
                    subject = "Umum",
                    description = userStory,
                    deadlineMillis = System.currentTimeMillis() + 86400000L,
                    deadlineFormatted = "Besok",
                    estimatedMinutes = 30,
                    priority = "MEDIUM",
                    aiAdvice = clarification ?: "Tugas apa yang mau dikerjain?",
                    isAmbiguous = true,
                    clarificationQuestion = clarification ?: "Tugas apa yang mau dikerjain? Ceritakan judul atau mata pelajarannya ya!"
                )
            }

            val title = parsed.optString("title", userStory)
            val subject = parsed.optString("subject", "Umum")
            val estimated = parsed.optInt("estimatedMinutes", 30)
            val priority = parsed.optString("priority", "MEDIUM")
            val location = if (parsed.isNull("locationTag") || parsed.optString("locationTag").isBlank()) null else parsed.optString("locationTag")
            val advice = parsed.optString("aiAdvice", "Kerjakan tepat waktu agar pikiran bebas santuy!")
            val deadlineIso = parsed.optString("deadlineIso", "")

            var deadlineMillis = System.currentTimeMillis() + (4 * 3600 * 1000L)
            var deadlineFormatted = "Hari ini"
            if (deadlineIso.isNotBlank()) {
                try {
                    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    val date = parser.parse(deadlineIso)
                    if (date != null) {
                        deadlineMillis = date.time
                        val outFmt = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
                        deadlineFormatted = outFmt.format(date)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse deadlineIso: $deadlineIso", e)
                }
            }

            ParsedTaskResult(
                title = title,
                subject = subject,
                description = userStory,
                deadlineMillis = deadlineMillis,
                deadlineFormatted = deadlineFormatted,
                estimatedMinutes = estimated,
                priority = priority,
                locationTag = location,
                aiAdvice = advice,
                isAmbiguous = false,
                clarificationQuestion = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini parser", e)
            null
        }
    }

    /**
     * Smart Indonesian Natural Language Parser (Offline & Fast)
     * Supports:
     * - "PR IPS besok"
     * - "Kerjain matematika jam 8 malam"
     * - "Jumat kumpul tugas IPA"
     * - "Nanti sore ingetin beli buku"
     * - "Besok sebelum sekolah bawa seragam olahraga"
     * - Ambiguity detection for messages like "Besok kerjain tugas"
     */
    fun parseStory(userStory: String, previousContext: String? = null): ParsedTaskResult {
        var input = userStory.trim()
        val lowerRaw = input.lowercase(Locale.ROOT)

        // If there was a previous ambiguous context and user is answering it
        if (!previousContext.isNullOrBlank() && (previousContext.contains("tugas apa", ignoreCase = true) || previousContext.contains("besok kerjain tugas", ignoreCase = true))) {
            input = "$previousContext $input"
        }

        val lower = input.lowercase(Locale.ROOT)

        // Check Ambiguity: e.g. "besok kerjain tugas", "nanti ingetin", "ada tugas besok"
        if (isInputAmbiguous(lower)) {
            val timeWord = when {
                lower.contains("besok") -> "besok"
                lower.contains("nanti") -> "nanti"
                lower.contains("jumat") -> "hari Jumat"
                else -> "ini"
            }
            return ParsedTaskResult(
                title = "",
                subject = "Umum",
                description = input,
                deadlineMillis = System.currentTimeMillis() + 86400000L,
                deadlineFormatted = "Besok",
                estimatedMinutes = 30,
                priority = "MEDIUM",
                aiAdvice = "Tugas apa yang mau dikerjain $timeWord? Beritahu judul atau mapelnya ya! ✍️",
                isAmbiguous = true,
                clarificationQuestion = "Tugas apa yang mau dikerjain $timeWord? Beritahu judul atau mapelnya ya! ✍️"
            )
        }

        // 1. Extract Subject
        val subject = extractSubject(lower)

        // 2. Extract Deadline & Time
        val (deadlineMillis, timeString) = extractDeadline(lower)

        // 3. Extract Duration
        val estimatedMinutes = extractDuration(lower)

        // 4. Extract Priority
        val priority = extractPriority(lower, deadlineMillis)

        // 5. Extract Location
        val locationTag = extractLocation(lower)

        // 6. Clean Title
        val cleanTitle = buildCleanTitle(input, subject)

        // 7. Anti-procrastination advice
        val advice = generateAntiMagerAdvice(priority, estimatedMinutes, subject)

        return ParsedTaskResult(
            title = cleanTitle,
            subject = subject,
            description = input,
            deadlineMillis = deadlineMillis,
            deadlineFormatted = timeString,
            estimatedMinutes = estimatedMinutes,
            priority = priority,
            locationTag = locationTag,
            aiAdvice = advice,
            isAmbiguous = false,
            clarificationQuestion = null
        )
    }

    private fun isInputAmbiguous(text: String): Boolean {
        // Strip common fillers and time indicators
        var stripped = text
        val fillers = listOf(
            "tolong", "ingetin", "ingatkan", "aku", "ada", "mau", "ngerjain", "kerjain",
            "kumpul", "kumpulin", "bikin", "buat", "tugas", "pr", "besok", "lusa",
            "nanti", "hari ini", "siang", "pagi", "sore", "malam", "ya", "dong", "deh", "nih"
        )
        for (f in fillers) {
            stripped = stripped.replace("(?i)\\b$f\\b".toRegex(), " ")
        }
        val remaining = stripped.trim().replace(Regex("\\s+"), " ")
        // If nothing of substance remains (less than 3 characters of content), it's ambiguous
        return remaining.length < 3
    }

    private fun extractSubject(text: String): String {
        return when {
            text.contains("ips") || text.contains("sosial") || text.contains("geografi") || text.contains("ekonomi") || text.contains("sosiologi") -> "IPS"
            text.contains("ipa") || text.contains("biologi") || text.contains("fisika") || text.contains("kimia") -> {
                when {
                    text.contains("biologi") -> "Biologi"
                    text.contains("fisika") -> "Fisika"
                    text.contains("kimia") -> "Kimia"
                    else -> "IPA"
                }
            }
            text.contains("matematika") || text.contains("mtk") || text.contains("kalkulus") || text.contains("aljabar") || text.contains("integral") -> "Matematika"
            text.contains("bahasa indonesia") || text.contains("b.indo") || text.contains("b indo") -> "B. Indonesia"
            text.contains("bahasa inggris") || text.contains("b.inggris") || text.contains("b inggris") || text.contains("english") -> "B. Inggris"
            text.contains("sejarah") -> "Sejarah"
            text.contains("pkn") || text.contains("kewarganegaraan") -> "PKN"
            text.contains("agama") -> "Agama"
            text.contains("seni") || text.contains("gambar") || text.contains("lukis") -> "Seni Budaya"
            text.contains("olahraga") || text.contains("penjas") || text.contains("seragam olahraga") || text.contains("senam") -> "Penjasorkes"
            text.contains("coding") || text.contains("koding") || text.contains("informatika") || text.contains("pemrograman") -> "Informatika"
            text.contains("beli") || text.contains("belanja") || text.contains("indomaret") || text.contains("alfamart") -> "Belanja"
            text.contains("kerjaan") || text.contains("kantor") || text.contains("meeting") || text.contains("laporan") -> "Pekerjaan"
            text.contains("rumah") || text.contains("cuci") || text.contains("beres") || text.contains("sapu") -> "Rumah"
            else -> "Umum"
        }
    }

    private fun extractDeadline(text: String): Pair<Long, String> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        var daysToAdd = 0
        var targetHour = 20
        var targetMinute = 0
        var dayLabel = ""

        // Indonesian day names mapping
        val dayMap = mapOf(
            "senin" to Calendar.MONDAY,
            "selasa" to Calendar.TUESDAY,
            "rabu" to Calendar.WEDNESDAY,
            "kamis" to Calendar.THURSDAY,
            "jumat" to Calendar.FRIDAY,
            "jum'at" to Calendar.FRIDAY,
            "sabtu" to Calendar.SATURDAY,
            "minggu" to Calendar.SUNDAY,
            "ahad" to Calendar.SUNDAY
        )

        var matchedDayOfWeek: Int? = null
        for ((name, calDay) in dayMap) {
            if (Pattern.compile("\\b$name\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                matchedDayOfWeek = calDay
                dayLabel = name.replaceFirstChar { it.uppercase() }
                break
            }
        }

        if (matchedDayOfWeek != null) {
            val currentDay = cal.get(Calendar.DAY_OF_WEEK)
            var diff = matchedDayOfWeek - currentDay
            if (diff <= 0) {
                diff += 7
            }
            daysToAdd = diff
            targetHour = 10 // Default daytime for weekday deadlines
        } else if (text.contains("lusa")) {
            daysToAdd = 2
            targetHour = 10
            dayLabel = "Lusa"
        } else if (text.contains("besok")) {
            daysToAdd = 1
            dayLabel = "Besok"
            when {
                text.contains("sebelum sekolah") || text.contains("sebelum masuk") -> {
                    targetHour = 6
                    targetMinute = 30
                }
                text.contains("pagi") -> targetHour = 8
                text.contains("siang") -> targetHour = 13
                text.contains("sore") -> targetHour = 16
                text.contains("malam") -> targetHour = 20
                else -> targetHour = 9
            }
        } else if (text.contains("nanti malam") || text.contains("malam ini")) {
            daysToAdd = 0
            targetHour = 20
            dayLabel = "Hari ini"
        } else if (text.contains("nanti sore") || text.contains("sore ini")) {
            daysToAdd = 0
            targetHour = 16
            targetMinute = 30
            dayLabel = "Hari ini"
        } else if (text.contains("nanti siang") || text.contains("siang ini")) {
            daysToAdd = 0
            targetHour = 13
            dayLabel = "Hari ini"
        } else if (text.contains("sebentar lagi") || text.contains("2 jam")) {
            cal.add(Calendar.HOUR_OF_DAY, 2)
            return Pair(cal.timeInMillis, "2 Jam Lagi")
        } else {
            daysToAdd = 1
            targetHour = 12
            dayLabel = "Besok"
        }

        // Specific time pattern: "jam 8 malam", "jam 20:00", "pukul 15"
        val jamPattern = Pattern.compile("(?:jam|pukul)\\s*(\\d{1,2})(?:[:.](\\d{1,2}))?", Pattern.CASE_INSENSITIVE)
        val matcher = jamPattern.matcher(text)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: targetHour
            val m = matcher.group(2)?.toIntOrNull() ?: 0
            targetHour = when {
                (text.contains("malam") || text.contains("mlm")) && h < 12 -> h + 12
                (text.contains("sore")) && h in 1..6 -> h + 12
                (text.contains("siang")) && h in 1..3 -> h + 12
                else -> h
            }
            targetMinute = m
        }

        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        cal.set(Calendar.HOUR_OF_DAY, targetHour.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, targetMinute.coerceIn(0, 59))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // If calculated time is in the past for today, push to tomorrow
        if (daysToAdd == 0 && cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dayLabel = "Besok"
        }

        val timeFormatted = String.format(Locale.ROOT, "%02d:%02d WIB", targetHour, targetMinute)
        val formatted = if (dayLabel.isNotBlank()) "$dayLabel, $timeFormatted" else {
            val sdf = SimpleDateFormat("EEE, dd MMM - HH:mm", Locale("id", "ID"))
            sdf.format(cal.time)
        }

        return Pair(cal.timeInMillis, formatted)
    }

    private fun extractDuration(text: String): Int {
        val menitPattern = Pattern.compile("(\\d+)\\s*(?:menit|mnt|m\\b)", Pattern.CASE_INSENSITIVE)
        val mMatcher = menitPattern.matcher(text)
        if (mMatcher.find()) {
            return mMatcher.group(1)?.toIntOrNull() ?: 30
        }

        val jamPattern = Pattern.compile("(\\d+)\\s*(?:jam|jm)", Pattern.CASE_INSENSITIVE)
        val jMatcher = jamPattern.matcher(text)
        if (jMatcher.find()) {
            val hours = jMatcher.group(1)?.toIntOrNull() ?: 1
            return hours * 60
        }

        return when {
            text.contains("bentar") || text.contains("sebentar") || text.contains("bawa") -> 15
            text.contains("beli") || text.contains("kumpul") -> 20
            text.contains("makalah") || text.contains("laporan") || text.contains("proyek") -> 60
            else -> 30
        }
    }

    private fun extractPriority(text: String, deadlineMillis: Long): String {
        val diffHours = (deadlineMillis - System.currentTimeMillis()) / (1000 * 60 * 60)
        return when {
            text.contains("urgent") || text.contains("penting") || text.contains("gawat") || text.contains("mepet") -> "HIGH"
            diffHours <= 12 -> "HIGH"
            text.contains("santai") || text.contains("kapan-kapan") || diffHours > 48 -> "LOW"
            else -> "MEDIUM"
        }
    }

    private fun extractLocation(text: String): String? {
        return when {
            text.contains("sekolah") -> "Sekolah"
            text.contains("kampus") || text.contains("univ") -> "Kampus"
            text.contains("indomaret") || text.contains("alfamart") || text.contains("minimarket") || text.contains("toko") -> "Indomaret"
            text.contains("perpus") || text.contains("perpustakaan") -> "Perpustakaan"
            text.contains("rumah") || text.contains("kost") || text.contains("kos") -> "Rumah"
            text.contains("kantor") -> "Kantor"
            else -> null
        }
    }

    private fun buildCleanTitle(raw: String, subject: String): String {
        var cleaned = raw
        val removePhrases = listOf(
            "tolong ingetin", "ingetin dong", "ingetin ya", "ingatkan saya", "ingetin",
            "aku ada", "mau ngerjain", "nanti sore", "nanti malam", "nanti siang",
            "besok pagi", "besok sore", "besok malam", "besok sebelum sekolah",
            "sebelum sekolah", "hari ini", "besok", "lusa", "sebentar lagi"
        )
        for (phrase in removePhrases) {
            cleaned = cleaned.replace("(?i)$phrase".toRegex(), " ")
        }
        // Remove "jam X" or "pukul X"
        cleaned = cleaned.replace("(?i)(jam|pukul)\\s*\\d{1,2}([:.]\\d{1,2})?(\\s*(malam|pagi|siang|sore))?".toRegex(), " ")
        cleaned = cleaned.trim().replace(Regex("\\s+"), " ")

        if (cleaned.length < 3) {
            cleaned = if (subject != "Umum") "Tugas $subject" else raw.take(30)
        }
        return cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    private fun generateAntiMagerAdvice(priority: String, duration: Int, subject: String): String {
        return when {
            priority == "HIGH" -> "🚨 Deadline sudah mepet! Trik anti-mager: pasang timer $duration menit, singkirkan HP, kerjain 1 langkah dulu!"
            duration <= 20 -> "⚡ Cuma butuh $duration menit! Beresin sekarang juga biar sisa harimu plong bebas beban."
            subject in listOf("IPS", "Sejarah", "PKN") -> "📖 Buat tugas bacaan $subject, cukup baca rangkumannya dulu sambil rileks 10 menit."
            subject in listOf("Matematika", "Fisika", "Kimia") -> "📐 Mulai dari soal nomor 1 yang paling gampang. Begitu mulai jalan, magernya langsung lenyap!"
            subject == "Belanja" -> "🛒 Beli sekarang selagi ingat supaya gak bolak-balik keluar rumah!"
            else -> "💡 Ingat moto AntiMager: Lebih baik tugas selesai daripada sempurna tapi gak dikumpul!"
        }
    }
}
