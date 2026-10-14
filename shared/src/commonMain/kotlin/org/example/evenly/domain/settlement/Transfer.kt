package org.example.evenly.domain.settlement

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Money

@Immutable
data class Transfer(val from: MemberId, val to: MemberId, val amount: Money)
