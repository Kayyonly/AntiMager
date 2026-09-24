package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AntiMagerApp
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.UserSettingsRepository
import com.example.data.service.ExtractedScheduleItem
import com.example.data.service.GeminiScheduleVisionService
import com.example.data.service.ScheduleScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ScheduleViewMode {
    DAILY,
    WEEKLY
}

enum class DailySubTab {
    TODAY,
    TOMORROW
}

data class ScheduleUiState(
    val allSchedules: List<ScheduleEntity> = emptyList(),
    val displayedSchedules: List<ScheduleEntity> = emptyList(),
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAILY,
    val dailySubTab: DailySubTab = DailySubTab.TODAY,
    val selectedDayOfWeek: Int = 1, // 1=Senin ... 7=Minggu
    val currentDayOfWeek: Int = 1,
    val editingSchedule: ScheduleEntity? = null,
    val isFormOpen: Boolean = false,
    // User Settings / Profil Kelas
    val userClass: String = "X IPA 2",
    // Scan states
    val isScanning: Boolean = false,
    val scanErrorMessage: String? = null,
    val scannedBitmap: Bitmap? = null,
    val extractedItems: List<ExtractedScheduleItem> = emptyList(),
    val isConfirmDialogVisible: Boolean = false,
    // Multi-class specific scan states
    val isMultiClassDetected: Boolean = false,
    val detectedClassesInPhoto: List<String> = emptyList(),
    val targetClassFoundInPhoto: Boolean = true,
    val matchedClass: String = "",
    val detectionNote: String = "",
    val isClassMismatchDialogVisible: Boolean = false
)

private data class ScheduleDisplayData(
    val all: List<ScheduleEntity>,
    val displayed: List<ScheduleEntity>,
    val viewMode: ScheduleViewMode,
    val dailySub: DailySubTab,
    val selectedDay: Int,
    val currentDay: Int
)

