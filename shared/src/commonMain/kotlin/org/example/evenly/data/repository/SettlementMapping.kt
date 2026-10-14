package org.example.evenly.data.repository

import org.example.evenly.data.local.entity.SettlementEntity
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Currency
import org.example.evenly.domain.money.Money
import org.example.evenly.domain.settlement.Settlement
import org.example.evenly.domain.settlement.SettlementId
import kotlin.time.Instant

internal fun Settlement.toEntity(modifiedAtEpochMillis: Long, groupId: String): SettlementEntity = SettlementEntity(
    isDeleted = false,
    isDirty = true,
    amountMinorUnits = amount.minorUnits,
    modifiedAtEpochMillis = modifiedAtEpochMillis,
    settledAtEpochMillis = settledAt.toEpochMilliseconds(),
    currencyCode = amount.currency.name,
    fromMemberId = from.raw,
    groupId = groupId,
    id = id.raw,
    toMemberId = to.raw,
)

internal fun SettlementEntity.toSettlementOrNull(): Settlement? {
    val currency = Currency.fromCode(currencyCode) ?: return null
    return Settlement(
        settledAt = Instant.fromEpochMilliseconds(settledAtEpochMillis),
        from = MemberId(fromMemberId),
        to = MemberId(toMemberId),
        amount = Money(minorUnits = amountMinorUnits, currency = currency),
        id = SettlementId(id),
    )
}
