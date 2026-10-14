package org.example.evenly.data.local.entity

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RowSyncStateTest {
    @Test
    fun aCleanRowAcceptsAnyOtherVersion() {
        assertTrue(state(isDirty = false, modifiedAtEpochMillis = 200L).acceptsRemote(100L))
        assertTrue(state(isDirty = false, modifiedAtEpochMillis = 200L).acceptsRemote(300L))
    }

    @Test
    fun aCleanRowSkipsTheVersionItAlreadyHolds() {
        assertFalse(state(isDirty = false, modifiedAtEpochMillis = 200L).acceptsRemote(200L))
    }

    @Test
    fun aDirtyRowKeepsANewerLocalEdit() {
        assertFalse(state(isDirty = true, modifiedAtEpochMillis = 300L).acceptsRemote(200L))
    }

    @Test
    fun aDirtyRowTakesANewerRemoteEdit() {
        assertTrue(state(isDirty = true, modifiedAtEpochMillis = 200L).acceptsRemote(300L))
    }

    @Test
    fun anUnknownRowIsAccepted() {
        val row: SyncedRow? = null

        assertTrue(row.acceptsRemote(100L))
    }

    private fun state(isDirty: Boolean, modifiedAtEpochMillis: Long): RowSyncState = RowSyncState(isDirty = isDirty, modifiedAtEpochMillis = modifiedAtEpochMillis, id = "row")
}
