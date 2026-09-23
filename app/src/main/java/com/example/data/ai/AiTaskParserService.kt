package com.example.data.ai

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    val aiAdvice: String = ""
)

object AiTaskParserService {

    /**
     * PLACEHOLDER FUNCTION UNTUK API EKSTERNAL (MISAL GEMINI / REST API SERVER):
     * Nantinya fungsi ini bisa memanggil endpoint backend atau Firebase AI / Gemini API
     * dengan prompt terstruktur (Structured JSON Output).
     */
    suspend fun parseWithExternalApi(userStory: String): ParsedTaskResult? {
        // --- TODO: Sambungkan ke endpoint API eksternal di sini ---
        // Contoh implementasi:
        // val response = myRetrofitApi.parsePrompt(PromptRequest(text = userStory))
        // return response.toParsedTaskResult()
        
        // Untuk sekarang, kita fallback secara cerdas ke local smart Indonesian parser:
        return null
    }

    /**
     * Smart Indonesian Natural Language Parser
     * Berjalan secara offline & instan dengan mengenali pola bahasa sehari-hari:
     * "PR IPS besok pagi jam 8", "Nanti malam rangkum biologi 30 mnt di rumah", dll.
     */
    fun parseStory(userStory: String): ParsedTaskResult {
        val lower = userStory.lowercase(Locale.ROOT).trim()

        // 1. Ekstrak Mata Pelajaran / Kategori
        val subject = extractSubject(lower)

        // 2. Ekstrak Waktu & Deadline
        val (deadlineMillis, timeString) = extractDeadline(lower)

        // 3. Ekstrak Estimasi Durasi Pengerjaan
        val estimatedMinutes = extractDuration(lower)

        // 4. Ekstrak Urgensi / Prioritas
        val priority = extractPriority(lower, deadlineMillis)

        // 5. Ekstrak Lokasi
        val locationTag = extractLocation(lower)

        // 6. Rapikan Judul Tugas
        val cleanTitle = buildCleanTitle(userStory, subject)

        // 7. Pesan & Tips Anti-Mager
        val advice = generateAntiMagerAdvice(priority, estimatedMinutes, subject)

        return ParsedTaskResult(
            title = cleanTitle,
            subject = subject,
            description = userStory,
            deadlineMillis = deadlineMillis,
            deadlineFormatted = timeString,
            estimatedMinutes = estimatedMinutes,
            priority = priority,
            locationTag = locationTag,
            aiAdvice = advice
        )
    }

    private fun extractSubject(text: String): String {
        return when {
            text.contains("ips") || text.contains("sosial") -> "IPS"
            text.contains("ipa") || text.contains("biologi") || text.contains("fisika") || text.contains("kimia") -> {
                when {
                    text.contains("biologi") -> "Biologi"
                    text.contains("fisika") -> "Fisika"
                    text.contains("kimia") -> "Kimia"
                    else -> "IPA"
                }
            }
            text.contains("matematika") || text.contains("mtk") || text.contains("kalkulus") || text.contains("aljabar") -> "Matematika"
            text.contains("bahasa indonesia") || text.contains("b.indo") || text.contains("b indo") -> "B. Indonesia"
            text.contains("bahasa inggris") || text.contains("b.inggris") || text.contains("b inggris") || text.contains("english") -> "B. Inggris"
            text.contains("sejarah") -> "Sejarah"
            text.contains("pkn") || text.contains("kewarganegaraan") -> "PKN"
            text.contains("agama") -> "Agama"
            text.contains("seni") || text.contains("gambar") -> "Seni Budaya"
            text.contains("olahraga") || text.contains("penjas") -> "Penjasorkes"
            text.contains("coding") || text.contains("koding") || text.contains("pemrograman") || text.contains("it") -> "Informatika"
            text.contains("beli") || text.contains("belanja") || text.contains("indomaret") || text.contains("alfamart") -> "Belanja"
            text.contains("kerjaan") || text.contains("kantor") || text.contains("meeting") || text.contains("laporan") -> "Pekerjaan"
            text.contains("rumah") || text.contains("cuci") || text.contains("beres") || text.contains("sapu") -> "Rumah"
            else -> "Umum"
        }
    }

    private fun extractDeadline(text: String): Pair<Long, String> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        // Cek hari: besok, lusa, nanti malam, hari ini
        var daysToAdd = 0
        var targetHour = 20 // default malam jika tidak spesifik
        var targetMinute = 0

