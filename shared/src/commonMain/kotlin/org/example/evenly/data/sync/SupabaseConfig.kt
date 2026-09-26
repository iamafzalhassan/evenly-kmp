package org.example.evenly.data.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

private const val SUPABASE_PUBLISHABLE_KEY: String = "sb_publishable_hYCU8NGFCq31ACN3gHc4Eg_ArTvUFOs"
private const val SUPABASE_URL: String = "https://qecwgvgfadcccykdkycx.supabase.co"

internal fun createEvenlySupabaseClient(): SupabaseClient = createSupabaseClient(supabaseKey = SUPABASE_PUBLISHABLE_KEY, supabaseUrl = SUPABASE_URL) {
    defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    install(Auth)
    install(Postgrest)
}
