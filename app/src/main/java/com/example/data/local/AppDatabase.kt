package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        ScheduleEntity::class,
        UserSettingsEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinutesBefore INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN source TEXT NOT NULL DEFAULT 'APP'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN externalId TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antimager_database.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
