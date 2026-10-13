package org.example.evenly.core.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class Expense(val title: String, val exchangeRate: ExchangeRate?, val id: ExpenseId, val spentAt: Instant, val paidBy: MemberId, val amount: Money, val split: SplitRule) {
    companion object {
        const val MAX_PARTICIPANTS: Int = 500
        const val MAX_TITLE_LENGTH: Int = 200
    }
}
