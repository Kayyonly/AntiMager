package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AntiMagerApp
import com.example.data.local.entity.HabitEntity
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitUiState(
    val habits: List<HabitEntity> = emptyList(),
    val totalActiveStreaks: Int = 0,
    val bestStreakOverall: Int = 0,
    val todayCompletedCount: Int = 0
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(AntiMagerApp.instance.database.habitDao())

    val uiState: StateFlow<HabitUiState> = repository.allHabits.map { list ->
        val todayEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val todayCompleted = list.count { it.lastCompletedEpochDay == todayEpochDay }
        val maxStreak = list.maxOfOrNull { it.bestStreak } ?: 0
        val activeStreakCount = list.count { it.streakCount > 0 }

        HabitUiState(
            habits = list,
            totalActiveStreaks = activeStreakCount,
            bestStreakOverall = maxStreak,
            todayCompletedCount = todayCompleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )

    fun checkIn(habitId: Long) {
        viewModelScope.launch {
            repository.checkInHabit(habitId)
        }
    }

    fun addHabit(name: String, description: String = "", iconName: String = "bolt", colorHex: String = "#38BDF8") {
        viewModelScope.launch {
            val habit = HabitEntity(
                name = name.trim(),
                description = description.trim(),
                iconName = iconName,
                streakCount = 0,
                bestStreak = 0,
                lastCompletedEpochDay = 0,
                colorHex = colorHex
            )
            repository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
