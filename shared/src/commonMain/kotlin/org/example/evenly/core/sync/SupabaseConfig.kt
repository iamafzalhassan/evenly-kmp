package org.example.evenly.core.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import org.example.evenly.core.database.SettingsDao

private const val SUPABASE_PUBLISHABLE_KEY: String = "sb_publishable_hYCU8NGFCq31ACN3gHc4Eg_ArTvUFOs"
private const val SUPABASE_URL: String = "https://qecwgvgfadcccykdkycx.supabase.co"

internal val remoteJson: Json = Json { ignoreUnknownKeys = true }

internal fun createEvenlySupabaseClient(settingsDao: SettingsDao): SupabaseClient = createSupabaseClient(supabaseKey = SUPABASE_PUBLISHABLE_KEY, supabaseUrl = SUPABASE_URL) {
    defaultLogLevel = LogLevel.NONE
    defaultSerializer = KotlinXSerializer(remoteJson)
    install(Auth) {
        sessionManager = RoomSessionManager(settingsDao)
    }
    install(Postgrest)
}
