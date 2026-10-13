package org.example.evenly.core.data

import org.example.evenly.core.database.GroupWithMembers
import org.example.evenly.core.model.Currency
import org.example.evenly.core.model.Group
import org.example.evenly.core.model.GroupId
import org.example.evenly.core.model.Member
import org.example.evenly.core.model.MemberId
import kotlin.time.Instant

internal fun GroupWithMembers.toGroupOrNull(): Group? {
    val currency = Currency.fromCode(group.currencyCode) ?: return null
    return Group(
        name = group.name,
        inviteCode = group.inviteCode,
        members = members.filterNot { it.isDeleted }.sortedWith(compareBy({ it.position }, { it.id })).map { Member(name = it.name, id = MemberId(it.id)) },
        currency = currency,
        id = GroupId(group.id),
        createdAt = Instant.fromEpochMilliseconds(group.createdAtEpochMillis),
    )
}
