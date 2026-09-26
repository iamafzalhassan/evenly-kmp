package org.example.evenly.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.evenly.data.sources.ExpenseDao
import org.example.evenly.domain.ExpenseSplitter
import org.example.evenly.model.ExchangeRate
import org.example.evenly.model.Expense
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.GroupId
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExpenseRepository internal constructor(private val onLocalChange: () -> Unit, private val expenseDao: ExpenseDao) {
    suspend fun deleteExpense(id: ExpenseId) {
        expenseDao.markExpenseDeleted(id = id.raw, modifiedAtEpochMillis = Clock.System.now().toEpochMilliseconds())
        onLocalChange()
    }

    suspend fun findExpense(id: ExpenseId): Expense? = expenseDao.findExpense(id.raw)?.toExpenseOrNull()

    fun observeExpenses(groupId: GroupId): Flow<List<Expense>> = expenseDao.observeExpenses(groupId.raw).map { expenses -> expenses.mapNotNull { it.toExpenseOrNull() } }

    suspend fun saveExpense(title: String, exchangeRate: ExchangeRate?, id: ExpenseId?, groupId: GroupId, spentAt: Instant?, paidBy: MemberId, amount: Money, split: SplitRule) {
        val error = ExpenseSplitter.validate(amount, split)
        require(error == null) { "Invalid split: $error" }
        require(exchangeRate == null || exchangeRate.from == amount.currency) { "The exchange rate must convert from ${amount.currency}" }
        val now = Clock.System.now()
        val expense = Expense(
            title = title.trim(),
            exchangeRate = exchangeRate,
            id = id ?: ExpenseId(Uuid.random().toString()),
            spentAt = spentAt ?: now,
            paidBy = paidBy,
            amount = amount,
            split = split,
        )
        expenseDao.saveExpenseWithShares(expense = expense.toEntity(groupId = groupId.raw, modifiedAtEpochMillis = now.toEpochMilliseconds()), shares = expense.toShareEntities())
        onLocalChange()
    }
}
