package org.example.evenly.core.domain

import org.example.evenly.core.format.MoneyFormat
import org.example.evenly.core.format.RateFormat
import org.example.evenly.core.model.Currency
import org.example.evenly.core.model.Expense
import org.example.evenly.core.model.Group
import org.example.evenly.core.model.Member
import org.example.evenly.core.model.MemberId
import org.example.evenly.core.model.Settlement
import org.example.evenly.core.model.memberIds
import kotlin.time.Instant

object LedgerIntegrity {
    const val MAX_TIMESTAMP_MILLIS: Long = 4_102_444_800_000L
    const val MIN_TIMESTAMP_MILLIS: Long = -5_364_662_400_000L

    fun isValid(expense: Expense, group: Group): Boolean = isValid(expense = expense, groupCurrency = group.currency) && group.hasMembers(expense.split.memberIds + expense.paidBy)

    fun isValid(expense: Expense, groupCurrency: Currency): Boolean {
        val amount = expense.amount
        val rate = expense.exchangeRate
        val isRateConsistent = if (rate == null) {
            amount.currency == groupCurrency
        } else {
            amount.currency != groupCurrency && rate.from == amount.currency && rate.to == groupCurrency && rate.micros in 1L..RateFormat.maxMicros()
        }
        val isWithinLimits = isValidTitle(expense.title) && expense.split.memberIds.size <= Expense.MAX_PARTICIPANTS && isValidTime(expense.spentAt) && amount.minorUnits <= MoneyFormat.maxMinorUnits(amount.currency)
        return isRateConsistent && isWithinLimits && ExpenseSplitter.validate(amount, expense.split) == null
    }

    fun isValid(settlement: Settlement, group: Group): Boolean = isValid(groupCurrency = group.currency, settlement = settlement) && group.hasMembers(listOf(settlement.from, settlement.to))

    fun isValid(settlement: Settlement, groupCurrency: Currency): Boolean {
        val amount = settlement.amount
        val isWithinLimits = amount.minorUnits <= MoneyFormat.maxMinorUnits(amount.currency) && isValidTime(settlement.settledAt)
        return amount.currency == groupCurrency && amount.isPositive && isWithinLimits && settlement.from != settlement.to
    }

    fun isValidGroupName(name: String): Boolean = name.isNotBlank() && name.length <= Group.MAX_NAME_LENGTH

    fun isValidMemberName(name: String): Boolean = name.isNotBlank() && name.length <= Member.MAX_NAME_LENGTH

    private fun isValidTitle(title: String): Boolean = title.isNotBlank() && title.length <= Expense.MAX_TITLE_LENGTH

    private fun Group.hasMembers(ids: List<MemberId>): Boolean {
        val memberIds = members.mapTo(HashSet()) { it.id }
        return ids.all { it in memberIds }
    }

    private fun isValidTime(instant: Instant): Boolean = instant.toEpochMilliseconds() in MIN_TIMESTAMP_MILLIS until MAX_TIMESTAMP_MILLIS
}
