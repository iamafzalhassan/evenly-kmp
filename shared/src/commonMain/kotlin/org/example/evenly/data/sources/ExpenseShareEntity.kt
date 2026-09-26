package org.example.evenly.data.sources

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    foreignKeys = [ForeignKey(childColumns = ["expenseId"], entity = ExpenseEntity::class, onDelete = ForeignKey.CASCADE, parentColumns = ["id"])],
    indices = [Index(value = ["expenseId"]), Index(value = ["memberId"])],
    primaryKeys = ["expenseId", "memberId"],
    tableName = "expense_shares",
)
data class ExpenseShareEntity(val position: Int, val value: Long, val expenseId: String, val memberId: String)
