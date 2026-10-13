package org.example.evenly.core.sync

import io.github.jan.supabase.exceptions.RestException
import org.example.evenly.core.data.toExpenseOrNull
import org.example.evenly.core.data.toSettlementOrNull
import org.example.evenly.core.database.AppSettingEntity
import org.example.evenly.core.database.ExpenseWithShares
import org.example.evenly.core.database.SettingsDao
import org.example.evenly.core.database.SyncDao
import org.example.evenly.core.domain.LedgerIntegrity
import org.example.evenly.core.model.Currency

private const val CURSOR_EXPENSES: String = "sync_cursor_expenses"
private const val CURSOR_GROUPS: String = "sync_cursor_groups"
private const val CURSOR_MEMBERS: String = "sync_cursor_members"
private const val CURSOR_SETTLEMENTS: String = "sync_cursor_settlements"
private const val KEY_CURSOR_RESET_PENDING: String = "sync_cursor_reset_pending"
private const val KEY_REJOIN_PENDING: String = "sync_rejoin_pending"

class SyncEngine internal constructor(private val settingsDao: SettingsDao, private val remote: SupabaseRemote, private val syncDao: SyncDao) {
    suspend fun joinGroup(code: String): String? {
        signIn()
        resetCursorsIfPending()
        writeFlag(isSet = true, key = KEY_CURSOR_RESET_PENDING)
        val groupId = remote.joinGroup(code)
        if (groupId == null) writeFlag(isSet = false, key = KEY_CURSOR_RESET_PENDING)
        return groupId
    }

    suspend fun rotateInviteCode(groupId: String) {
        signIn()
        syncDao.saveInviteCode(id = groupId, inviteCode = remote.rotateInviteCode(groupId))
    }

    suspend fun sync() {
        signIn()
        resetCursorsIfPending()
        val rejectedCount = listOf(
            push(markClean = syncDao::markGroupsClean, rows = syncDao.dirtyGroups(), send = { rows -> remote.pushGroups(rows.map { it.toRemote() }) }),
            push(markClean = syncDao::markMembersClean, rows = syncDao.dirtyMembers(), send = { rows -> remote.pushMembers(rows.map { it.toRemote() }) }),
            push(markClean = { rows -> syncDao.markExpensesClean(rows.map { it.expense }) }, rows = syncDao.dirtyExpenses(), send = { rows -> remote.pushExpenses(rows.map { it.toRemote() }) }),
            push(markClean = syncDao::markSettlementsClean, rows = syncDao.dirtySettlements(), send = { rows -> remote.pushSettlements(rows.map { it.toRemote() }) }),
        ).sum()
        pullTable(apply = ::applyGroups, cursorKey = CURSOR_GROUPS, fetch = remote::pullGroups)
        pullTable(apply = ::applyMembers, cursorKey = CURSOR_MEMBERS, fetch = remote::pullMembers)
        pullTable(apply = ::applyExpenses, cursorKey = CURSOR_EXPENSES, fetch = remote::pullExpenses)
        pullTable(apply = ::applySettlements, cursorKey = CURSOR_SETTLEMENTS, fetch = remote::pullSettlements)
        if (rejectedCount > 0) throw SyncRejectedException(rejectedCount)
    }

    private suspend fun signIn() {
        if (remote.ensureSignedIn()) {
            writeFlag(isSet = true, key = KEY_REJOIN_PENDING)
        }
        if (readFlag(KEY_REJOIN_PENDING)) {
            syncDao.inviteCodes().forEach { remote.joinGroup(it) }
            writeFlag(isSet = false, key = KEY_REJOIN_PENDING)
        }
    }

    private suspend fun resetCursorsIfPending() {
        if (!readFlag(KEY_CURSOR_RESET_PENDING)) return
        listOf(CURSOR_EXPENSES, CURSOR_GROUPS, CURSOR_MEMBERS, CURSOR_SETTLEMENTS).forEach { writeCursor(key = it, value = 0L) }
        writeFlag(isSet = false, key = KEY_CURSOR_RESET_PENDING)
    }

    private suspend fun readFlag(key: String): Boolean = settingsDao.findValue(key) == true.toString()

