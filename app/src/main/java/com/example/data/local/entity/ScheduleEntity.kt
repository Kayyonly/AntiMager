package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Senin, 2 = Selasa, 3 = Rabu, 4 = Kamis, 5 = Jumat, 6 = Sabtu, 7 = Minggu
    val subject: String,
    val startTime: String, // e.g. "07:15"
    val endTime: String,   // e.g. "08:45"
    val roomOrTeacher: String = "",
    val isBreak: Boolean = false, // Slot khusus Istirahat
    val colorHex: String = "#38BDF8",
    val sortOrder: Int = 0
)
