package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val userClass: String = "",
    val studentName: String = "",
    val schoolName: String = "",
    val autoFilterClassSchedule: Boolean = true
)
