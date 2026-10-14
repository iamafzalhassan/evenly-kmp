package org.example.evenly.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.createDefaultSessionManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.example.evenly.common.succeeds

internal const val PULL_PAGE_SIZE: Int = 500

private const val COLUMN_ID: String = "id"
private const val COLUMN_SYNC_SEQ: String = "sync_seq"
private const val TABLE_EXPENSES: String = "expenses"
private const val TABLE_GROUPS: String = "groups"
private const val TABLE_MEMBERS: String = "members"
private const val TABLE_SETTLEMENTS: String = "settlements"

class SupabaseRemote internal constructor(private val client: SupabaseClient) {
    suspend fun ensureSignedIn(): Boolean {
        client.auth.awaitInitialization()
        if (client.auth.currentSessionOrNull() != null || restoreLegacySession()) return false
        client.auth.signInAnonymously()
        return true
    }

    suspend fun fetchGroups(ids: List<String>): List<RemoteGroupRow> = client.postgrest.from(TABLE_GROUPS).select { filter { isIn(COLUMN_ID, ids) } }.decodeList<JsonObject>().mapNotNull { decodeOrNull<RemoteGroupRow>(it) }

    suspend fun joinGroup(code: String): String? = client.postgrest.rpc(function = "join_group", parameters = buildJsonObject { put("code", code) }).decodeAs<String?>()

    suspend fun pullExpenses(afterSeq: Long): PulledPage<RemoteExpenseRow> = pull(afterSeq = afterSeq, table = TABLE_EXPENSES)

    suspend fun pullGroups(afterSeq: Long): PulledPage<RemoteGroupRow> = pull(afterSeq = afterSeq, table = TABLE_GROUPS)

    suspend fun pullMembers(afterSeq: Long): PulledPage<RemoteMemberRow> = pull(afterSeq = afterSeq, table = TABLE_MEMBERS)

    suspend fun pullSettlements(afterSeq: Long): PulledPage<RemoteSettlementRow> = pull(afterSeq = afterSeq, table = TABLE_SETTLEMENTS)

    suspend fun pushExpenses(rows: List<RemoteExpenseWrite>) = push(rows = rows, table = TABLE_EXPENSES)

    suspend fun pushGroups(rows: List<RemoteGroupWrite>) = push(rows = rows, table = TABLE_GROUPS)

    suspend fun pushMembers(rows: List<RemoteMemberWrite>) = push(rows = rows, table = TABLE_MEMBERS)

    suspend fun pushSettlements(rows: List<RemoteSettlementWrite>) = push(rows = rows, table = TABLE_SETTLEMENTS)

    suspend fun rotateInviteCode(groupId: String): String = client.postgrest.rpc(function = "rotate_invite_code", parameters = buildJsonObject { put("target_group_id", groupId) }).decodeAs<String>()

    @OptIn(SupabaseInternal::class)
    private suspend fun restoreLegacySession(): Boolean {
        val legacySessionManager = client.auth.createDefaultSessionManager()
        val session = legacySessionManager.loadSessionOrNull() ?: return false
        legacySessionManager.deleteSession()
        return succeeds { client.auth.importSession(session) }
    }

    private suspend inline fun <reified T : Any> pull(afterSeq: Long, table: String): PulledPage<T> {
        val rows = client.postgrest.from(table).select {
            filter { gt(COLUMN_SYNC_SEQ, afterSeq) }
            order(column = COLUMN_SYNC_SEQ, order = Order.ASCENDING)
            limit(PULL_PAGE_SIZE.toLong())
        }.decodeList<JsonObject>()
        return PulledPage(size = rows.size, lastSeq = rows.maxOfOrNull { it[COLUMN_SYNC_SEQ]?.jsonPrimitive?.longOrNull ?: afterSeq } ?: afterSeq, rows = rows.mapNotNull { decodeOrNull<T>(it) })
    }

    private inline fun <reified T : Any> decodeOrNull(row: JsonObject): T? = try {
        remoteJson.decodeFromJsonElement<T>(row)
    } catch (exception: IllegalArgumentException) {
        null
    }

    private suspend inline fun <reified T : Any> push(rows: List<T>, table: String) {
        client.postgrest.from(table).upsert(rows) { onConflict = COLUMN_ID }
    }
}
