package org.example.evenly.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.example.evenly.data.local.dao.ExpenseDao
import org.example.evenly.domain.expense.Expense
import org.example.evenly.domain.expense.ExpenseId
import org.example.evenly.domain.expense.ExpenseSplitter
import org.example.evenly.domain.expense.SplitRule
import org.example.evenly.domain.group.GroupId
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.ExchangeRate
import org.example.evenly.domain.money.Money
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExpenseRepository internal constructor(private val onLocalChange: () -> Unit, private val expenseDao: ExpenseDao) {
    suspend fun deleteExpense(id: ExpenseId) {
        expenseDao.markExpenseDeleted(id = id.raw, modifiedAtEpochMillis = Clock.System.now().toEpochMilliseconds())
        onLocalChange()
    }

    suspend fun findExpense(id: ExpenseId): Expense? = expenseDao.findExpense(id.raw)?.toExpenseOrNull()

    fun observeExpenses(groupId: GroupId): Flow<List<Expense>> = expenseDao.observeExpenses(groupId.raw).map { expenses -> expenses.mapNotNull { it.toExpenseOrNull() } }.flowOn(Dispatchers.Default)

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
