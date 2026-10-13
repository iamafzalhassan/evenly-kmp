package org.example.evenly.core.database

interface SyncedRow {
    val isDirty: Boolean

    val modifiedAtEpochMillis: Long
}

data class RowSyncState(override val isDirty: Boolean, override val modifiedAtEpochMillis: Long, val id: String) : SyncedRow

data class GroupSyncState(override val isDirty: Boolean, override val modifiedAtEpochMillis: Long, val currencyCode: String, val id: String, val inviteCode: String?) : SyncedRow

internal fun SyncedRow?.acceptsRemote(remoteModifiedAtEpochMillis: Long): Boolean = when {
    this == null -> true
    isDirty -> modifiedAtEpochMillis <= remoteModifiedAtEpochMillis
    else -> modifiedAtEpochMillis != remoteModifiedAtEpochMillis
}
