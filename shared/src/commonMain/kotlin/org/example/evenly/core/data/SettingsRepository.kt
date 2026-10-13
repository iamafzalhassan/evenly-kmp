package org.example.evenly.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.example.evenly.core.database.AppSettingEntity
import org.example.evenly.core.database.SettingsDao

private const val KEY_BIOMETRIC_LOCK: String = "biometric_lock_enabled"

class SettingsRepository internal constructor(private val settingsDao: SettingsDao) {
    fun observeBiometricLockEnabled(): Flow<Boolean> = settingsDao.observeValue(KEY_BIOMETRIC_LOCK).map { it == true.toString() }.distinctUntilChanged()

    suspend fun setBiometricLockEnabled(isEnabled: Boolean) = settingsDao.upsertSetting(AppSettingEntity(key = KEY_BIOMETRIC_LOCK, value = isEnabled.toString()))
}
