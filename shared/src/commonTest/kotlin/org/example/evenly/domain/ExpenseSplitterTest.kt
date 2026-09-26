package org.example.evenly.domain

import org.example.evenly.model.Currency
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ExpenseSplitterTest {
    private val alice: MemberId = MemberId("alice")
    private val bob: MemberId = MemberId("bob")
    private val chen: MemberId = MemberId("chen")

    private val hundredRupees: Money = Money(minorUnits = 10_000L, currency = Currency.LKR)

    @Test
    fun equalSplitGivesLeftoverCentsToTheFirstParticipants() {
        val shares = ExpenseSplitter.shares(hundredRupees, SplitRule.Equal(listOf(alice, bob, chen)))

        assertEquals(mapOf(alice to lkr(3_334L), bob to lkr(3_333L), chen to lkr(3_333L)), shares)
        assertEquals(hundredRupees.minorUnits, shares.values.sumOf { it.minorUnits })
    }

    @Test
    fun exactSplitMustAddUpToTheTotal() {
        val rule = SplitRule.Exact(mapOf(alice to 6_000L, bob to 3_000L))

        assertEquals(SplitError.SHARES_DO_NOT_MATCH_TOTAL, ExpenseSplitter.validate(hundredRupees, rule))
        assertFailsWith<IllegalArgumentException> { ExpenseSplitter.shares(hundredRupees, rule) }
    }

    @Test
    fun percentageSplitUsesTheLargestRemainder() {
        val total = lkr(100L)
        val shares = ExpenseSplitter.shares(total, SplitRule.Percentage(mapOf(alice to 3_333, bob to 3_333, chen to 3_334)))

        assertEquals(mapOf(alice to lkr(33L), bob to lkr(33L), chen to lkr(34L)), shares)
        assertEquals(total.minorUnits, shares.values.sumOf { it.minorUnits })
    }

    @Test
    fun validateRejectsDuplicateAndMissingParticipants() {
        assertEquals(SplitError.DUPLICATE_PARTICIPANT, ExpenseSplitter.validate(hundredRupees, SplitRule.Equal(listOf(alice, alice))))
        assertEquals(SplitError.NO_PARTICIPANTS, ExpenseSplitter.validate(hundredRupees, SplitRule.Equal(emptyList())))
        assertEquals(SplitError.NON_POSITIVE_AMOUNT, ExpenseSplitter.validate(lkr(0L), SplitRule.Equal(listOf(alice))))
        assertNull(ExpenseSplitter.validate(hundredRupees, SplitRule.Equal(listOf(alice, bob))))
    }

    private fun lkr(minorUnits: Long): Money = Money(minorUnits = minorUnits, currency = Currency.LKR)
}
