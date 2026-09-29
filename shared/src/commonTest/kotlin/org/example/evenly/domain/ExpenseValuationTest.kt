package org.example.evenly.domain

import org.example.evenly.model.Currency
import org.example.evenly.model.ExchangeRate
import org.example.evenly.model.Expense
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ExpenseValuationTest {
    private val alice: MemberId = MemberId("alice")
    private val bob: MemberId = MemberId("bob")
    private val chen: MemberId = MemberId("chen")

    @Test
    fun convertedSharesAddUpWhenRoundingFallsShort() {
        val expense = expense(ExchangeRate(micros = 1_100_000L, from = Currency.EUR, to = Currency.LKR))
        val shares = ExpenseValuation.groupShares(expense)

        assertEquals(lkr(110L), ExpenseValuation.groupAmount(expense))
        assertEquals(mapOf(alice to lkr(38L), bob to lkr(36L), chen to lkr(36L)), shares)
    }

    @Test
    fun convertedSharesAddUpWhenRoundingOvershoots() {
        val expense = expense(ExchangeRate(micros = 1_500_000L, from = Currency.EUR, to = Currency.LKR))
        val shares = ExpenseValuation.groupShares(expense)

        assertEquals(lkr(150L), ExpenseValuation.groupAmount(expense))
        assertEquals(mapOf(alice to lkr(50L), bob to lkr(50L), chen to lkr(50L)), shares)
    }

    @Test
    fun sharesInTheGroupCurrencyAreNotConverted() {
        val expense = expense(null)

        assertEquals(eur(100L), ExpenseValuation.groupAmount(expense))
        assertEquals(mapOf(alice to eur(34L), bob to eur(33L), chen to eur(33L)), ExpenseValuation.groupShares(expense))
    }

    private fun expense(exchangeRate: ExchangeRate?): Expense = Expense(
        title = "Dinner",
        exchangeRate = exchangeRate,
        id = ExpenseId("dinner"),
        spentAt = Instant.fromEpochMilliseconds(0L),
        paidBy = alice,
        amount = eur(100L),
        split = SplitRule.Equal(listOf(alice, bob, chen)),
    )

    private fun eur(minorUnits: Long): Money = Money(minorUnits = minorUnits, currency = Currency.EUR)

    private fun lkr(minorUnits: Long): Money = Money(minorUnits = minorUnits, currency = Currency.LKR)
}
