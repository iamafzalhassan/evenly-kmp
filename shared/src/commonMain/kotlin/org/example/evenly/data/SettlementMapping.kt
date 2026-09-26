package org.example.evenly.data

import org.example.evenly.data.sources.SettlementEntity
import org.example.evenly.model.Currency
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.Settlement
import org.example.evenly.model.SettlementId
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
