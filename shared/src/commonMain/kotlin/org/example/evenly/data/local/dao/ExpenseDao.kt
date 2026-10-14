package org.example.evenly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.example.evenly.data.local.entity.ExpenseEntity
import org.example.evenly.data.local.entity.ExpenseShareEntity
import org.example.evenly.data.local.entity.ExpenseWithShares

@Dao
abstract class ExpenseDao {
    @Query("DELETE FROM expense_shares WHERE expenseId = :expenseId")
    abstract suspend fun deleteShares(expenseId: String)

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id AND isDeleted = 0")
    abstract suspend fun findExpense(id: String): ExpenseWithShares?

    @Query("SELECT modifiedAtEpochMillis FROM expenses WHERE id = :id")
    abstract suspend fun findModifiedAt(id: String): Long?

    @Insert
    abstract suspend fun insertShares(shares: List<ExpenseShareEntity>)

    @Query("UPDATE expenses SET isDeleted = 1, isDirty = 1, modifiedAtEpochMillis = MAX(:modifiedAtEpochMillis, modifiedAtEpochMillis + 1) WHERE id = :id")
    abstract suspend fun markExpenseDeleted(modifiedAtEpochMillis: Long, id: String)

    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND isDeleted = 0 ORDER BY spentAtEpochMillis DESC")
    abstract fun observeExpenses(groupId: String): Flow<List<ExpenseWithShares>>

    @Upsert
    abstract suspend fun upsertExpense(expense: ExpenseEntity)

    @Transaction
    open suspend fun saveExpenseWithShares(expense: ExpenseEntity, shares: List<ExpenseShareEntity>) {
        val previousModifiedAt = findModifiedAt(expense.id)
        upsertExpense(if (previousModifiedAt == null) expense else expense.copy(modifiedAtEpochMillis = maxOf(expense.modifiedAtEpochMillis, previousModifiedAt + 1)))
        deleteShares(expense.id)
        insertShares(shares)
    }
}
