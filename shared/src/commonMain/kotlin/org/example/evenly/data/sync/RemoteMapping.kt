package org.example.evenly.data.sync

import org.example.evenly.data.sources.ExpenseEntity
import org.example.evenly.data.sources.ExpenseShareEntity
import org.example.evenly.data.sources.ExpenseWithShares
import org.example.evenly.data.sources.GroupEntity
import org.example.evenly.data.sources.MemberEntity
import org.example.evenly.data.sources.SettlementEntity

internal fun ExpenseWithShares.toRemote(): RemoteExpenseWrite = RemoteExpenseWrite(
    isDeleted = expense.isDeleted,
    amountMinor = expense.amountMinorUnits,
    modifiedAtMs = expense.modifiedAtEpochMillis,
    spentAtMs = expense.spentAtEpochMillis,
    exchangeRateMicros = expense.exchangeRateMicros,
    currencyCode = expense.currencyCode,
    groupId = expense.groupId,
    id = expense.id,
    paidByMemberId = expense.paidByMemberId,
    splitKind = expense.splitKind,
    title = expense.title,
    exchangeRateCurrencyCode = expense.exchangeRateCurrencyCode,
    shares = shares.sortedBy { it.position }.map { RemoteShare(position = it.position, value = it.value, memberId = it.memberId) },
)

internal fun GroupEntity.toRemote(): RemoteGroupWrite = RemoteGroupWrite(isDeleted = isDeleted, createdAtMs = createdAtEpochMillis, modifiedAtMs = modifiedAtEpochMillis, currencyCode = currencyCode, id = id, name = name)

internal fun MemberEntity.toRemote(): RemoteMemberWrite = RemoteMemberWrite(isDeleted = isDeleted, position = position, modifiedAtMs = modifiedAtEpochMillis, groupId = groupId, id = id, name = name)

internal fun RemoteExpenseRow.toEntity(): ExpenseEntity = ExpenseEntity(
    isDeleted = isDeleted,
    isDirty = false,
    amountMinorUnits = amountMinor,
    modifiedAtEpochMillis = modifiedAtMs,
    spentAtEpochMillis = spentAtMs,
    exchangeRateMicros = exchangeRateMicros,
    currencyCode = currencyCode,
    groupId = groupId,
    id = id,
    paidByMemberId = paidByMemberId,
    splitKind = splitKind,
    title = title,
    exchangeRateCurrencyCode = exchangeRateCurrencyCode,
)

internal fun RemoteExpenseRow.toShareEntities(): List<ExpenseShareEntity> = shares.map { ExpenseShareEntity(position = it.position, value = it.value, expenseId = id, memberId = it.memberId) }

internal fun RemoteGroupRow.toEntity(): GroupEntity = GroupEntity(
    isDeleted = isDeleted,
    isDirty = false,
    createdAtEpochMillis = createdAtMs,
    modifiedAtEpochMillis = modifiedAtMs,
    currencyCode = currencyCode,
    id = id,
    name = name,
    inviteCode = inviteCode,
)

internal fun RemoteMemberRow.toEntity(): MemberEntity = MemberEntity(isDeleted = isDeleted, isDirty = false, position = position, modifiedAtEpochMillis = modifiedAtMs, groupId = groupId, id = id, name = name)

internal fun RemoteSettlementRow.toEntity(): SettlementEntity = SettlementEntity(
    isDeleted = isDeleted,
    isDirty = false,
    amountMinorUnits = amountMinor,
    modifiedAtEpochMillis = modifiedAtMs,
    settledAtEpochMillis = settledAtMs,
    currencyCode = currencyCode,
    fromMemberId = fromMemberId,
    groupId = groupId,
    id = id,
    toMemberId = toMemberId,
)

internal fun SettlementEntity.toRemote(): RemoteSettlementWrite = RemoteSettlementWrite(
    isDeleted = isDeleted,
    amountMinor = amountMinorUnits,
    modifiedAtMs = modifiedAtEpochMillis,
    settledAtMs = settledAtEpochMillis,
    currencyCode = currencyCode,
    fromMemberId = fromMemberId,
    groupId = groupId,
    id = id,
    toMemberId = toMemberId,
)
