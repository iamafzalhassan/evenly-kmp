package org.example.evenly.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithShares(@Relation(entityColumn = "expenseId", parentColumn = "id") val shares: List<ExpenseShareEntity>, @Embedded val expense: ExpenseEntity)
