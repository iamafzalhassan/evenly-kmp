package org.example.evenly.data

import org.example.evenly.data.sources.ExpenseEntity
import org.example.evenly.data.sources.ExpenseShareEntity
import org.example.evenly.data.sources.ExpenseWithShares
import org.example.evenly.model.Currency
import org.example.evenly.model.ExchangeRate
import org.example.evenly.model.Expense
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule
import kotlin.time.Instant

private const val SPLIT_KIND_EQUAL: String = "EQUAL"
private const val SPLIT_KIND_EXACT: String = "EXACT"
private const val SPLIT_KIND_PERCENTAGE: String = "PERCENTAGE"

internal fun Expense.toEntity(modifiedAtEpochMillis: Long, groupId: String): ExpenseEntity = ExpenseEntity(
    isDeleted = false,
    isDirty = true,
    amountMinorUnits = amount.minorUnits,
    modifiedAtEpochMillis = modifiedAtEpochMillis,
    spentAtEpochMillis = spentAt.toEpochMilliseconds(),
    exchangeRateMicros = exchangeRate?.micros,
    currencyCode = amount.currency.name,
    groupId = groupId,
    id = id.raw,
    paidByMemberId = paidBy.raw,
    splitKind = split.kind(),
    title = title,
    exchangeRateCurrencyCode = exchangeRate?.to?.name,
)

internal fun Expense.toShareEntities(): List<ExpenseShareEntity> {
    val values = when (val rule = split) {
        is SplitRule.Equal -> rule.participants.map { it to 1L }
        is SplitRule.Exact -> rule.minorUnits.map { (memberId, minorUnits) -> memberId to minorUnits }
        is SplitRule.Percentage -> rule.basisPoints.map { (memberId, basisPoints) -> memberId to basisPoints.toLong() }
    }
    return values.mapIndexed { index, (memberId, value) -> ExpenseShareEntity(position = index, value = value, expenseId = id.raw, memberId = memberId.raw) }
}

internal fun ExpenseWithShares.toExpenseOrNull(): Expense? {
    val orderedShares = shares.sortedBy { it.position }
    val currency = Currency.fromCode(expense.currencyCode) ?: return null
    val split = when (expense.splitKind) {
        SPLIT_KIND_EQUAL -> SplitRule.Equal(participants = orderedShares.map { MemberId(it.memberId) })
        SPLIT_KIND_EXACT -> SplitRule.Exact(minorUnits = orderedShares.associate { MemberId(it.memberId) to it.value })
        SPLIT_KIND_PERCENTAGE -> SplitRule.Percentage(basisPoints = orderedShares.associate { MemberId(it.memberId) to it.value.toInt() })
        else -> return null
    }
    val rateMicros = expense.exchangeRateMicros
    val rateCurrencyCode = expense.exchangeRateCurrencyCode
    val exchangeRate = when {
        rateMicros == null && rateCurrencyCode == null -> null
        rateMicros == null || rateCurrencyCode == null -> return null
        else -> ExchangeRate(micros = rateMicros, from = currency, to = Currency.fromCode(rateCurrencyCode) ?: return null)
    }
    return Expense(
        title = expense.title,
        exchangeRate = exchangeRate,
        id = ExpenseId(expense.id),
        spentAt = Instant.fromEpochMilliseconds(expense.spentAtEpochMillis),
        paidBy = MemberId(expense.paidByMemberId),
        amount = Money(minorUnits = expense.amountMinorUnits, currency = currency),
        split = split,
    )
}

private fun SplitRule.kind(): String = when (this) {
    is SplitRule.Equal -> SPLIT_KIND_EQUAL
    is SplitRule.Exact -> SPLIT_KIND_EXACT
    is SplitRule.Percentage -> SPLIT_KIND_PERCENTAGE
}