        when {
            text.contains("lusa") -> {
                daysToAdd = 2
                targetHour = 10
            }
            text.contains("besok") -> {
                daysToAdd = 1
                when {
                    text.contains("pagi") -> targetHour = 8
                    text.contains("siang") -> targetHour = 13
                    text.contains("sore") -> targetHour = 16
                    text.contains("malam") -> targetHour = 20
                    else -> targetHour = 9
                }
            }
            text.contains("nanti malam") || text.contains("malam ini") -> {
                daysToAdd = 0
                targetHour = 20
            }
            text.contains("nanti sore") || text.contains("sore ini") -> {
                daysToAdd = 0
                targetHour = 17
            }
            text.contains("nanti siang") || text.contains("siang ini") -> {
                daysToAdd = 0
                targetHour = 13
            }
            text.contains("sebentar lagi") || text.contains("nanti") -> {
                cal.add(Calendar.HOUR_OF_DAY, 2)
                return Pair(cal.timeInMillis, "2 Jam Lagi")
            }
            else -> {
                // Default besok siang jika tidak ada indikasi
                daysToAdd = 1
                targetHour = 12
            }
        }

        // Cek pola jam spesifik: "jam 8", "jam 08:30", "pukul 15"
        val jamPattern = Pattern.compile("(?:jam|pukul)\\s*(\\d{1,2})(?:[:.](\\d{1,2}))?", Pattern.CASE_INSENSITIVE)
        val matcher = jamPattern.matcher(text)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: targetHour
            val m = matcher.group(2)?.toIntOrNull() ?: 0
            // Koreksi jika format 12 jam (misal "jam 8 malam" -> 20)
            targetHour = if (text.contains("malam") && h < 12) h + 12 else if (text.contains("sore") && h in 1..6) h + 12 else h
            targetMinute = m
        }

        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        cal.set(Calendar.HOUR_OF_DAY, targetHour.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, targetMinute.coerceIn(0, 59))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Jika waktu yang dihitung sudah lewat dari hari ini, majukan 1 hari
        if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val sdf = SimpleDateFormat("EEE, dd MMM - HH:mm", Locale.forLanguageTag("id-ID"))
        val formatted = when {
            daysToAdd == 0 && (text.contains("malam") || text.contains("sore") || text.contains("siang")) -> "Hari ini, ${String.format(Locale.ROOT, "%02d:%02d", targetHour, targetMinute)} WIB"
            daysToAdd == 1 || text.contains("besok") -> "Besok, ${String.format(Locale.ROOT, "%02d:%02d", targetHour, targetMinute)} WIB"
            daysToAdd == 2 || text.contains("lusa") -> "Lusa, ${String.format(Locale.ROOT, "%02d:%02d", targetHour, targetMinute)} WIB"
            else -> sdf.format(cal.time)
        }

        return Pair(cal.timeInMillis, formatted)
    }

    private fun extractDuration(text: String): Int {
        val menitPattern = Pattern.compile("(\\d+)\\s*(?:menit|mnt|m)", Pattern.CASE_INSENSITIVE)
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
            text.contains("bentar") || text.contains("sebentar") || text.contains("cepat") -> 15
            text.contains("lama") || text.contains("banyak") || text.contains("makalah") -> 60
            else -> 30 // default 30 menit
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
            text.contains("indomaret") || text.contains("alfamart") || text.contains("minimarket") -> "Minimarket"
            text.contains("perpus") || text.contains("perpustakaan") -> "Perpustakaan"
            text.contains("rumah") || text.contains("kost") || text.contains("kos") -> "Rumah"
            text.contains("kantor") -> "Kantor"
            else -> null
        }
    }

    private fun buildCleanTitle(raw: String, subject: String): String {
        // Hilangkan kata-kata filler umum untuk judul yang rapi
        var cleaned = raw
        val fillers = listOf(
            "tolong ingetin", "ingetin dong", "ingetin ya", "ingatkan", "aku ada",
            "mau ngerjain", "nanti", "besok", "lusa", "jam", "pukul", "hari ini"
        )
        for (filler in fillers) {
            cleaned = cleaned.replace("(?i)$filler".toRegex(), "")
        }
        cleaned = cleaned.trim().replace(Regex("\\s+"), " ")
        if (cleaned.length < 3) {
            cleaned = if (subject != "Umum") "Tugas $subject" else raw.take(30)
        }
        return cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    private fun generateAntiMagerAdvice(priority: String, duration: Int, subject: String): String {
        return when {
            priority == "HIGH" -> "🚨 Deadline mepet banget! Trik anti-mager: pasang timer $duration menit, jauhkan HP, kerjain tanpa mikir berat dulu!"
            duration <= 20 -> "⚡ Cuma $duration menit doang kok! Kerjain sekarang biar pikiran plong bebas santuy seharian."
            subject in listOf("IPS", "Sejarah", "PKN") -> "📖 Buat tugas bacaan $subject, cukup baca poin pentingnya dulu sambil rebahan 10 menit."
            subject in listOf("Matematika", "Fisika", "Kimia") -> "📐 Mulai dari 1 nomor paling gampang dulu. Begitu jalan, biasanya magernya langsung hilang!"
            else -> "💡 Ingat: lebih baik tugas selesai daripada sempurna tapi nggak dikumpul!"
        }
    }
}
