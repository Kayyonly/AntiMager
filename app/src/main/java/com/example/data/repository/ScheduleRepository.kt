package com.example.data.repository

import com.example.data.local.dao.ScheduleDao
import com.example.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    val allSchedules: Flow<List<ScheduleEntity>> = dao.getAllSchedules()

    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<ScheduleEntity>> {
        return dao.getSchedulesForDay(dayOfWeek)
    }

    suspend fun insertSchedule(schedule: ScheduleEntity): Long {
        return dao.insertSchedule(schedule)
    }

    suspend fun insertAll(schedules: List<ScheduleEntity>) {
        dao.insertAll(schedules)
    }

    suspend fun updateSchedule(schedule: ScheduleEntity) {
        dao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        dao.deleteSchedule(schedule)
    }

    suspend fun deleteScheduleById(id: Long) {
        dao.deleteScheduleById(id)
    }
}
