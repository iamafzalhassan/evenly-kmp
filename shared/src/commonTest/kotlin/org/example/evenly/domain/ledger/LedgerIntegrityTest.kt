package org.example.evenly.domain.ledger

import org.example.evenly.domain.expense.Expense
import org.example.evenly.domain.expense.ExpenseId
import org.example.evenly.domain.expense.SplitRule
import org.example.evenly.domain.group.Group
import org.example.evenly.domain.group.GroupId
import org.example.evenly.domain.group.Member
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Currency
import org.example.evenly.domain.money.Money
import org.example.evenly.domain.settlement.Settlement
import org.example.evenly.domain.settlement.SettlementId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class LedgerIntegrityTest {
    private val group: Group = Group(
        name = "Flat",
        inviteCode = null,
        members = listOf(Member(name = "Alice", id = MemberId("alice")), Member(name = "Bob", id = MemberId("bob")), Member(name = "Chen", id = MemberId("chen"))),
        currency = Currency.EUR,
        id = GroupId("flat"),
        createdAt = Instant.fromEpochMilliseconds(0L),
    )

    private val alice: MemberId = MemberId("alice")
    private val bob: MemberId = MemberId("bob")
    private val chen: MemberId = MemberId("chen")
    private val dana: MemberId = MemberId("dana")

    @Test
    fun expenseForAMemberOutsideTheGroupIsInvalid() {
        val expense = expense(split = SplitRule.Equal(listOf(alice, dana)))

        assertTrue(LedgerIntegrity.isValid(expense = expense, groupCurrency = Currency.EUR))
        assertFalse(LedgerIntegrity.isValid(expense = expense, group = group))
    }

    @Test
    fun expenseInsideTheGroupIsValid() {
        assertTrue(LedgerIntegrity.isValid(expense = expense(), group = group))
    }

    @Test
    fun expenseOutsideTheSupportedDateRangeIsInvalid() {
        assertFalse(LedgerIntegrity.isValid(expense = expense(spentAt = Instant.fromEpochMilliseconds(LedgerIntegrity.MAX_TIMESTAMP_MILLIS)), group = group))
        assertFalse(LedgerIntegrity.isValid(expense = expense(spentAt = Instant.fromEpochMilliseconds(LedgerIntegrity.MIN_TIMESTAMP_MILLIS - 1L)), group = group))
    }

    @Test
    fun expenseWithAnOverlongTitleIsInvalid() {
        assertTrue(LedgerIntegrity.isValid(expense = expense(title = "x".repeat(Expense.MAX_TITLE_LENGTH)), group = group))
        assertFalse(LedgerIntegrity.isValid(expense = expense(title = "x".repeat(Expense.MAX_TITLE_LENGTH + 1)), group = group))
    }

    @Test
    fun namesMustBePresentAndWithinTheLimit() {
        assertTrue(LedgerIntegrity.isValidGroupName("Trip"))
        assertFalse(LedgerIntegrity.isValidGroupName(" "))
        assertFalse(LedgerIntegrity.isValidGroupName("x".repeat(Group.MAX_NAME_LENGTH + 1)))
        assertTrue(LedgerIntegrity.isValidMemberName("x".repeat(Member.MAX_NAME_LENGTH)))
        assertFalse(LedgerIntegrity.isValidMemberName("x".repeat(Member.MAX_NAME_LENGTH + 1)))
    }

    @Test
    fun settlementAboveTheAmountLimitIsInvalid() {
        val limit = Money.maxMinorUnits(Currency.EUR)

        assertTrue(LedgerIntegrity.isValid(group = group, settlement = settlement(amount = eur(limit))))
        assertFalse(LedgerIntegrity.isValid(group = group, settlement = settlement(amount = eur(limit + 1L))))
    }

    @Test
    fun settlementToSomeoneOutsideTheGroupIsInvalid() {
        assertTrue(LedgerIntegrity.isValid(groupCurrency = Currency.EUR, settlement = settlement(to = dana)))
        assertFalse(LedgerIntegrity.isValid(group = group, settlement = settlement(to = dana)))
    }

    private fun expense(title: String = "Dinner", spentAt: Instant = Instant.fromEpochMilliseconds(0L), split: SplitRule = SplitRule.Equal(listOf(alice, bob, chen))): Expense = Expense(
        title = title,
        exchangeRate = null,
        id = ExpenseId("dinner"),
        spentAt = spentAt,
        paidBy = alice,
        amount = eur(300L),
        split = split,
    )

    private fun settlement(to: MemberId = bob, amount: Money = eur(100L)): Settlement = Settlement(settledAt = Instant.fromEpochMilliseconds(0L), from = alice, to = to, amount = amount, id = SettlementId("payment"))

    private fun eur(minorUnits: Long): Money = Money(minorUnits = minorUnits, currency = Currency.EUR)
}
