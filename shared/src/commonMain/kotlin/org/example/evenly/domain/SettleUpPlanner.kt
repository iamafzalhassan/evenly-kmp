package org.example.evenly.domain

import org.example.evenly.model.Balance
import org.example.evenly.model.Money
import org.example.evenly.model.Transfer

object SettleUpPlanner {
    fun plan(balances: List<Balance>): List<Transfer> {
        val currency = balances.firstOrNull()?.net?.currency ?: return emptyList()
        val remaining = balances.associate { it.memberId to it.net.minorUnits }.toMutableMap()
        val transfers = mutableListOf<Transfer>()
        while (true) {
            val (creditorId, credit) = remaining.entries.filter { it.value > 0L }.maxByOrNull { it.value } ?: break
            val (debtorId, debt) = remaining.entries.filter { it.value < 0L }.minByOrNull { it.value } ?: break
            val minorUnits = minOf(credit, -debt)
            transfers += Transfer(from = debtorId, to = creditorId, amount = Money(minorUnits = minorUnits, currency = currency))
            remaining[creditorId] = credit - minorUnits
            remaining[debtorId] = debt + minorUnits
        }
        return transfers
    }
}
