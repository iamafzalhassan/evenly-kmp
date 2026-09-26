package org.example.evenly.domain

import org.example.evenly.model.Expense
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import kotlin.math.abs

object ExpenseValuation {
    fun groupShares(expense: Expense): Map<MemberId, Money> {
        val shares = ExpenseSplitter.shares(expense.amount, expense.split)
        val rate = expense.exchangeRate ?: return shares
        val total = groupAmount(expense)
        val converted = shares.mapValues { (_, share) -> rate.convert(share).minorUnits }
        val difference = total.minorUnits - converted.values.sum()
        val step = if (difference >= 0L) 1L else -1L
        val adjusted = converted.entries.sortedByDescending { it.value }.take(abs(difference).toInt()).map { it.key }.toSet()
        return converted.mapValues { (memberId, minorUnits) -> Money(minorUnits = minorUnits + if (memberId in adjusted) step else 0L, currency = total.currency) }
    }

    fun groupAmount(expense: Expense): Money = expense.exchangeRate?.convert(expense.amount) ?: expense.amount
}
