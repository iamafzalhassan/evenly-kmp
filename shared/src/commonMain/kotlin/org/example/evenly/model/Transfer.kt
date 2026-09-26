package org.example.evenly.model

import androidx.compose.runtime.Immutable

@Immutable
data class Transfer(val from: MemberId, val to: MemberId, val amount: Money)
