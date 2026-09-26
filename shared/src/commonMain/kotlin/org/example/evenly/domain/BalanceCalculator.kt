package org.example.evenly.domain

import org.example.evenly.model.Balance
import org.example.evenly.model.Expense
import org.example.evenly.model.Group
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.Settlement

private fun MutableMap<MemberId, Long>.credit(id: MemberId, minorUnits: Long) {
    this[id] = getOrElse(id) { 0L } + minorUnits
}

object BalanceCalculator {
    fun balances(group: Group, expenses: List<Expense>, settlements: List<Settlement>): List<Balance> {
        val net = group.members.associate { it.id to 0L }.toMutableMap()
        expenses.forEach { expense ->
            net.credit(expense.paidBy, ExpenseValuation.groupAmount(expense).minorUnits)
            ExpenseValuation.groupShares(expense).forEach { (id, share) -> net.credit(id, -share.minorUnits) }
        }
        settlements.forEach { settlement ->
            net.credit(settlement.from, settlement.amount.minorUnits)
            net.credit(settlement.to, -settlement.amount.minorUnits)
        }
        return net.map { (id, minorUnits) -> Balance(memberId = id, net = Money(minorUnits = minorUnits, currency = group.currency)) }
    }
}
