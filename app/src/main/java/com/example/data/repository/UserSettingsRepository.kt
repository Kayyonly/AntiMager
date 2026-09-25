package com.example.data.repository

import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(
    private val userSettingsDao: UserSettingsDao
) {

    val userSettingsFlow: Flow<UserSettingsEntity> = userSettingsDao.getUserSettingsFlow().map {
        it ?: UserSettingsEntity(id = 1)
    }

    suspend fun getUserSettings(): UserSettingsEntity {
        return userSettingsDao.getUserSettings() ?: UserSettingsEntity(id = 1).also {
            userSettingsDao.saveUserSettings(it)
        }
    }

    suspend fun getUserClass(): String {
        return getUserSettings().userClass
    }

    suspend fun updateUserClass(newClass: String) {
        val current = getUserSettings()
        userSettingsDao.saveUserSettings(current.copy(userClass = newClass.trim()))
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        userSettingsDao.saveUserSettings(settings.copy(id = 1, userClass = settings.userClass.trim()))
    }
}
