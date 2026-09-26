package org.example.evenly.data

import org.example.evenly.data.sources.GroupWithMembers
import org.example.evenly.model.Currency
import org.example.evenly.model.Group
import org.example.evenly.model.GroupId
import org.example.evenly.model.Member
import org.example.evenly.model.MemberId
import kotlin.time.Instant

internal fun GroupWithMembers.toGroupOrNull(): Group? {
    val currency = Currency.fromCode(group.currencyCode) ?: return null
    return Group(
        name = group.name,
        inviteCode = group.inviteCode,
        members = members.filterNot { it.isDeleted }.sortedBy { it.position }.map { Member(name = it.name, id = MemberId(it.id)) },
        currency = currency,
        id = GroupId(group.id),
        createdAt = Instant.fromEpochMilliseconds(group.createdAtEpochMillis),
    )
}
