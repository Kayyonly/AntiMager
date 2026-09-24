package com.example.data.repository

import com.example.data.local.dao.HabitDao
import com.example.data.local.entity.HabitEntity
import com.example.util.HabitStreakCalculator
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)

    suspend fun checkInHabit(id: Long) {
        val habit = habitDao.getHabitById(id) ?: return
        val todayEpochDay = HabitStreakCalculator.getLocalEpochDay()
        val updatedHabit = HabitStreakCalculator.toggleCheckIn(habit, todayEpochDay)
        habitDao.updateHabit(updatedHabit)
    }
}
