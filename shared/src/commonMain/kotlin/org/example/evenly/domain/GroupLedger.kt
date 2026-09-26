package org.example.evenly.domain

import org.example.evenly.model.Balance
import org.example.evenly.model.Expense
import org.example.evenly.model.Group
import org.example.evenly.model.Money
import org.example.evenly.model.Settlement
import org.example.evenly.model.Transfer
import org.example.evenly.model.total

data class GroupLedger(
    val balances: List<Balance>,
    val expenses: List<Expense>,
    val settlements: List<Settlement>,
    val transfers: List<Transfer>,
    val group: Group,
    val totalPaidBack: Money,
    val totalSpent: Money,
) {
    companion object {
        fun build(group: Group, expenses: List<Expense>, settlements: List<Settlement>): GroupLedger {
            val validExpenses = expenses.filter { LedgerIntegrity.isValid(expense = it, groupCurrency = group.currency) }
            val validSettlements = settlements.filter { LedgerIntegrity.isValid(groupCurrency = group.currency, settlement = it) }
            val balances = BalanceCalculator.balances(expenses = validExpenses, group = group, settlements = validSettlements)
            return GroupLedger(
                balances = balances,
                expenses = validExpenses,
                settlements = validSettlements,
                transfers = SettleUpPlanner.plan(balances),
                group = group,
                totalPaidBack = validSettlements.map { it.amount }.total(group.currency),
                totalSpent = validExpenses.map(ExpenseValuation::groupAmount).total(group.currency),
            )
        }
    }
}
