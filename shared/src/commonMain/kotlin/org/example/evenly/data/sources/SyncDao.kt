package org.example.evenly.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
abstract class SyncDao {
    @Query("DELETE FROM expense_shares WHERE expenseId IN (:expenseIds)")
    abstract suspend fun deleteShares(expenseIds: List<String>)

    @Transaction
    @Query("SELECT * FROM expenses WHERE isDirty = 1")
    abstract suspend fun dirtyExpenses(): List<ExpenseWithShares>

    @Query("SELECT * FROM expense_groups WHERE isDirty = 1")
    abstract suspend fun dirtyGroups(): List<GroupEntity>

    @Query("SELECT * FROM members WHERE isDirty = 1")
    abstract suspend fun dirtyMembers(): List<MemberEntity>

    @Query("SELECT * FROM settlements WHERE isDirty = 1")
    abstract suspend fun dirtySettlements(): List<SettlementEntity>

    @Query("SELECT id, isDirty, modifiedAtEpochMillis FROM expenses WHERE id IN (:ids)")
    abstract suspend fun expenseStates(ids: List<String>): List<RowSyncState>

    @Query("SELECT id, isDirty, modifiedAtEpochMillis, currencyCode, inviteCode FROM expense_groups WHERE id IN (:ids)")
    abstract suspend fun groupStates(ids: List<String>): List<GroupSyncState>

    @Insert
    abstract suspend fun insertShares(shares: List<ExpenseShareEntity>)

    @Query("SELECT inviteCode FROM expense_groups WHERE inviteCode IS NOT NULL AND isDeleted = 0")
    abstract suspend fun inviteCodes(): List<String>

    @Query("UPDATE expenses SET isDirty = 0 WHERE id = :id AND modifiedAtEpochMillis = :modifiedAtEpochMillis")
    abstract suspend fun markExpenseClean(modifiedAtEpochMillis: Long, id: String)

    @Query("UPDATE expense_groups SET isDirty = 0 WHERE id = :id AND modifiedAtEpochMillis = :modifiedAtEpochMillis")
    abstract suspend fun markGroupClean(modifiedAtEpochMillis: Long, id: String)

    @Query("UPDATE members SET isDirty = 0 WHERE id = :id AND modifiedAtEpochMillis = :modifiedAtEpochMillis")
    abstract suspend fun markMemberClean(modifiedAtEpochMillis: Long, id: String)

    @Query("UPDATE settlements SET isDirty = 0 WHERE id = :id AND modifiedAtEpochMillis = :modifiedAtEpochMillis")
    abstract suspend fun markSettlementClean(modifiedAtEpochMillis: Long, id: String)

    @Query("SELECT id, isDirty, modifiedAtEpochMillis FROM members WHERE id IN (:ids)")
    abstract suspend fun memberStates(ids: List<String>): List<RowSyncState>

    @Query("UPDATE expense_groups SET inviteCode = :inviteCode WHERE id = :id")
    abstract suspend fun saveInviteCode(id: String, inviteCode: String)

    @Query("SELECT id, isDirty, modifiedAtEpochMillis FROM settlements WHERE id IN (:ids)")
    abstract suspend fun settlementStates(ids: List<String>): List<RowSyncState>

    @Upsert
    abstract suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    @Upsert
    abstract suspend fun upsertGroups(groups: List<GroupEntity>)

    @Upsert
    abstract suspend fun upsertMembers(members: List<MemberEntity>)

    @Upsert
    abstract suspend fun upsertSettlements(settlements: List<SettlementEntity>)

    @Transaction
    open suspend fun markExpensesClean(expenses: List<ExpenseEntity>) {
        expenses.forEach { markExpenseClean(id = it.id, modifiedAtEpochMillis = it.modifiedAtEpochMillis) }
    }

    @Transaction
    open suspend fun markGroupsClean(groups: List<GroupEntity>) {
        groups.forEach { markGroupClean(id = it.id, modifiedAtEpochMillis = it.modifiedAtEpochMillis) }
    }

    @Transaction
    open suspend fun markMembersClean(members: List<MemberEntity>) {
        members.forEach { markMemberClean(id = it.id, modifiedAtEpochMillis = it.modifiedAtEpochMillis) }
    }

    @Transaction
    open suspend fun markSettlementsClean(settlements: List<SettlementEntity>) {
        settlements.forEach { markSettlementClean(id = it.id, modifiedAtEpochMillis = it.modifiedAtEpochMillis) }
    }

    @Transaction
    open suspend fun saveRemoteExpenses(expenses: List<ExpenseEntity>, shares: List<ExpenseShareEntity>) {
        upsertExpenses(expenses)
        deleteShares(expenses.map { it.id })
        insertShares(shares)
    }

    @Transaction
    open suspend fun saveRemoteGroups(groups: List<GroupEntity>, inviteCodes: Map<String, String>) {
        upsertGroups(groups)
        inviteCodes.forEach { (id, inviteCode) -> saveInviteCode(id = id, inviteCode = inviteCode) }
    }
}
