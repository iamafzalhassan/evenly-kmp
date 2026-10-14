package org.example.evenly.domain.group

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.money.Currency
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@JvmInline
value class GroupId(val raw: String)

@Immutable
data class Group(val name: String, val inviteCode: String?, val members: List<Member>, val currency: Currency, val id: GroupId, val createdAt: Instant) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 100
        const val MIN_MEMBERS: Int = 2
    }
}
