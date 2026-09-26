package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AntiMagerApp
import com.example.data.ai.AiTaskParserService
import com.example.data.ai.ParsedTaskResult
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.util.LocationReminderManager
import com.example.util.TaskReminderScheduler
import com.example.widget.AntiMagerWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val parsedTask: ParsedTaskResult? = null,
    val isSavedToDatabase: Boolean = false
)

enum class ChatSender {
    USER, AI
}

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isThinking: Boolean = false,
    val inputText: String = ""
)

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(AntiMagerApp.instance.database.taskDao())
    private var lastAmbiguousContext: String? = null

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            messages = listOf(
                ChatMessage(
                    sender = ChatSender.AI,
                    text = "Halo sobat mager! 👋 Ceritakan tugas atau hal yang harus kamu kerjakan. Misalnya:\n• *PR IPS besok*\n• *Kerjain matematika jam 8 malam*\n• *Jumat kumpul tugas IPA*\n• *Nanti sore ingetin beli buku*\n• *Besok sebelum sekolah bawa seragam olahraga*\n\nBiar aku yang parse otomatis jadi pengingat rapi!"
                )
            )
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendPrompt(prompt: String = _uiState.value.inputText) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        val userMessage = ChatMessage(sender = ChatSender.USER, text = trimmed)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isThinking = true
        )

        viewModelScope.launch {
            delay(180)

            if (!looksLikeTaskRequest(trimmed)) {
                lastAmbiguousContext = null
                val reply = AiTaskParserService.chatWithExternalApi(trimmed)
                    ?: localChatFallback(trimmed)

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + ChatMessage(
                        sender = ChatSender.AI,
                        text = reply
                    ),
                    isThinking = false
                )
                return@launch
            }

            val contextToUse = lastAmbiguousContext
            val parsedResult = AiTaskParserService.parseWithExternalApi(trimmed, contextToUse)
                ?: AiTaskParserService.parseStory(trimmed, contextToUse)

            if (parsedResult.isAmbiguous) {
                lastAmbiguousContext = trimmed
                val question = parsedResult.clarificationQuestion
                    ?: "Tugas apa yang mau dikerjakan? Sebut judul atau mapelnya."
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + ChatMessage(
                        sender = ChatSender.AI,
                        text = question
                    ),
                    isThinking = false
                )
            } else {
                lastAmbiguousContext = null
                val aiMessage = ChatMessage(
                    sender = ChatSender.AI,
                    text = parsedResult.aiAdvice.ifBlank { "Sip, tugasnya sudah kebaca." },
                    parsedTask = parsedResult
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isThinking = false
                )
            }
        }
    }

    private fun looksLikeTaskRequest(text: String): Boolean {
        val lower = text.lowercase()

        val actionWords = listOf(
            "pr ", "tugas", "kerjain", "kerjakan", "ngerjain", "ingatkan", "ingetin",
            "jangan lupa", "deadline", "kumpul", "kumpulin", "beli ", "bawa ", "buat ",
            "selesaikan", "belajar", "latihan", "rapat", "meeting"
        )
        val timeWords = listOf(
            "besok", "lusa", "nanti", "pagi", "siang", "sore", "malam", "jam ",
            "senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu"
        )
        val subjectWords = listOf(
            "matematika", "mtk", "ips", "ipa", "biologi", "fisika", "kimia",
            "bahasa inggris", "bahasa indonesia", "informatika", "agama", "pkn", "seni"
        )

        val questionLike = lower.startsWith("apa ") ||
            lower.startsWith("siapa ") ||
            lower.startsWith("kenapa ") ||
            lower.startsWith("mengapa ") ||
            lower.startsWith("gimana ") ||
            lower.startsWith("bagaimana ") ||
            lower.startsWith("kamu ")

        val hasAction = actionWords.any { lower.contains(it) }
        val hasTime = timeWords.any { lower.contains(it) }
        val hasSubject = subjectWords.any { lower.contains(it) }

        if (questionLike && !hasAction) return false
        return hasAction || (hasTime && hasSubject)
    }

    private fun localChatFallback(text: String): String {
        val lower = text.lowercase()
        return when {
            lower in setOf("hai", "halo", "hi", "hey", "p", "hii", "hallo", "oi", "oe", "woi", "bro", "cuy") ->
                listOf(
                    "Oi, ada apa?",
                    "Yo. Mau ngobrol atau ada tugas yang mau diberesin?",
                    "Hadir. Ada yang bisa gue bantu?"
                ).random()
            lower.contains("ai apa") || lower.contains("model apa") || lower.contains("pake ai") ->
                "Untuk chat teks aku pakai Groq dengan Llama 3.3 70B. Scan foto jadwal pakai Gemini Vision."
            lower.contains("ngobrol") || lower.contains("chat") ->
                "Boleh. Ngobrol aja, gue nggak bakal ubah percakapan biasa jadi tugas kecuali kamu memang minta dibuatkan pengingat."
            else ->
                "Gue nangkep itu sebagai obrolan biasa. Lanjut aja—kalau mau bikin tugas, tinggal bilang contohnya “PR IPS besok jam 8 pagi”."
        }
    }

    fun saveTaskToDatabase(messageId: String, parsed: ParsedTaskResult) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = parsed.title,
                subject = parsed.subject,
                description = parsed.description,
                deadlineEpochMillis = parsed.deadlineMillis,
                estimatedMinutes = parsed.estimatedMinutes,
                priority = parsed.priority,
                isCompleted = false,
                isPersistent = true,
                locationName = parsed.locationTag,
                locationTrigger = if (parsed.locationTag.isNullOrBlank()) null else "ENTER"
            )
            val newId = repository.insertTask(task)
            val createdTask = task.copy(id = newId)
            TaskReminderScheduler.schedule(getApplication(), createdTask)
            if (!createdTask.locationName.isNullOrBlank()) {
                LocationReminderManager.refreshGeofencesIfActive(getApplication())
            }
            AntiMagerWidgetProvider.sendUpdateBroadcast(getApplication())

            // Update message state as saved
            val updatedMessages = _uiState.value.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(isSavedToDatabase = true)
                } else {
                    msg
                }
            }
            _uiState.value = _uiState.value.copy(messages = updatedMessages)
        }
    }
}
