package org.example.evenly.core.sync

import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json
import org.example.evenly.core.database.AppSettingEntity
import org.example.evenly.core.database.SettingsDao

private const val KEY_SESSION: String = "supabase_session"

internal class RoomSessionManager(private val settingsDao: SettingsDao) : SessionManager {
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun deleteSession() = settingsDao.deleteSetting(KEY_SESSION)

    override suspend fun loadSession(): UserSession = json.decodeFromString(settingsDao.findValue(KEY_SESSION) ?: throw NoSessionFoundException())

    override suspend fun saveSession(session: UserSession) = settingsDao.upsertSetting(AppSettingEntity(key = KEY_SESSION, value = json.encodeToString(session)))
}
