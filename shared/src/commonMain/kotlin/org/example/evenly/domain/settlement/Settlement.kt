package org.example.evenly.domain.settlement

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Money
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@JvmInline
value class SettlementId(val raw: String)

@Immutable
data class Settlement(val settledAt: Instant, val from: MemberId, val to: MemberId, val amount: Money, val id: SettlementId)
