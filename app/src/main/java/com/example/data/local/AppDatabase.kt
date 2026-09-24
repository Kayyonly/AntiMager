package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.util.HabitStreakCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [TaskEntity::class, HabitEntity::class, ScheduleEntity::class, UserSettingsEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antimager_database.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }

            private suspend fun seedInitialData(database: AppDatabase) {
                val cal = Calendar.getInstance()
                // Task 1: PR IPS Besok Pagi jam 08:00
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 8)
                cal.set(Calendar.MINUTE, 0)
                val besokPagi = cal.timeInMillis

                // Task 2: Resume Biologi Malam ini jam 20:00
                val cal2 = Calendar.getInstance()
                cal2.set(Calendar.HOUR_OF_DAY, 20)
                cal2.set(Calendar.MINUTE, 0)
                val malamIni = cal2.timeInMillis

                database.taskDao().insertTask(
                    TaskEntity(
                        title = "PR IPS Bab 3: Globalisasi",
                        subject = "IPS",
                        description = "Halaman 45 nomor 1 sampai 10 di buku tulis",
                        deadlineEpochMillis = besokPagi,
                        estimatedMinutes = 35,
                        priority = "HIGH",
                        isCompleted = false,
                        isPersistent = true,
                        locationName = "Sekolah"
                    )
                )

                database.taskDao().insertTask(
                    TaskEntity(
                        title = "Beli Buku Tulis & Pulpen",
                        subject = "Belanja",
                        description = "Mampir beli alat tulis sebelum masuk kelas",
                        deadlineEpochMillis = besokPagi,
                        estimatedMinutes = 15,
                        priority = "MEDIUM",
                        isCompleted = false,
                        isPersistent = true,
                        locationName = "Indomaret"
                    )
                )

                database.taskDao().insertTask(
                    TaskEntity(
                        title = "Rangkum Catatan Matematika",
                        subject = "Matematika",
                        description = "Rumus integral substitusi bab 4",
                        deadlineEpochMillis = malamIni,
                        estimatedMinutes = 45,
                        priority = "HIGH",
                        isCompleted = false,
                        isPersistent = true,
                        locationName = "Rumah"
                    )
                )

                // Seed Habits
                val todayEpochDay = HabitStreakCalculator.getLocalEpochDay()
                val habit1Days = (1..5).map { todayEpochDay - it }.joinToString(",")
                val habit2Days = listOf(todayEpochDay - 2, todayEpochDay - 1, todayEpochDay).joinToString(",")
                val habit3Days = (1..8).map { todayEpochDay - it }.joinToString(",")

                database.habitDao().insertHabit(
                    HabitEntity(
                        name = "Minum 1 Gelas Air Pas Bangun",
                        description = "Segarkan otak biar gak mager beranjak dari kasur",
                        iconName = "water",
                        streakCount = 5,
                        bestStreak = 7,
                        lastCompletedEpochDay = todayEpochDay - 1,
                        completedDaysCsv = habit1Days,
                        colorHex = "#38BDF8"
                    )
                )

                database.habitDao().insertHabit(
                    HabitEntity(
                        name = "Beresin Meja Belajar 3 Menit",
                        description = "Meja rapi bikin fokus naik 200%",
                        iconName = "clean",
                        streakCount = 3,
                        bestStreak = 4,
                        lastCompletedEpochDay = todayEpochDay,
                        completedDaysCsv = habit2Days,
                        colorHex = "#A78BFA"
                    )
                )

                database.habitDao().insertHabit(
                    HabitEntity(
                        name = "Cicil Tugas 15 Menit Tanpa HP",
                        description = "Teknik Pomodoro mini buat orang gampang terdistraksi",
                        iconName = "bolt",
                        streakCount = 8,
                        bestStreak = 12,
                        lastCompletedEpochDay = todayEpochDay - 1,
                        completedDaysCsv = habit3Days,
                        colorHex = "#34D399"
                    )
                )

                // Seed Realistic School Timetable with Lessons & Breaks
                val initialSchedules = listOf(
                    // Senin (dayOfWeek = 1)
                    ScheduleEntity(dayOfWeek = 1, subject = "Upacara Bendera", startTime = "07:00", endTime = "07:45", roomOrTeacher = "Lapangan Utama", colorHex = "#94A3B8"),
                    ScheduleEntity(dayOfWeek = 1, subject = "Matematika", startTime = "07:45", endTime = "09:15", roomOrTeacher = "Ruang 12 • Bu Sri", colorHex = "#38BDF8"),
                    ScheduleEntity(dayOfWeek = 1, subject = "Istirahat Pagi", startTime = "09:15", endTime = "09:45", roomOrTeacher = "Kantin / Santai", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 1, subject = "Bahasa Indonesia", startTime = "09:45", endTime = "11:15", roomOrTeacher = "Ruang 12 • Pak Joko", colorHex = "#A78BFA"),
                    ScheduleEntity(dayOfWeek = 1, subject = "Istirahat Siang & Sholat", startTime = "11:15", endTime = "12:00", roomOrTeacher = "Masjid / Kantin", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 1, subject = "IPS (Geografi)", startTime = "12:00", endTime = "13:30", roomOrTeacher = "Ruang 12 • Bu Linda", colorHex = "#FBBF24"),

                    // Selasa (dayOfWeek = 2)
                    ScheduleEntity(dayOfWeek = 2, subject = "Fisika / IPA", startTime = "07:15", endTime = "08:45", roomOrTeacher = "Lab Fisika • Pak Rudi", colorHex = "#34D399"),
                    ScheduleEntity(dayOfWeek = 2, subject = "Bahasa Inggris", startTime = "08:45", endTime = "10:00", roomOrTeacher = "Ruang 12 • Miss Diana", colorHex = "#F472B6"),
                    ScheduleEntity(dayOfWeek = 2, subject = "Istirahat", startTime = "10:00", endTime = "10:30", roomOrTeacher = "Kantin", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 2, subject = "Pendidikan Jasmani (Olahraga)", startTime = "10:30", endTime = "12:00", roomOrTeacher = "GOR Sekolah • Pak Heru", colorHex = "#FB923C"),

                    // Rabu (dayOfWeek = 3)
                    ScheduleEntity(dayOfWeek = 3, subject = "Biologi", startTime = "07:15", endTime = "08:45", roomOrTeacher = "Lab Biologi • Bu Ratna", colorHex = "#34D399"),
                    ScheduleEntity(dayOfWeek = 3, subject = "Kimia", startTime = "08:45", endTime = "10:00", roomOrTeacher = "Lab Kimia • Pak Dani", colorHex = "#2DD4BF"),
                    ScheduleEntity(dayOfWeek = 3, subject = "Istirahat", startTime = "10:00", endTime = "10:30", roomOrTeacher = "Kantin", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 3, subject = "Sejarah Indonesia", startTime = "10:30", endTime = "12:00", roomOrTeacher = "Ruang 12 • Pak Wahyu", colorHex = "#E879F9"),

                    // Kamis (dayOfWeek = 4)
                    ScheduleEntity(dayOfWeek = 4, subject = "Informatika / Coding", startTime = "07:15", endTime = "09:00", roomOrTeacher = "Lab Komputer • Bu Mega", colorHex = "#38BDF8"),
                    ScheduleEntity(dayOfWeek = 4, subject = "Seni Budaya", startTime = "09:00", endTime = "10:00", roomOrTeacher = "Ruang Kesenian", colorHex = "#C084FC"),
                    ScheduleEntity(dayOfWeek = 4, subject = "Istirahat", startTime = "10:00", endTime = "10:30", roomOrTeacher = "Kantin", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 4, subject = "Pendidikan Agama & Budi Pekerti", startTime = "10:30", endTime = "12:00", roomOrTeacher = "Ruang 12 • Ust. Farhan", colorHex = "#10B981"),

                    // Jumat (dayOfWeek = 5)
                    ScheduleEntity(dayOfWeek = 5, subject = "Senam / Jalan Sehat", startTime = "07:00", endTime = "07:45", roomOrTeacher = "Lapangan", colorHex = "#FB923C"),
                    ScheduleEntity(dayOfWeek = 5, subject = "Pendidikan Kewarganegaraan (PKn)", startTime = "07:45", endTime = "09:15", roomOrTeacher = "Ruang 12 • Bu Endang", colorHex = "#38BDF8"),
                    ScheduleEntity(dayOfWeek = 5, subject = "Istirahat & Sholat Jumat", startTime = "09:15", endTime = "10:00", roomOrTeacher = "Masjid", isBreak = true, colorHex = "#F59E0B"),
                    ScheduleEntity(dayOfWeek = 5, subject = "Bimbingan Konseling / Pengembangan Diri", startTime = "10:00", endTime = "11:15", roomOrTeacher = "Ruang BK", colorHex = "#818CF8")
                )

                database.scheduleDao().insertAll(initialSchedules)

                database.userSettingsDao().saveUserSettings(
                    UserSettingsEntity(
                        id = 1,
                        userClass = "X IPA 2",
                        studentName = "",
                        schoolName = "SMAN 1 AntiMager",
                        autoFilterClassSchedule = true
                    )
                )
            }
        }
    }
}
