package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "bolt", // water, book, clean, sleep, check, bolt
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedEpochDay: Long = 0, // LocalDate.now().toEpochDay()
    val colorHex: String = "#38BDF8",
    val createdAt: Long = System.currentTimeMillis()
)
