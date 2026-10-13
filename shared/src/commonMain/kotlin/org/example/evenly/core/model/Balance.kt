package org.example.evenly.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Balance(val memberId: MemberId, val net: Money)
