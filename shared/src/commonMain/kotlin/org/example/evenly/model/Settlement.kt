package org.example.evenly.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class Settlement(val settledAt: Instant, val from: MemberId, val to: MemberId, val amount: Money, val id: SettlementId)
