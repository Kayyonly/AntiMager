package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String = "Umum",
    val description: String = "",
    val deadlineEpochMillis: Long,
    val estimatedMinutes: Int = 30,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val isPersistent: Boolean = true,
    val snoozeCount: Int = 0,
    val locationName: String? = null,
    val locationTrigger: String? = null, // "ENTER", "EXIT"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val aiMotivationQuote: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
