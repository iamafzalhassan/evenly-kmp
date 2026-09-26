package org.example.evenly.data.sources

interface SyncedRow {
    val isDirty: Boolean

    val modifiedAtEpochMillis: Long
}

data class RowSyncState(override val isDirty: Boolean, override val modifiedAtEpochMillis: Long, val id: String) : SyncedRow

data class GroupSyncState(override val isDirty: Boolean, override val modifiedAtEpochMillis: Long, val currencyCode: String, val id: String, val inviteCode: String?) : SyncedRow
