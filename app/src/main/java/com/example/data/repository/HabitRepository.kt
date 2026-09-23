package com.example.data.repository

import com.example.data.local.dao.HabitDao
import com.example.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)

    suspend fun checkInHabit(id: Long) {
        val habit = habitDao.getHabitById(id) ?: return
        val todayEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

        if (habit.lastCompletedEpochDay == todayEpochDay) {
            // Already checked in today, uncheck
            val newStreak = (habit.streakCount - 1).coerceAtLeast(0)
            val updated = habit.copy(
                streakCount = newStreak,
                lastCompletedEpochDay = todayEpochDay - 1
            )
            habitDao.updateHabit(updated)
        } else {
            // Check if streak is continuous (yesterday checkin)
            val newStreak = if (habit.lastCompletedEpochDay == todayEpochDay - 1) {
                habit.streakCount + 1
            } else {
                1
            }
            val newBest = maxOf(habit.bestStreak, newStreak)
            val updated = habit.copy(
                streakCount = newStreak,
                bestStreak = newBest,
                lastCompletedEpochDay = todayEpochDay
            )
            habitDao.updateHabit(updated)
        }
    }
}
