package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) = taskDao.setTaskCompleted(id, isCompleted)

    suspend fun snoozeTask(id: Long, newDeadlineMillis: Long) = taskDao.snoozeTask(id, newDeadlineMillis)

    suspend fun getTasksWithLocation(): List<TaskEntity> = taskDao.getTasksWithLocation()
}
