package org.example.evenly.data.sources

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_groups")
data class GroupEntity(
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean,
    @ColumnInfo(defaultValue = "1") val isDirty: Boolean,
    val createdAtEpochMillis: Long,
    @ColumnInfo(defaultValue = "0") val modifiedAtEpochMillis: Long,
    val currencyCode: String,
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(defaultValue = "NULL") val inviteCode: String?,
)
