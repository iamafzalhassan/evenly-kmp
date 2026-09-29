package org.example.evenly.domain

import org.example.evenly.model.Balance
import org.example.evenly.model.Currency
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.Transfer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettleUpPlannerTest {
    private val alice: MemberId = MemberId("alice")
    private val bob: MemberId = MemberId("bob")
    private val chen: MemberId = MemberId("chen")
    private val dana: MemberId = MemberId("dana")

    @Test
    fun everyBalanceClearsInAtMostOneFewerPaymentThanMembers() {
        val balances = listOf(balance(alice, 7_000L), balance(bob, -2_500L), balance(chen, -4_000L), balance(dana, -500L))
        val transfers = SettleUpPlanner.plan(balances)

        assertTrue(transfers.size <= balances.size - 1)
        assertTrue(remainingAfter(balances, transfers).values.all { it == 0L })
    }

    @Test
    fun largestDebtorPaysLargestCreditorFirst() {
        val transfers = SettleUpPlanner.plan(listOf(balance(alice, 5_000L), balance(bob, -3_000L), balance(chen, -2_000L)))

        assertEquals(listOf(Transfer(from = bob, to = alice, amount = lkr(3_000L)), Transfer(from = chen, to = alice, amount = lkr(2_000L))), transfers)
    }

    @Test
    fun randomBalancesClearInAtMostOneFewerPaymentThanMembers() {
        repeat(1_000) { seed ->
            val balances = randomBalances(Random(seed))
            val transfers = SettleUpPlanner.plan(balances)

            assertTrue(transfers.size <= balances.size - 1, "seed $seed")
            assertTrue(transfers.all { it.amount.isPositive && it.from != it.to }, "seed $seed")
            assertTrue(remainingAfter(balances, transfers).values.all { it == 0L }, "seed $seed")
        }
    }

    @Test
    fun settledGroupNeedsNoPayments() {
        assertEquals(emptyList(), SettleUpPlanner.plan(listOf(balance(alice, 0L), balance(bob, 0L))))
        assertEquals(emptyList(), SettleUpPlanner.plan(emptyList()))
    }

    private fun randomBalances(random: Random): List<Balance> {
        val nets = List(random.nextInt(1, 16)) { if (random.nextInt(5) == 0) 0L else random.nextLong(-500_000L, 500_000L) }.dropLast(1)
        return (nets + -nets.sum()).shuffled(random).mapIndexed { index, minorUnits -> balance(MemberId("member$index"), minorUnits) }
    }

    private fun balance(memberId: MemberId, minorUnits: Long): Balance = Balance(memberId = memberId, net = lkr(minorUnits))

    private fun lkr(minorUnits: Long): Money = Money(minorUnits = minorUnits, currency = Currency.LKR)

    private fun remainingAfter(balances: List<Balance>, transfers: List<Transfer>): Map<MemberId, Long> {
        val remaining = balances.associate { it.memberId to it.net.minorUnits }.toMutableMap()
        transfers.forEach { transfer ->
            remaining[transfer.from] = remaining.getValue(transfer.from) + transfer.amount.minorUnits
            remaining[transfer.to] = remaining.getValue(transfer.to) - transfer.amount.minorUnits
        }
        return remaining
    }
}
