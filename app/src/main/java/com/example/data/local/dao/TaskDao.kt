package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, deadlineEpochMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadlineEpochMillis ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadlineEpochMillis ASC")
    suspend fun getIncompleteTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean)

    @Query("UPDATE tasks SET snoozeCount = snoozeCount + 1, deadlineEpochMillis = :newDeadline WHERE id = :id")
    suspend fun snoozeTask(id: Long, newDeadline: Long)

    @Query("SELECT * FROM tasks WHERE locationName IS NOT NULL AND isCompleted = 0")
    suspend fun getTasksWithLocation(): List<TaskEntity>
}
