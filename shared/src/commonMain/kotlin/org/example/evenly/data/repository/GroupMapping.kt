package org.example.evenly.data.repository

import org.example.evenly.data.local.entity.GroupWithMembers
import org.example.evenly.domain.group.Group
import org.example.evenly.domain.group.GroupId
import org.example.evenly.domain.group.Member
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Currency
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
