package org.example.evenly.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class Expense(val title: String, val exchangeRate: ExchangeRate?, val id: ExpenseId, val spentAt: Instant, val paidBy: MemberId, val amount: Money, val split: SplitRule)