    private suspend fun writeFlag(isSet: Boolean, key: String) = settingsDao.upsertSetting(AppSettingEntity(key = key, value = isSet.toString()))

    private suspend fun <T> push(rows: List<T>, markClean: suspend (List<T>) -> Unit, send: suspend (List<T>) -> Unit): Int {
        if (rows.isEmpty()) return 0
        try {
            send(rows)
            markClean(rows)
            return 0
        } catch (exception: RestException) {
            if (rows.size == 1) return 1
        }
        return rows.sumOf { row -> push(markClean = markClean, rows = listOf(row), send = send) }
    }

    private suspend fun <T> pullTable(apply: suspend (List<T>) -> Unit, cursorKey: String, fetch: suspend (Long) -> PulledPage<T>) {
        var cursor = readCursor(cursorKey)
        while (true) {
            val page = fetch(cursor)
            if (page.size == 0) return
            if (page.rows.isNotEmpty()) apply(page.rows)
            cursor = page.lastSeq
            writeCursor(key = cursorKey, value = cursor)
            if (page.size < PULL_PAGE_SIZE) return
        }
    }

    private suspend fun readCursor(key: String): Long = settingsDao.findValue(key)?.toLongOrNull() ?: 0L

    private suspend fun writeCursor(key: String, value: Long) = settingsDao.upsertSetting(AppSettingEntity(key = key, value = value.toString()))

    private suspend fun applyMembers(rows: List<RemoteMemberRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val validRows = rows.filter { it.groupId in groupCurrencies && LedgerIntegrity.isValidMemberName(it.name) && it.position >= 0 }
        if (validRows.isNotEmpty()) syncDao.saveRemoteMembers(validRows.map { it.toEntity() })
    }

    private suspend fun applyExpenses(rows: List<RemoteExpenseRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val validRows = rows.filter { isValidExpense(groupCurrency = groupCurrencies[it.groupId], row = it) }
        if (validRows.isNotEmpty()) syncDao.saveRemoteExpenses(expenses = validRows.map { it.toEntity() }, shares = validRows.flatMap { it.toShareEntities() })
    }

    private fun isValidExpense(groupCurrency: Currency?, row: RemoteExpenseRow): Boolean {
        if (groupCurrency == null || row.shares.map { it.memberId }.toSet().size != row.shares.size) return false
        if (row.isDeleted) return true
        val expense = ExpenseWithShares(expense = row.toEntity(), shares = row.toShareEntities()).toExpenseOrNull() ?: return false
        return LedgerIntegrity.isValid(expense = expense, groupCurrency = groupCurrency)
    }

    private suspend fun applySettlements(rows: List<RemoteSettlementRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val validRows = rows.filter { isValidSettlement(groupCurrency = groupCurrencies[it.groupId], row = it) }
        if (validRows.isNotEmpty()) syncDao.saveRemoteSettlements(validRows.map { it.toEntity() })
    }

    private fun isValidSettlement(groupCurrency: Currency?, row: RemoteSettlementRow): Boolean {
        if (groupCurrency == null) return false
        if (row.isDeleted) return true
        val settlement = row.toEntity().toSettlementOrNull() ?: return false
        return LedgerIntegrity.isValid(groupCurrency = groupCurrency, settlement = settlement)
    }

    private suspend fun localGroupCurrencies(groupIds: List<String>): Map<String, Currency> {
        val ids = groupIds.distinct()
        var states = syncDao.groupStates(ids)
        val missingIds = ids - states.map { it.id }.toSet()
        if (missingIds.isNotEmpty()) {
            applyGroups(remote.fetchGroups(missingIds))
            states = syncDao.groupStates(ids)
        }
        return states.mapNotNull { state -> Currency.fromCode(state.currencyCode)?.let { state.id to it } }.toMap()
    }

    private suspend fun applyGroups(rows: List<RemoteGroupRow>) {
        val validRows = rows.filter { Currency.fromCode(it.currencyCode) != null && LedgerIntegrity.isValidGroupName(it.name) }
        if (validRows.isNotEmpty()) syncDao.saveRemoteGroups(validRows.map { it.toEntity() })
    }
}
