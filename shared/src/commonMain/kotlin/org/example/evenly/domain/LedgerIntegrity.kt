package org.example.evenly.domain

import org.example.evenly.model.Currency
import org.example.evenly.model.Expense
import org.example.evenly.model.Settlement
import org.example.evenly.util.MoneyFormat
import org.example.evenly.util.RateFormat

object LedgerIntegrity {
    fun isValid(expense: Expense, groupCurrency: Currency): Boolean {
        val amount = expense.amount
        val rate = expense.exchangeRate
        val isRateConsistent = if (rate == null) {
            amount.currency == groupCurrency
        } else {
            amount.currency != groupCurrency && rate.from == amount.currency && rate.to == groupCurrency && rate.micros in 1L..RateFormat.maxMicros()
        }
        return isRateConsistent && amount.minorUnits <= MoneyFormat.maxMinorUnits(amount.currency) && ExpenseSplitter.validate(amount, expense.split) == null
    }

    fun isValid(settlement: Settlement, groupCurrency: Currency): Boolean {
        val amount = settlement.amount
        return amount.currency == groupCurrency && amount.isPositive && settlement.from != settlement.to
    }
}
