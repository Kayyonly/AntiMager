package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AntiMagerApp
import com.example.data.ai.AiTaskParserService
import com.example.data.ai.ParsedTaskResult
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.data.service.AiPrioritySortResult
import com.example.data.service.GroqPriorityService
import com.example.util.LocationReminderManager
import com.example.util.NotificationHelper
import com.example.util.SmartPrioritySorter
import com.example.util.SortMode
import com.example.util.TaskReminderScheduler
import com.example.widget.AntiMagerWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("Semua"),
    PENDING("Belum Selesai"),
    COMPLETED("Selesai")
}

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val totalPending: Int = 0,
    val urgentCount: Int = 0,
    val sortMode: SortMode = SortMode.SMART_AI,
    val filter: TaskFilter = TaskFilter.PENDING,
    val searchQuery: String = "",
    val activeLocation: String? = null,
    val lastLocationMessage: String? = null,
    val voiceResultPrompt: String? = null,
    val isAiSortingLoading: Boolean = false,
    val aiModelUsed: String? = null,
    val aiGlobalAdvice: String? = null,
    val taskAiRationaleMap: Map<Long, String> = emptyMap(),
    val taskAiBadgeMap: Map<Long, Pair<String, String>> = emptyMap()
)

private data class FilteredTaskData(
    val tasks: List<TaskEntity>,
    val pendingCount: Int,
    val urgentCount: Int,
    val sortMode: SortMode,
    val filter: TaskFilter,
    val searchQuery: String,
    val isAiSortingLoading: Boolean,
    val aiSortResult: AiPrioritySortResult?
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(AntiMagerApp.instance.database.taskDao())

    private val _sortMode = MutableStateFlow(SortMode.SMART_AI)
    private val _filter = MutableStateFlow(TaskFilter.PENDING)
    private val _searchQuery = MutableStateFlow("")
    private val _activeLocation = MutableStateFlow<String?>(null)
    private val _lastLocationMessage = MutableStateFlow<String?>(null)
    private val _voiceParsedTask = MutableStateFlow<ParsedTaskResult?>(null)
    private val _isAiSortingLoading = MutableStateFlow(false)
    private val _aiSortResult = MutableStateFlow<AiPrioritySortResult?>(null)

    val voiceParsedTask: StateFlow<ParsedTaskResult?> = _voiceParsedTask

    @Suppress("UNCHECKED_CAST")
    private val _filteredData = combine(
        repository.allTasks,
        _sortMode,
        _filter,
        _searchQuery,
        _isAiSortingLoading,
        _aiSortResult
    ) { args: Array<Any?> ->
        val allTasks = args[0] as List<TaskEntity>
        val sortMode = args[1] as SortMode
        val filter = args[2] as TaskFilter
        val search = args[3] as String
        val isAiLoading = args[4] as Boolean
        val aiResult = args[5] as? AiPrioritySortResult
        val now = System.currentTimeMillis()

        // 1. Filter by status
        val filteredByStatus = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> allTasks.filter { it.isCompleted }
        }

        // 2. Filter by search query
        val filteredBySearch = if (search.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.title.contains(search, ignoreCase = true) ||
                it.subject.contains(search, ignoreCase = true) ||
                (it.locationName?.contains(search, ignoreCase = true) == true)
            }
        }

        // 3. Smart Priority Sorting:
        val sorted = if (sortMode == SortMode.SMART_AI && aiResult != null && aiResult.prioritizedTasks.isNotEmpty()) {
            val (pending, completed) = filteredBySearch.partition { !it.isCompleted }
            val rankMap = aiResult.prioritizedTasks.associate { it.task.id to it.rank }
            val sortedPending = pending.sortedBy { rankMap[it.id] ?: 999 }
            sortedPending + completed.sortedByDescending { it.deadlineEpochMillis }
        } else {
            SmartPrioritySorter.sortTasks(filteredBySearch, sortMode, now)
        }

        val pendingCount = allTasks.count { !it.isCompleted }
        val urgentCount = allTasks.count { !it.isCompleted && SmartPrioritySorter.calculateUrgency(it, now).score >= 60 }

        FilteredTaskData(
            tasks = sorted,
            pendingCount = pendingCount,
            urgentCount = urgentCount,
            sortMode = sortMode,
            filter = filter,
            searchQuery = search,
            isAiSortingLoading = isAiLoading,
            aiSortResult = aiResult
        )
    }

    val uiState: StateFlow<TaskUiState> = combine(
        _filteredData,
        _activeLocation,
        _lastLocationMessage
    ) { filtered, activeLoc, locMsg ->
        val rationaleMap = filtered.aiSortResult?.prioritizedTasks?.associate {
            it.task.id to it.aiRationale
        } ?: emptyMap()

        val badgeMap = filtered.aiSortResult?.prioritizedTasks?.associate {
            it.task.id to Pair(it.badgeLabel, it.badgeColorHex)
        } ?: emptyMap()

        TaskUiState(
            tasks = filtered.tasks,
            totalPending = filtered.pendingCount,
            urgentCount = filtered.urgentCount,
            sortMode = filtered.sortMode,
            filter = filtered.filter,
            searchQuery = filtered.searchQuery,
            activeLocation = activeLoc,
            lastLocationMessage = locMsg,
            isAiSortingLoading = filtered.isAiSortingLoading,
            aiModelUsed = filtered.aiSortResult?.modelName,
            aiGlobalAdvice = filtered.aiSortResult?.globalAdvice,
            taskAiRationaleMap = rationaleMap,
            taskAiBadgeMap = badgeMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    init {
        // Automatically perform smart priority sorting on startup
        triggerGroqAiSort()
    }

    fun triggerGroqAiSort() {
        viewModelScope.launch {
            _isAiSortingLoading.value = true
            try {
                val currentTasks = repository.allTasks.first()
                val result = GroqPriorityService.sortTasksWithGroq(currentTasks)
                _aiSortResult.value = result
            } catch (e: Exception) {
                // ignore
            } finally {
                _isAiSortingLoading.value = false
            }
        }
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        if (mode == SortMode.SMART_AI && _aiSortResult.value == null) {
            triggerGroqAiSort()
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.setTaskCompleted(task.id, newCompleted)
            if (newCompleted) {
                NotificationHelper.dismissNotification(getApplication(), task.id)
                TaskReminderScheduler.cancel(getApplication(), task.id)
            } else {
                TaskReminderScheduler.schedule(getApplication(), task.copy(isCompleted = false))
            }
            AntiMagerWidgetProvider.sendUpdateBroadcast(getApplication())
            triggerGroqAiSort()
        }
    }

    fun snoozeTask(task: TaskEntity, minutes: Int = 15) {
        viewModelScope.launch {
            val newDeadline = System.currentTimeMillis() + (minutes * 60 * 1000L)
            repository.snoozeTask(task.id, newDeadline)
            val updated = task.copy(
                deadlineEpochMillis = newDeadline,
                snoozeCount = task.snoozeCount + 1
            )
            NotificationHelper.dismissNotification(getApplication(), task.id)
            TaskReminderScheduler.schedule(getApplication(), updated)
            AntiMagerWidgetProvider.sendUpdateBroadcast(getApplication())
            triggerGroqAiSort()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            NotificationHelper.dismissNotification(getApplication(), task.id)
            TaskReminderScheduler.cancel(getApplication(), task.id)
            AntiMagerWidgetProvider.sendUpdateBroadcast(getApplication())
            triggerGroqAiSort()
        }
    }

    fun triggerPersistentNotification(task: TaskEntity) {
        NotificationHelper.showPersistentReminderNotification(getApplication(), task)
    }

    fun addTask(
        title: String,
        subject: String = "Umum",
        description: String = "",
        deadlineMillis: Long,
        estimatedMinutes: Int = 30,
        priority: String = "MEDIUM",
        isPersistent: Boolean = true,
        locationName: String? = null,
        locationTrigger: String? = null
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                subject = subject.trim().ifBlank { "Umum" },
                description = description.trim(),
                deadlineEpochMillis = deadlineMillis,
                estimatedMinutes = estimatedMinutes,
                priority = priority,
                isCompleted = false,
                isPersistent = isPersistent,
                locationName = locationName?.trim()?.ifBlank { null },
                locationTrigger = if (locationName.isNullOrBlank()) null else (locationTrigger ?: "ENTER")
            )
            val newId = repository.insertTask(task)
            val createdTask = task.copy(id = newId)
            TaskReminderScheduler.schedule(getApplication(), createdTask)
            if (!createdTask.locationName.isNullOrBlank()) {
                LocationReminderManager.refreshGeofencesIfActive(getApplication())
            }
            AntiMagerWidgetProvider.sendUpdateBroadcast(getApplication())
            // Re-run AI sort to factor in the new task
            triggerGroqAiSort()
        }
    }

    fun handleVoiceTranscription(spokenText: String) {
        viewModelScope.launch {
            val parsed = AiTaskParserService.parseWithExternalApi(spokenText)
                ?: AiTaskParserService.parseStory(spokenText)
            _voiceParsedTask.value = parsed
        }
    }

    fun clearVoiceParsedTask() {
        _voiceParsedTask.value = null
    }

    fun commitVoiceTask(parsed: ParsedTaskResult) {
        addTask(
            title = parsed.title,
            subject = parsed.subject,
            description = parsed.description,
            deadlineMillis = parsed.deadlineMillis,
            estimatedMinutes = parsed.estimatedMinutes,
            priority = parsed.priority,
            isPersistent = true,
            locationName = parsed.locationTag
        )
        _voiceParsedTask.value = null
    }

    fun simulateLocationEvent(locationName: String, isEnter: Boolean) {
        LocationReminderManager.simulateAreaEvent(
            context = getApplication(),
            locationName = locationName,
            isEnter = isEnter
        ) { message ->
            _lastLocationMessage.value = message
            _activeLocation.value = if (isEnter) locationName else null
        }
    }

    fun clearLocationMessage() {
        _lastLocationMessage.value = null
    }
}
