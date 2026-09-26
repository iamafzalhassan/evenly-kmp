package org.example.evenly.data.sync

import io.github.jan.supabase.exceptions.RestException
import org.example.evenly.data.sources.AppSettingEntity
import org.example.evenly.data.sources.ExpenseWithShares
import org.example.evenly.data.sources.SettingsDao
import org.example.evenly.data.sources.SyncDao
import org.example.evenly.data.sources.SyncedRow
import org.example.evenly.data.toExpenseOrNull
import org.example.evenly.data.toSettlementOrNull
import org.example.evenly.domain.LedgerIntegrity
import org.example.evenly.model.Currency

private const val CURSOR_EXPENSES: String = "sync_cursor_expenses"
private const val CURSOR_GROUPS: String = "sync_cursor_groups"
private const val CURSOR_MEMBERS: String = "sync_cursor_members"
private const val CURSOR_SETTLEMENTS: String = "sync_cursor_settlements"
private const val KEY_REJOIN_PENDING: String = "sync_rejoin_pending"

class SyncEngine internal constructor(private val settingsDao: SettingsDao, private val remote: SupabaseRemote, private val syncDao: SyncDao) {
    suspend fun joinGroup(code: String): String? {
        signIn()
        val groupId = remote.joinGroup(code) ?: return null
        listOf(CURSOR_EXPENSES, CURSOR_GROUPS, CURSOR_MEMBERS, CURSOR_SETTLEMENTS).forEach { writeCursor(key = it, value = 0L) }
        sync()
        return groupId
    }

    suspend fun sync() {
        signIn()
        val rejectedCount = listOf(
            push(markClean = syncDao::markGroupsClean, rows = syncDao.dirtyGroups(), send = { rows -> remote.pushGroups(rows.map { it.toRemote() }) }),
            push(markClean = syncDao::markMembersClean, rows = syncDao.dirtyMembers(), send = { rows -> remote.pushMembers(rows.map { it.toRemote() }) }),
            push(markClean = { rows -> syncDao.markExpensesClean(rows.map { it.expense }) }, rows = syncDao.dirtyExpenses(), send = { rows -> remote.pushExpenses(rows.map { it.toRemote() }) }),
            push(markClean = syncDao::markSettlementsClean, rows = syncDao.dirtySettlements(), send = { rows -> remote.pushSettlements(rows.map { it.toRemote() }) }),
        ).sum()
        pullTable(apply = ::applyGroups, cursorKey = CURSOR_GROUPS, fetch = remote::pullGroups, seqOf = { it.syncSeq })
        pullTable(apply = ::applyMembers, cursorKey = CURSOR_MEMBERS, fetch = remote::pullMembers, seqOf = { it.syncSeq })
        pullTable(apply = ::applyExpenses, cursorKey = CURSOR_EXPENSES, fetch = remote::pullExpenses, seqOf = { it.syncSeq })
        pullTable(apply = ::applySettlements, cursorKey = CURSOR_SETTLEMENTS, fetch = remote::pullSettlements, seqOf = { it.syncSeq })
        if (rejectedCount > 0) throw SyncRejectedException(rejectedCount)
    }

    private suspend fun signIn() {
        if (remote.ensureSignedIn()) {
            settingsDao.upsertSetting(AppSettingEntity(key = KEY_REJOIN_PENDING, value = true.toString()))
        }
        if (settingsDao.findValue(KEY_REJOIN_PENDING) == true.toString()) {
            syncDao.inviteCodes().forEach { remote.joinGroup(it) }
            settingsDao.upsertSetting(AppSettingEntity(key = KEY_REJOIN_PENDING, value = false.toString()))
        }
    }

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

    private suspend fun <T> pullTable(apply: suspend (List<T>) -> Unit, cursorKey: String, fetch: suspend (Long) -> List<T>, seqOf: (T) -> Long) {
        var cursor = readCursor(cursorKey)
        while (true) {
            val rows = fetch(cursor)
            if (rows.isEmpty()) return
            apply(rows)
            cursor = rows.maxOf(seqOf)
            writeCursor(key = cursorKey, value = cursor)
            if (rows.size < PULL_PAGE_SIZE) return
        }
    }

    private suspend fun readCursor(key: String): Long = settingsDao.findValue(key)?.toLongOrNull() ?: 0L

    private suspend fun writeCursor(key: String, value: Long) = settingsDao.upsertSetting(AppSettingEntity(key = key, value = value.toString()))

    private suspend fun applyMembers(rows: List<RemoteMemberRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val states = syncDao.memberStates(rows.map { it.id }.distinct()).associateBy { it.id }
        val acceptedRows = rows.filter { it.groupId in groupCurrencies && it.name.isNotBlank() && it.position >= 0 && states[it.id].acceptsRemote(it.modifiedAtMs) }
        if (acceptedRows.isEmpty()) return
        syncDao.upsertMembers(acceptedRows.map { it.toEntity() })
    }

    private suspend fun applyExpenses(rows: List<RemoteExpenseRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val states = syncDao.expenseStates(rows.map { it.id }.distinct()).associateBy { it.id }
        val acceptedRows = rows.filter { isValidExpense(groupCurrency = groupCurrencies[it.groupId], row = it) && states[it.id].acceptsRemote(it.modifiedAtMs) }
        if (acceptedRows.isEmpty()) return
        syncDao.saveRemoteExpenses(expenses = acceptedRows.map { it.toEntity() }, shares = acceptedRows.flatMap { it.toShareEntities() })
    }

    private fun isValidExpense(groupCurrency: Currency?, row: RemoteExpenseRow): Boolean {
        if (groupCurrency == null || row.shares.map { it.memberId }.toSet().size != row.shares.size) return false
        if (row.isDeleted) return true
        val expense = ExpenseWithShares(expense = row.toEntity(), shares = row.toShareEntities()).toExpenseOrNull() ?: return false
        return row.title.isNotBlank() && LedgerIntegrity.isValid(expense = expense, groupCurrency = groupCurrency)
    }

    private suspend fun applySettlements(rows: List<RemoteSettlementRow>) {
        val groupCurrencies = localGroupCurrencies(rows.map { it.groupId })
        val states = syncDao.settlementStates(rows.map { it.id }.distinct()).associateBy { it.id }
        val acceptedRows = rows.filter { isValidSettlement(groupCurrency = groupCurrencies[it.groupId], row = it) && states[it.id].acceptsRemote(it.modifiedAtMs) }
        if (acceptedRows.isEmpty()) return
        syncDao.upsertSettlements(acceptedRows.map { it.toEntity() })
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
        val states = syncDao.groupStates(rows.map { it.id }.distinct()).associateBy { it.id }
        val validRows = rows.filter { Currency.fromCode(it.currencyCode) != null && it.name.isNotBlank() }
        val acceptedRows = validRows.filter { states[it.id].acceptsRemote(it.modifiedAtMs) }
        val acceptedIds = acceptedRows.map { it.id }.toSet()
        val inviteCodes = validRows.filter { it.id !in acceptedIds && states[it.id]?.inviteCode != it.inviteCode }.associate { it.id to it.inviteCode }
        if (acceptedRows.isEmpty() && inviteCodes.isEmpty()) return
        syncDao.saveRemoteGroups(groups = acceptedRows.map { it.toEntity() }, inviteCodes = inviteCodes)
    }

    private fun SyncedRow?.acceptsRemote(remoteModifiedAtMs: Long): Boolean = when {
        this == null -> true
        isDirty -> modifiedAtEpochMillis <= remoteModifiedAtMs
        else -> modifiedAtEpochMillis != remoteModifiedAtMs
    }
}
