package org.example.evenly.domain

import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule

object ExpenseSplitter {
    const val FULL_BASIS_POINTS: Int = 10_000

    fun shares(amount: Money, rule: SplitRule): Map<MemberId, Money> {
        val error = validate(amount, rule)
        require(error == null) { "Invalid split: $error" }
        val minorUnits = when (rule) {
            is SplitRule.Equal -> equalShares(amount.minorUnits, rule.participants)
            is SplitRule.Exact -> rule.minorUnits
            is SplitRule.Percentage -> percentageShares(amount.minorUnits, rule.basisPoints)
        }
        return minorUnits.mapValues { (_, units) -> Money(minorUnits = units, currency = amount.currency) }
    }

    fun validate(amount: Money, rule: SplitRule): SplitError? = when {
        !amount.isPositive -> SplitError.NON_POSITIVE_AMOUNT
        rule is SplitRule.Equal && rule.participants.isEmpty() -> SplitError.NO_PARTICIPANTS
        rule is SplitRule.Equal && rule.participants.toSet().size != rule.participants.size -> SplitError.DUPLICATE_PARTICIPANT
        rule is SplitRule.Exact && rule.minorUnits.isEmpty() -> SplitError.NO_PARTICIPANTS
        rule is SplitRule.Exact && rule.minorUnits.values.any { it < 0L } -> SplitError.NEGATIVE_SHARE
        rule is SplitRule.Exact && rule.minorUnits.values.sum() != amount.minorUnits -> SplitError.SHARES_DO_NOT_MATCH_TOTAL
        rule is SplitRule.Percentage && rule.basisPoints.isEmpty() -> SplitError.NO_PARTICIPANTS
        rule is SplitRule.Percentage && rule.basisPoints.values.any { it < 0 } -> SplitError.NEGATIVE_SHARE
        rule is SplitRule.Percentage && rule.basisPoints.values.sum() != FULL_BASIS_POINTS -> SplitError.SHARES_DO_NOT_MATCH_TOTAL
        else -> null
    }

    private fun equalShares(total: Long, participants: List<MemberId>): Map<MemberId, Long> {
        val base = total / participants.size
        val remainder = total % participants.size
        return participants.withIndex().associate { (index, id) -> id to base + if (index < remainder) 1L else 0L }
    }

    private fun percentageShares(total: Long, basisPoints: Map<MemberId, Int>): Map<MemberId, Long> {
        val products = basisPoints.mapValues { (_, points) -> total * points }
        val floors = products.mapValues { (_, product) -> product / FULL_BASIS_POINTS }
        val leftover = total - floors.values.sum()
        val roundedUp = products.entries.sortedByDescending { (_, product) -> product % FULL_BASIS_POINTS }.take(leftover.toInt()).map { it.key }.toSet()
        return floors.mapValues { (id, floor) -> floor + if (id in roundedUp) 1L else 0L }
    }
}
