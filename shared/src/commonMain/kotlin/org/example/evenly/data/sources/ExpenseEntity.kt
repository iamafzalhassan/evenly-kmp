package org.example.evenly.data.sources

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(childColumns = ["groupId"], entity = GroupEntity::class, onDelete = ForeignKey.CASCADE, parentColumns = ["id"])],
    indices = [Index(value = ["groupId"])],
    tableName = "expenses",
)
data class ExpenseEntity(
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean,
    @ColumnInfo(defaultValue = "1") val isDirty: Boolean,
    val amountMinorUnits: Long,
    @ColumnInfo(defaultValue = "0") val modifiedAtEpochMillis: Long,
    val spentAtEpochMillis: Long,
    val exchangeRateMicros: Long?,
    val currencyCode: String,
    val groupId: String,
    @PrimaryKey val id: String,
    val paidByMemberId: String,
    val splitKind: String,
    val title: String,
    val exchangeRateCurrencyCode: String?,
)
