package org.example.evenly.domain.expense

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.ExchangeRate
import org.example.evenly.domain.money.Money
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@JvmInline
value class ExpenseId(val raw: String)

@Immutable
data class Expense(val title: String, val exchangeRate: ExchangeRate?, val id: ExpenseId, val spentAt: Instant, val paidBy: MemberId, val amount: Money, val split: SplitRule) {
    companion object {
        const val MAX_PARTICIPANTS: Int = 500
        const val MAX_TITLE_LENGTH: Int = 200
    }
}
