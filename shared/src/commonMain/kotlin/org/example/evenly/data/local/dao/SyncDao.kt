package org.example.evenly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import org.example.evenly.data.local.entity.ExpenseEntity
import org.example.evenly.data.local.entity.ExpenseShareEntity
import org.example.evenly.data.local.entity.ExpenseWithShares
import org.example.evenly.data.local.entity.GroupEntity
import org.example.evenly.data.local.entity.GroupSyncState
import org.example.evenly.data.local.entity.MemberEntity
import org.example.evenly.data.local.entity.RowSyncState
import org.example.evenly.data.local.entity.SettlementEntity
import org.example.evenly.data.local.entity.acceptsRemote

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
        val states = expenseStates(expenses.map { it.id }.distinct()).associateBy { it.id }
        val acceptedExpenses = expenses.filter { states[it.id].acceptsRemote(it.modifiedAtEpochMillis) }
        if (acceptedExpenses.isEmpty()) return
        val acceptedIds = acceptedExpenses.map { it.id }.toSet()
        upsertExpenses(acceptedExpenses)
        deleteShares(acceptedIds.toList())
        insertShares(shares.filter { it.expenseId in acceptedIds })
    }

    @Transaction
    open suspend fun saveRemoteGroups(groups: List<GroupEntity>) {
        val states = groupStates(groups.map { it.id }.distinct()).associateBy { it.id }
        val acceptedGroups = groups.filter { states[it.id].acceptsRemote(it.modifiedAtEpochMillis) }
        val acceptedIds = acceptedGroups.map { it.id }.toSet()
        if (acceptedGroups.isNotEmpty()) upsertGroups(acceptedGroups)
        groups.filter { it.id !in acceptedIds && states[it.id]?.inviteCode != it.inviteCode }.forEach { group -> group.inviteCode?.let { saveInviteCode(id = group.id, inviteCode = it) } }
    }

    @Transaction
    open suspend fun saveRemoteMembers(members: List<MemberEntity>) {
        val states = memberStates(members.map { it.id }.distinct()).associateBy { it.id }
        val acceptedMembers = members.filter { states[it.id].acceptsRemote(it.modifiedAtEpochMillis) }
        if (acceptedMembers.isNotEmpty()) upsertMembers(acceptedMembers)
    }

    @Transaction
    open suspend fun saveRemoteSettlements(settlements: List<SettlementEntity>) {
        val states = settlementStates(settlements.map { it.id }.distinct()).associateBy { it.id }
        val acceptedSettlements = settlements.filter { states[it.id].acceptsRemote(it.modifiedAtEpochMillis) }
        if (acceptedSettlements.isNotEmpty()) upsertSettlements(acceptedSettlements)
    }
}
