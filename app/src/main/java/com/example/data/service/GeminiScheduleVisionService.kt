package com.example.data.service

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ExtractedScheduleItem(
    val dayOfWeek: Int, // 1 = Senin, 2 = Selasa, 3 = Rabu, 4 = Kamis, 5 = Jumat, 6 = Sabtu, 7 = Minggu
    val dayName: String,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val roomOrTeacher: String = "",
    val isBreak: Boolean = false,
    val colorHex: String = "#38BDF8",
    val className: String = "" // Optional class tag (e.g., "X IPA 2")
) {
    fun toEntity(): ScheduleEntity {
        val finalRoomOrTeacher = if (className.isNotBlank() && !roomOrTeacher.contains(className)) {
            if (roomOrTeacher.isBlank()) "Kelas $className" else "$roomOrTeacher • $className"
        } else {
            roomOrTeacher
        }

        return ScheduleEntity(
            dayOfWeek = dayOfWeek,
            subject = subject,
            startTime = startTime,
            endTime = endTime,
            roomOrTeacher = finalRoomOrTeacher,
            isBreak = isBreak,
            colorHex = if (isBreak) "#F59E0B" else colorHex
        )
    }
}

/**
 * Result data class containing multi-class detection, list of detected classes,
 * target class matching status, and extracted schedule slots.
 */
data class ScheduleScanResult(
    val isMultiClass: Boolean,
    val detectedClasses: List<String>,
    val targetClass: String?,
    val targetClassFound: Boolean,
    val matchedClass: String,
    val detectionNote: String,
    val items: List<ExtractedScheduleItem>
)

class GeminiScheduleVisionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun extractScheduleFromBitmap(
        bitmap: Bitmap,
        targetUserClass: String? = null
    ): Result<ScheduleScanResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            // Downscale bitmap if too large for faster network payload
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, maxDimension = 1280)
            val base64Image = bitmapToBase64(scaledBitmap)

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key belum dikonfigurasi. Scan foto tidak bisa dijalankan.")
                )
            }

            val userClassPrompt = if (!targetUserClass.isNullOrBlank()) {
                "Pengguna memiliki profil kelas: \"$targetUserClass\"."
            } else {
                "Pengguna belum mengatur profil kelas (targetUserClass kosong)."
            }

            val promptText = """
                Kamu adalah asisten cerdas AI pembaca jadwal pelajaran sekolah di Indonesia.
                Tugasmu membaca foto jadwal pelajaran (bisa jadwal 1 kelas, atau tabel gabungan BANYAK KELAS sekaligus dalam format kolom/baris).
                
                $userClassPrompt
                
                Instruksi Analisis:
                1. DETEKSI FORMAT: Apakah foto berisi jadwal 1 KELAS saja ("isMultiClass": false) atau jadwal gabungan BANYAK KELAS ("isMultiClass": true, misalnya ada kolom/header untuk "X IPA 1", "X IPA 2", "X IPS 1", "XI MIPA 1", "7A", "8B", dll)?
                2. DAFTAR KELAS: Kumpulkan semua nama kelas yang terbaca di tabel ke dalam array "detectedClasses".
                3. FILTERING KELAS:
                   - Jika jadwal BANYAK KELAS ("isMultiClass": true):
                     * Cari kelas yang paling cocok dengan "$targetUserClass" (cocokkan secara toleran, contoh "X IPA 2" cocok dengan "X-IPA-2", "X.IPA.2", "X 2", "10 IPA 2").
                     * Jika COCOK ("targetClassFound": true): ekstrak HANYA mata pelajaran untuk kelas "$targetUserClass" tersebut ke dalam array "items". Isi "matchedClass" dengan nama kelas yang cocok.
                     * Jika TIDAK COCOK atau "$targetUserClass" tidak ada di foto ("targetClassFound": false): kosongkan array "items" ("items": []), isi "detectionNote" dengan keterangan bahwa kelas tersebut tidak ditemukan dan sertakan semua kelas yang ada di "detectedClasses".
                   - Jika jadwal 1 KELAS SAJA ("isMultiClass": false):
                     * Set "targetClassFound": true.
                     * Ekstrak seluruh mata pelajaran yang ada di foto ke dalam array "items".
                4. EKSTRAKSI SLOT JADWAL:
                   - "day": salah satu dari "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu".
                   - "startTime" dan "endTime": format 24 jam HH:mm (contoh: "07:15", "08:45").
                   - "subject": nama mata pelajaran.
                   - "roomOrTeacher": nama ruang kelas atau nama guru jika tertera di foto.
                   - "isBreak": true jika merupakan jam istirahat.
                
                Kembalikan HANYA format JSON murni dengan struktur:
                {
                  "isMultiClass": true,
                  "detectedClasses": ["X IPA 1", "X IPA 2", "X IPS 1"],
                  "targetClassFound": true,
                  "matchedClass": "X IPA 2",
                  "detectionNote": "Jadwal gabungan banyak kelas terdeteksi. Berhasil memfilter jadwal khusus untuk kelas X IPA 2.",
                  "items": [
                    {
                      "day": "Senin",
                      "startTime": "07:15",
                      "endTime": "08:45",
                      "subject": "Matematika",
                      "roomOrTeacher": "Lab Komputer • Bu Siti",
                      "isBreak": false
                    }
                  ]
                }
            """.trimIndent()

            // Prepare Gemini 3.5 Flash REST payload
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.15)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiVision", "API Error ${response.code}: $responseString")
                return@withContext Result.failure(
                    IllegalStateException("Gemini Vision gagal (HTTP ${response.code}). Coba lagi saat koneksi stabil.")
                )
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("Tidak ada teks jadwal yang terdeteksi."))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.getJSONObject(0)?.optString("text") ?: ""

            val parsedResult = parseJsonToScanResult(textOutput, targetUserClass)
                ?: return@withContext Result.failure(
                    IllegalStateException("Format jadwal dari AI tidak valid. Foto tidak disimpan.")
                )
            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e("GeminiVision", "Failed to extract schedule", e)
            Result.failure(e)
        }
    }

    private fun parseJsonToScanResult(rawJson: String, targetUserClass: String?): ScheduleScanResult? {
        try {
            var cleanJson = rawJson.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json")
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```")
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```")
            }
            cleanJson = cleanJson.trim()

            // Handle either a root JSON object or legacy array
            if (cleanJson.startsWith("{")) {
                val rootObj = JSONObject(cleanJson)
                val isMultiClass = rootObj.optBoolean("isMultiClass", false)
                val targetClassFound = rootObj.optBoolean("targetClassFound", true)
                val matchedClass = rootObj.optString("matchedClass", targetUserClass ?: "")
                val detectionNote = rootObj.optString("detectionNote", "")

                val detectedClasses = mutableListOf<String>()
                val classesArray = rootObj.optJSONArray("detectedClasses")
                if (classesArray != null) {
                    for (i in 0 until classesArray.length()) {
                        detectedClasses.add(classesArray.optString(i))
                    }
                }

                val itemsArray = rootObj.optJSONArray("items") ?: JSONArray()
                val items = parseItemsArray(itemsArray, matchedClass)

                return ScheduleScanResult(
                    isMultiClass = isMultiClass,
                    detectedClasses = detectedClasses,
                    targetClass = targetUserClass,
                    targetClassFound = targetClassFound,
                    matchedClass = matchedClass,
                    detectionNote = detectionNote,
                    items = items
                )
            } else if (cleanJson.startsWith("[")) {
                // Single-class array fallback
                val jsonArray = JSONArray(cleanJson)
                val items = parseItemsArray(jsonArray, targetUserClass ?: "")
                return ScheduleScanResult(
                    isMultiClass = false,
                    detectedClasses = emptyList(),
                    targetClass = targetUserClass,
                    targetClassFound = true,
                    matchedClass = targetUserClass ?: "Jadwal Kelas",
                    detectionNote = "Jadwal 1 kelas terdeteksi.",
                    items = items
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiVision", "Error parsing schedule json", e)
        }

        return null
    }

    private fun parseItemsArray(jsonArray: JSONArray, className: String): List<ExtractedScheduleItem> {
        val list = mutableListOf<ExtractedScheduleItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val dayStr = obj.optString("day", "Senin")
            val dayInt = dayStringToDayInt(dayStr)
            val startTime = obj.optString("startTime", "07:15")
            val endTime = obj.optString("endTime", "08:45")
            val subject = obj.optString("subject", "Pelajaran")
            val roomOrTeacher = obj.optString("roomOrTeacher", "")
            val isBreak = obj.optBoolean("isBreak", false) || subject.contains("Istirahat", ignoreCase = true)
            val colorHex = pickColorForSubject(subject, isBreak)

            list.add(
                ExtractedScheduleItem(
                    dayOfWeek = dayInt,
                    dayName = dayIntToDayString(dayInt),
                    startTime = startTime,
                    endTime = endTime,
                    subject = subject,
                    roomOrTeacher = roomOrTeacher,
                    isBreak = isBreak,
                    colorHex = colorHex,
                    className = className
                )
            )
        }
        return list
    }

    private fun dayStringToDayInt(day: String): Int {
        return when (day.trim().lowercase()) {
            "senin", "monday", "sen" -> 1
            "selasa", "tuesday", "sel" -> 2
            "rabu", "wednesday", "rab" -> 3
            "kamis", "thursday", "kam" -> 4
            "jumat", "jum'at", "friday", "jum" -> 5
            "sabtu", "saturday", "sab" -> 6
            "minggu", "ahad", "sunday", "min" -> 7
            else -> 1
        }
    }

    private fun dayIntToDayString(day: Int): String {
        return when (day) {
            1 -> "Senin"
            2 -> "Selasa"
            3 -> "Rabu"
            4 -> "Kamis"
            5 -> "Jumat"
            6 -> "Sabtu"
            7 -> "Minggu"
            else -> "Senin"
        }
    }

    private fun pickColorForSubject(subject: String, isBreak: Boolean): String {
        if (isBreak) return "#F59E0B"
        val lower = subject.lowercase()
        return when {
            lower.contains("matematika") || lower.contains("mtk") -> "#38BDF8"
            lower.contains("inggris") || lower.contains("english") -> "#F472B6"
            lower.contains("indonesia") -> "#A78BFA"
            lower.contains("fisika") || lower.contains("ipa") || lower.contains("biologi") || lower.contains("kimia") -> "#34D399"
            lower.contains("ips") || lower.contains("sejarah") || lower.contains("geografi") -> "#FBBF24"
            lower.contains("informatika") || lower.contains("komputer") || lower.contains("coding") -> "#818CF8"
            lower.contains("olahraga") || lower.contains("penjas") -> "#FB923C"
            else -> "#38BDF8"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun generateFallbackScanResult(targetClass: String?): ScheduleScanResult {
        return ScheduleScanResult(
            isMultiClass = false,
            detectedClasses = emptyList(),
            targetClass = targetClass,
            targetClassFound = false,
            matchedClass = "",
            detectionNote = "Tidak ada hasil scan asli yang tersedia.",
            items = emptyList()
        )
    }
}
