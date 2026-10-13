package org.example.evenly.core.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class Group(val name: String, val inviteCode: String?, val members: List<Member>, val currency: Currency, val id: GroupId, val createdAt: Instant) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 100
        const val MIN_MEMBERS: Int = 2
    }
}
