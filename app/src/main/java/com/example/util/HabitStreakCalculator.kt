package com.example.util

import com.example.data.local.entity.HabitEntity
import java.util.Calendar

object HabitStreakCalculator {

    /**
     * Converts calendar year/month/day to civil epoch day (days since 1970-01-01),
     * strictly respecting the device's local calendar & timezone.
     */
    fun getLocalEpochDay(cal: Calendar = Calendar.getInstance()): Long {
        val y = cal.get(Calendar.YEAR).toLong()
        val m = (cal.get(Calendar.MONTH) + 1).toLong()
        val d = cal.get(Calendar.DAY_OF_MONTH).toLong()
        val a = (14 - m) / 12
        val yAdj = y + 4800 - a
        val mAdj = m + 12 * a - 3
        val jdn = d + (153 * mAdj + 2) / 5 + 365 * yAdj + yAdj / 4 - yAdj / 100 + yAdj / 400 - 32045
        return jdn - 2440588L
    }

    /**
     * Parses completedDaysCsv into a sorted set of epoch days.
     */
    fun parseCompletedDays(csv: String): Set<Long> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .toSet()
    }

    /**
     * Converts a set of epoch days back to clean comma-separated string.
     */
    fun toCsv(days: Set<Long>): String {
        return days.sorted().joinToString(",")
    }

    /**
     * Calculates the active current streak count backwards from today (or yesterday).
     */
    fun calculateCurrentStreak(completedDays: Set<Long>, todayEpochDay: Long = getLocalEpochDay()): Int {
        if (completedDays.isEmpty()) return 0

        // If today is completed, count backwards starting from today
        // If today is NOT completed, but yesterday is completed, the streak is still alive!
        val startDay = when {
            completedDays.contains(todayEpochDay) -> todayEpochDay
            completedDays.contains(todayEpochDay - 1) -> todayEpochDay - 1
            else -> return 0 // Streak broken!
        }

        var streak = 0
        var checkDay = startDay
        while (completedDays.contains(checkDay)) {
            streak++
            checkDay--
        }
        return streak
    }

    /**
     * Calculates the longest historical streak in the user's completion records.
     */
    fun calculateBestStreak(completedDays: Set<Long>, previousBest: Int = 0): Int {
        if (completedDays.isEmpty()) return previousBest
        val sortedList = completedDays.sorted()

        var maxRun = 0
        var currentRun = 0
        var prevDay: Long? = null

        for (day in sortedList) {
            if (prevDay == null || day == prevDay + 1) {
                currentRun++
            } else if (day > prevDay + 1) {
                currentRun = 1
            }
            if (currentRun > maxRun) {
                maxRun = currentRun
            }
            prevDay = day
        }

        return maxOf(maxRun, previousBest)
    }

    /**
     * Performs a check-in or uncheck (undo) on a habit, recalculating streak and best streak.
     */
    fun toggleCheckIn(habit: HabitEntity, todayEpochDay: Long = getLocalEpochDay()): HabitEntity {
        val currentDays = parseCompletedDays(habit.completedDaysCsv).toMutableSet()
        // If already completed today: undo completion
        if (currentDays.contains(todayEpochDay)) {
            currentDays.remove(todayEpochDay)
            val newStreak = calculateCurrentStreak(currentDays, todayEpochDay)
            val bestStreak = calculateBestStreak(currentDays, habit.bestStreak)
            val lastDay = currentDays.maxOrNull() ?: 0L

            return habit.copy(
                streakCount = newStreak,
                bestStreak = bestStreak,
                lastCompletedEpochDay = lastDay,
                completedDaysCsv = toCsv(currentDays)
            )
        } else {
            // Mark today completed
            currentDays.add(todayEpochDay)
            val newStreak = calculateCurrentStreak(currentDays, todayEpochDay)
            val bestStreak = maxOf(habit.bestStreak, newStreak)

            return habit.copy(
                streakCount = newStreak,
                bestStreak = bestStreak,
                lastCompletedEpochDay = todayEpochDay,
                completedDaysCsv = toCsv(currentDays)
            )
        }
    }
}
