package org.example.evenly.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExpenseDao {
    @Query("DELETE FROM expense_shares WHERE expenseId = :expenseId")
    abstract suspend fun deleteShares(expenseId: String)

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id AND isDeleted = 0")
    abstract suspend fun findExpense(id: String): ExpenseWithShares?

    @Insert
    abstract suspend fun insertShares(shares: List<ExpenseShareEntity>)

    @Query("UPDATE expenses SET isDeleted = 1, isDirty = 1, modifiedAtEpochMillis = :modifiedAtEpochMillis WHERE id = :id")
    abstract suspend fun markExpenseDeleted(modifiedAtEpochMillis: Long, id: String)

    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND isDeleted = 0 ORDER BY spentAtEpochMillis DESC")
    abstract fun observeExpenses(groupId: String): Flow<List<ExpenseWithShares>>

    @Upsert
    abstract suspend fun upsertExpense(expense: ExpenseEntity)

    @Transaction
    open suspend fun saveExpenseWithShares(expense: ExpenseEntity, shares: List<ExpenseShareEntity>) {
        upsertExpense(expense)
        deleteShares(expense.id)
        insertShares(shares)
    }
}