private data class ScanStateData(
    val isScanning: Boolean,
    val errorMessage: String?,
    val scannedBitmap: Bitmap?,
    val extractedItems: List<ExtractedScheduleItem>,
    val isConfirmDialogVisible: Boolean,
    val isMultiClassDetected: Boolean,
    val detectedClassesInPhoto: List<String>,
    val targetClassFoundInPhoto: Boolean,
    val matchedClass: String,
    val detectionNote: String,
    val isClassMismatchDialogVisible: Boolean
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(AntiMagerApp.instance.database.scheduleDao())
    private val userSettingsRepo = UserSettingsRepository(AntiMagerApp.instance.database.userSettingsDao())
    private val visionService = GeminiScheduleVisionService()

    private val _viewMode = MutableStateFlow(ScheduleViewMode.DAILY)
    private val _dailySubTab = MutableStateFlow(DailySubTab.TODAY)
    private val _selectedDayOfWeek = MutableStateFlow(determineCurrentDayOfWeek())
    private val _editingSchedule = MutableStateFlow<ScheduleEntity?>(null)
    private val _isFormOpen = MutableStateFlow(false)

    // User Class Setting State
    private val _userClass = MutableStateFlow("X IPA 2")

    // Scan Vision state flows
    private val _isScanning = MutableStateFlow(false)
    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    private val _extractedItems = MutableStateFlow<List<ExtractedScheduleItem>>(emptyList())
    private val _isConfirmDialogVisible = MutableStateFlow(false)

    // Multi-class detection state flows
    private val _isMultiClassDetected = MutableStateFlow(false)
    private val _detectedClassesInPhoto = MutableStateFlow<List<String>>(emptyList())
    private val _targetClassFoundInPhoto = MutableStateFlow(true)
    private val _matchedClass = MutableStateFlow("")
    private val _detectionNote = MutableStateFlow("")
    private val _isClassMismatchDialogVisible = MutableStateFlow(false)

    init {
        // Collect user class from repository
        viewModelScope.launch {
            userSettingsRepo.userSettingsFlow.collect { settings ->
                _userClass.value = settings.userClass
            }
        }
    }

    private val _displayData = combine(
        repository.allSchedules,
        _viewMode,
        _dailySubTab,
        _selectedDayOfWeek
    ) { all, viewMode, dailySub, selectedDay ->
        val currentDay = determineCurrentDayOfWeek()
        val targetDay = when (viewMode) {
            ScheduleViewMode.DAILY -> {
                if (dailySub == DailySubTab.TODAY) {
                    currentDay
                } else {
                    if (currentDay == 7) 1 else currentDay + 1
                }
            }
            ScheduleViewMode.WEEKLY -> selectedDay
        }

        val filtered = all
            .filter { it.dayOfWeek == targetDay }
            .sortedBy { it.startTime }

        ScheduleDisplayData(
            all = all,
            displayed = filtered,
            viewMode = viewMode,
            dailySub = dailySub,
            selectedDay = selectedDay,
            currentDay = currentDay
        )
    }

    private val _scanData = combine(
        _isScanning,
        _scanErrorMessage,
        _scannedBitmap,
        _extractedItems,
        _isConfirmDialogVisible
    ) { isScanning, errorMsg, bitmap, items, isConfirmOpen ->
        Pair(
            Pair(isScanning, errorMsg),
            Triple(bitmap, items, isConfirmOpen)
        )
    }

    private val _multiClassData = combine(
        _isMultiClassDetected,
        _detectedClassesInPhoto,
        _targetClassFoundInPhoto,
        _matchedClass,
        _detectionNote
    ) { isMulti, detected, found, matched, note ->
        Pair(
            Triple(isMulti, detected, found),
            Pair(matched, note)
        )
    }

    val uiState: StateFlow<ScheduleUiState> = combine(
        _displayData,
        _editingSchedule,
        _isFormOpen,
        _userClass,
        combine(_scanData, _multiClassData, _isClassMismatchDialogVisible) { scan, multi, isMismatchOpen ->
            Triple(scan, multi, isMismatchOpen)
        }
    ) { display, editing, isForm, userCls, compositeScan ->
        val scan = compositeScan.first
        val multi = compositeScan.second
        val isMismatchOpen = compositeScan.third

        ScheduleUiState(
            allSchedules = display.all,
            displayedSchedules = display.displayed,
            viewMode = display.viewMode,
            dailySubTab = display.dailySub,
            selectedDayOfWeek = display.selectedDay,
            currentDayOfWeek = display.currentDay,
            editingSchedule = editing,
            isFormOpen = isForm,
            userClass = userCls,
            isScanning = scan.first.first,
            scanErrorMessage = scan.first.second,
            scannedBitmap = scan.second.first,
            extractedItems = scan.second.second,
            isConfirmDialogVisible = scan.second.third,
            isMultiClassDetected = multi.first.first,
            detectedClassesInPhoto = multi.first.second,
            targetClassFoundInPhoto = multi.first.third,
            matchedClass = multi.second.first,
            detectionNote = multi.second.second,
            isClassMismatchDialogVisible = isMismatchOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState()
    )

    fun setViewMode(mode: ScheduleViewMode) {
        _viewMode.value = mode
    }

    fun setDailySubTab(subTab: DailySubTab) {
        _dailySubTab.value = subTab
    }

    fun setSelectedDayOfWeek(day: Int) {
        _selectedDayOfWeek.value = day
    }

    fun openAddForm() {
        _editingSchedule.value = null
        _isFormOpen.value = true
    }

    fun openEditForm(schedule: ScheduleEntity) {
        _editingSchedule.value = schedule
        _isFormOpen.value = true
    }

    fun closeForm() {
        _isFormOpen.value = false
        _editingSchedule.value = null
    }

    fun saveSchedule(
        id: Long,
        dayOfWeek: Int,
        subject: String,
        startTime: String,
        endTime: String,
        roomOrTeacher: String,
        isBreak: Boolean,
        colorHex: String
    ) {
        viewModelScope.launch {
            val entity = ScheduleEntity(
                id = id,
                dayOfWeek = dayOfWeek,
                subject = subject.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                roomOrTeacher = roomOrTeacher.trim(),
                isBreak = isBreak,
                colorHex = if (isBreak) "#F59E0B" else colorHex
            )
            if (entity.id == 0L) {
                repository.insertSchedule(entity)
            } else {
                repository.updateSchedule(entity)
            }
            closeForm()
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun updateUserClass(newClass: String) {
        viewModelScope.launch {
            userSettingsRepo.updateUserClass(newClass)
        }
    }

    /**
     * Scan schedule photo with intelligent Multi-Class detection & filtering.
     * Automatically uses the user's active "Kelas Saya" preference.
     */
    fun scanSchedulePhoto(bitmap: Bitmap, customTargetClass: String? = null) {
        viewModelScope.launch {
            _scannedBitmap.value = bitmap
            _isScanning.value = true
            _scanErrorMessage.value = null
            _isClassMismatchDialogVisible.value = false

            val targetClass = customTargetClass ?: _userClass.value

            val result = visionService.extractScheduleFromBitmap(bitmap, targetClass)
            _isScanning.value = false

            result.onSuccess { scanResult ->
                _isMultiClassDetected.value = scanResult.isMultiClass
                _detectedClassesInPhoto.value = scanResult.detectedClasses
                _targetClassFoundInPhoto.value = scanResult.targetClassFound
                _matchedClass.value = scanResult.matchedClass
                _detectionNote.value = scanResult.detectionNote

                if (scanResult.isMultiClass && !scanResult.targetClassFound) {
                    // Kelas yang diset tidak ditemukan di foto!
                    // Munculkan dialog agar user bisa echo/ketik ulang atau pilih manual dari kelas yang terdeteksi
                    _extractedItems.value = emptyList()
                    _isConfirmDialogVisible.value = false
                    _isClassMismatchDialogVisible.value = true
                } else {
                    // Berhasil menemukan jadwal (single-class atau multi-class yang cocok)
                    _extractedItems.value = scanResult.items
                    _isConfirmDialogVisible.value = true
                    _isClassMismatchDialogVisible.value = false
                }
            }.onFailure { error ->
                _scanErrorMessage.value = error.message ?: "Gagal memindai jadwal pelajaran dari foto."
            }
        }
    }

    /**
     * User selects a manual class from the detected classes in the photo,
     * or typed a new class name. Re-runs filtering or updates the items!
     */
    fun onSelectClassFromPhoto(chosenClass: String, updateProfileClass: Boolean = false) {
        viewModelScope.launch {
            _isClassMismatchDialogVisible.value = false
            if (updateProfileClass) {
                userSettingsRepo.updateUserClass(chosenClass)
            }

            val currentBitmap = _scannedBitmap.value
            if (currentBitmap != null) {
                scanSchedulePhoto(currentBitmap, customTargetClass = chosenClass)
            } else {
                _extractedItems.value = emptyList()
                _isConfirmDialogVisible.value = false
                _scanErrorMessage.value = "Foto jadwal sudah tidak tersedia. Pilih fotonya lagi untuk scan ulang."
            }
        }
    }

    fun dismissClassMismatchDialog() {
        _isClassMismatchDialogVisible.value = false
    }

    fun updateExtractedItem(index: Int, updated: ExtractedScheduleItem) {
        val current = _extractedItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _extractedItems.value = current
        }
    }

    fun removeExtractedItem(index: Int) {
        val current = _extractedItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _extractedItems.value = current
            if (current.isEmpty()) {
                _isConfirmDialogVisible.value = false
            }
        }
    }

    fun confirmAndSaveExtractedSchedules(onComplete: (count: Int) -> Unit) {
        viewModelScope.launch {
            val itemsToSave = _extractedItems.value.map { it.toEntity() }
            if (itemsToSave.isNotEmpty()) {
                repository.insertAll(itemsToSave)
            }
            val count = itemsToSave.size
            _isConfirmDialogVisible.value = false
            _extractedItems.value = emptyList()
            _scannedBitmap.value = null
            onComplete(count)
        }
    }

    fun dismissConfirmDialog() {
        _isConfirmDialogVisible.value = false
        _extractedItems.value = emptyList()
        _scannedBitmap.value = null
        _scanErrorMessage.value = null
    }

    fun clearErrorMessage() {
        _scanErrorMessage.value = null
    }

    companion object {
        fun determineCurrentDayOfWeek(): Int {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        }

        fun getDayName(dayOfWeek: Int): String {
            return when (dayOfWeek) {
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
    }
}
