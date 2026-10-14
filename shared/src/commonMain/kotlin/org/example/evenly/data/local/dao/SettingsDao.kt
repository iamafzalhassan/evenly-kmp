package org.example.evenly.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.example.evenly.data.local.entity.AppSettingEntity

@Dao
abstract class SettingsDao {
    @Query("DELETE FROM app_settings WHERE `key` = :key")
    abstract suspend fun deleteSetting(key: String)

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    abstract suspend fun findValue(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    abstract fun observeValue(key: String): Flow<String?>

    @Upsert
    abstract suspend fun upsertSetting(setting: AppSettingEntity)
}
