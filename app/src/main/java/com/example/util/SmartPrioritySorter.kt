package com.example.util

import com.example.data.local.entity.TaskEntity

enum class SortMode(val displayName: String, val icon: String) {
    SMART_AI("🔥 Smart AI", "smart"),
    DEADLINE("⏰ Deadline", "time"),
    DURATION("⏳ Durasi", "timer"),
    SUBJECT("📚 Mapel", "school")
}

data class TaskUrgencyInfo(
    val score: Double,
    val badgeLabel: String,
    val badgeColorHex: String,
    val humanTimeRemaining: String
)

object SmartPrioritySorter {

    fun calculateUrgency(task: TaskEntity, now: Long = System.currentTimeMillis()): TaskUrgencyInfo {
        if (task.isCompleted) {
            return TaskUrgencyInfo(
                score = -1.0,
                badgeLabel = "✅ Tuntas",
                badgeColorHex = "#34D399",
                humanTimeRemaining = "Selesai"
            )
        }

        val remainingMillis = task.deadlineEpochMillis - now
        val remainingHours = remainingMillis / (1000.0 * 60.0 * 60.0)

        // Base urgency from remaining time
        val timeScore = when {
            remainingHours <= 0 -> 200.0 // Overdue!
            remainingHours <= 3 -> 120.0 - (remainingHours * 10)
            remainingHours <= 12 -> 80.0 - (remainingHours * 2)
            remainingHours <= 24 -> 50.0 - (remainingHours * 0.8)
            remainingHours <= 48 -> 30.0 - (remainingHours * 0.3)
            else -> 10.0
        }

        // Duration load factor: tasks that take 60m+ when remaining time is short need attention!
        val durationHours = task.estimatedMinutes / 60.0
        val durationLoadScore = if (remainingHours > 0) {
            val loadRatio = durationHours / remainingHours
            (loadRatio * 40.0).coerceIn(0.0, 50.0)
        } else {
            30.0
        }

        // Priority weight
        val priorityScore = when (task.priority.uppercase()) {
            "HIGH" -> 30.0
            "MEDIUM" -> 15.0
            else -> 0.0
        }

        // Procrastination / Snooze factor: if snoozed often, boost score to wake them up
        val snoozePenalty = task.snoozeCount * 8.0

        val totalScore = timeScore + durationLoadScore + priorityScore + snoozePenalty

        val (badgeLabel, colorHex) = when {
            remainingHours <= 0 -> Pair("⚠️ Terlewat!", "#EF4444")
            totalScore >= 100 || remainingHours <= 4 -> Pair("🔥 Gawat! Kerjain Sekarang", "#F43F5E")
            totalScore >= 60 || remainingHours <= 18 -> Pair("⚡ Perlu Dicicil", "#F59E0B")
            else -> Pair("☕ Masih Santai", "#10B981")
        }

        val timeRemainingStr = formatRemainingTime(remainingMillis)

        return TaskUrgencyInfo(
            score = totalScore,
            badgeLabel = badgeLabel,
            badgeColorHex = colorHex,
            humanTimeRemaining = timeRemainingStr
        )
    }

    private fun formatRemainingTime(remainingMillis: Long): String {
        if (remainingMillis <= 0) return "Sudah lewat deadline"
        val minutes = remainingMillis / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days hari lagi"
            hours > 0 -> "$hours jam lagi"
            else -> "$minutes menit lagi!"
        }
    }

    fun sortTasks(tasks: List<TaskEntity>, mode: SortMode, now: Long = System.currentTimeMillis()): List<TaskEntity> {
        val (pending, completed) = tasks.partition { !it.isCompleted }

        val sortedPending = when (mode) {
            SortMode.SMART_AI -> {
                pending.sortedByDescending { calculateUrgency(it, now).score }
            }
            SortMode.DEADLINE -> {
                pending.sortedBy { it.deadlineEpochMillis }
            }
            SortMode.DURATION -> {
                pending.sortedByDescending { it.estimatedMinutes }
            }
            SortMode.SUBJECT -> {
                pending.sortedWith(compareBy({ it.subject }, { it.deadlineEpochMillis }))
            }
        }

        // Always keep completed at the bottom
        return sortedPending + completed.sortedByDescending { it.deadlineEpochMillis }
    }
}
