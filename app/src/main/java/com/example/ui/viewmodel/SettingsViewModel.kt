package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userClass: String = "X IPA 2",
    val studentName: String = "",
    val schoolName: String = "",
    val autoFilterClassSchedule: Boolean = true,
    val saveMessage: String? = null
) {
    val autoFilterSchedule: Boolean get() = autoFilterClassSchedule
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userSettingsRepo: UserSettingsRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        userSettingsRepo = UserSettingsRepository(database.userSettingsDao())

        viewModelScope.launch {
            userSettingsRepo.userSettingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        userClass = settings.userClass,
                        studentName = settings.studentName,
                        schoolName = settings.schoolName,
                        autoFilterClassSchedule = settings.autoFilterClassSchedule
                    )
                }
            }
        }
    }

    fun onClassChange(newClass: String) {
        _uiState.update { it.copy(userClass = newClass) }
    }

    fun onStudentNameChange(newName: String) {
        _uiState.update { it.copy(studentName = newName) }
    }

    fun onSchoolNameChange(newSchool: String) {
        _uiState.update { it.copy(schoolName = newSchool) }
    }

    fun onAutoFilterToggle(enabled: Boolean) {
        _uiState.update { it.copy(autoFilterClassSchedule = enabled) }
    }

    fun toggleAutoFilterSchedule(enabled: Boolean) = onAutoFilterToggle(enabled)

    fun saveProfile() = saveSettings()

    fun saveSettings(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val entity = UserSettingsEntity(
                id = 1,
                userClass = currentState.userClass.trim().ifBlank { "X IPA 2" },
                studentName = currentState.studentName.trim(),
                schoolName = currentState.schoolName.trim(),
                autoFilterClassSchedule = currentState.autoFilterClassSchedule
            )
            userSettingsRepo.saveUserSettings(entity)
            _uiState.update { it.copy(saveMessage = "Profil kelas berhasil disimpan! ✅") }
            onSuccess?.invoke()
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}
