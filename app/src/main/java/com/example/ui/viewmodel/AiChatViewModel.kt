package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AntiMagerApp
import com.example.data.ai.AiTaskParserService
import com.example.data.ai.ParsedTaskResult
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.util.NotificationHelper
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

        val userMessage = ChatMessage(
            sender = ChatSender.USER,
            text = trimmed
        )

        val updatedList = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedList,
            inputText = "",
            isThinking = true
        )

        viewModelScope.launch {
            delay(300)

            val contextToUse = lastAmbiguousContext
            // Parse with external API or fallback to Indonesian smart parser
            val parsedResult = AiTaskParserService.parseWithExternalApi(trimmed, contextToUse)
                ?: AiTaskParserService.parseStory(trimmed, contextToUse)

            if (parsedResult.isAmbiguous) {
                // Ambiguous input: ask follow-up clarification and remember context
                lastAmbiguousContext = trimmed
                val question = parsedResult.clarificationQuestion ?: "Tugas apa yang mau dikerjakan? Ceritakan judul atau mapelnya ya!"
                val aiMessage = ChatMessage(
                    sender = ChatSender.AI,
                    text = question,
                    parsedTask = null,
                    isSavedToDatabase = false
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isThinking = false
                )
            } else {
                // Clear ambiguous context now that full task is resolved
                lastAmbiguousContext = null
                val aiResponseText = buildString {
                    append("Siap! Udah aku uraikan jadi jadwal rapi nih 👇\n\n")
                    append(parsedResult.aiAdvice)
                }

                val aiMessage = ChatMessage(
                    sender = ChatSender.AI,
                    text = aiResponseText,
                    parsedTask = parsedResult,
                    isSavedToDatabase = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isThinking = false
                )
            }
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
                locationName = parsed.locationTag
            )
            val newId = repository.insertTask(task)
            NotificationHelper.showPersistentReminderNotification(getApplication(), task.copy(id = newId))
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
