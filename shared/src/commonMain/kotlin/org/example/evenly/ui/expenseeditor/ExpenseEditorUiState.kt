package org.example.evenly.ui.expenseeditor

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.ExpenseSplitter
import org.example.evenly.domain.SplitError
import org.example.evenly.model.Currency
import org.example.evenly.model.ExchangeRate
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.Group
import org.example.evenly.model.Member
import org.example.evenly.model.MemberId
import org.example.evenly.model.Money
import org.example.evenly.model.SplitRule
import org.example.evenly.util.MoneyFormat
import org.example.evenly.util.PercentFormat
import org.example.evenly.util.RateFormat
import kotlin.time.Instant

@Immutable
data class ExpenseEditorUiState(
    val isDeleteFailed: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val isMissing: Boolean = false,
    val isSaveFailed: Boolean = false,
    val isSaving: Boolean = false,
    val amountText: String = "",
    val rateText: String = "",
    val title: String = "",
    val formerMemberIds: List<MemberId> = emptyList(),
    val exactAmountTexts: Map<MemberId, String> = emptyMap(),
    val percentageTexts: Map<MemberId, String> = emptyMap(),
    val participantIds: Set<MemberId> = emptySet(),
    val currency: Currency? = null,
    val expenseId: ExpenseId? = null,
    val group: Group? = null,
    val spentAt: Instant? = null,
    val paidBy: MemberId? = null,
    val splitMode: SplitMode = SplitMode.EQUAL,
) {
    val assignedBasisPoints: Int by lazy { members.sumOf { PercentFormat.parseBasisPoints(percentageTexts[it.id].orEmpty()) ?: 0 } }

    val members: List<Member> by lazy {
        val groupMembers = group?.members.orEmpty()
        val groupMemberIds = groupMembers.map { it.id }.toSet()
        groupMembers + formerMemberIds.filterNot { it in groupMemberIds }.map { Member(name = "", id = it) }
    }

    val shares: Map<MemberId, Money> by lazy {
        val currentAmount = amount
        val rule = splitRule
        if (currentAmount == null || rule == null || splitError != null) emptyMap() else ExpenseSplitter.shares(currentAmount, rule)
    }

    val exchangeRate: ExchangeRate? by lazy {
        val from = expenseCurrency
        val to = group?.currency
        val micros = RateFormat.parseMicros(rateText)
        if (from == null || to == null || from == to || micros == null) null else ExchangeRate(micros = micros, from = from, to = to)
    }

    val paidByMember: Member? by lazy { members.firstOrNull { it.id == paidBy } }

    val amount: Money? by lazy {
        val currentCurrency = expenseCurrency
        val minorUnits = currentCurrency?.let { MoneyFormat.parseMinorUnits(amountText, it) }
        if (currentCurrency == null || minorUnits == null) null else Money(minorUnits = minorUnits, currency = currentCurrency)
    }

    val assignedAmount: Money? by lazy { expenseCurrency?.let { currentCurrency -> Money(minorUnits = members.sumOf { MoneyFormat.parseMinorUnits(exactAmountTexts[it.id].orEmpty(), currentCurrency) ?: 0L }, currency = currentCurrency) } }
    val groupAmount: Money? by lazy { amount?.let { currentAmount -> exchangeRate?.convert(currentAmount) } }

    val saveBlocker: SaveBlocker? by lazy {
        when {
            title.isBlank() -> SaveBlocker.TITLE
            amount == null -> SaveBlocker.AMOUNT
            isForeignCurrency && exchangeRate == null -> SaveBlocker.EXCHANGE_RATE
            paidBy == null -> SaveBlocker.PAYER
            splitRule == null || splitError != null -> SaveBlocker.SPLIT
            else -> null
        }
    }

    val splitError: SplitError? by lazy {
        val currentAmount = amount
        val rule = splitRule
        if (currentAmount == null || rule == null) null else ExpenseSplitter.validate(currentAmount, rule)
    }

    val splitRule: SplitRule? by lazy {
        val currentCurrency = expenseCurrency
        if (currentCurrency == null) {
            null
        } else {
            when (splitMode) {
                SplitMode.EQUAL -> SplitRule.Equal(participants = members.map { it.id }.filter { it in participantIds })
                SplitMode.EXACT -> members.associate { member -> member.id to (parseOrZero(exactAmountTexts[member.id]) { MoneyFormat.parseMinorUnits(it, currentCurrency) } ?: return@lazy null) }.let { SplitRule.Exact(minorUnits = it) }
                SplitMode.PERCENTAGE -> members.associate { member -> member.id to (parseOrZero(percentageTexts[member.id]) { PercentFormat.parseBasisPoints(it)?.toLong() }?.toInt() ?: return@lazy null) }.let { SplitRule.Percentage(basisPoints = it) }
            }
        }
    }

    val canSave: Boolean get() = !isLoading && !isMissing && !isSaving && saveBlocker == null
    val isEditing: Boolean get() = expenseId != null
    val isForeignCurrency: Boolean get() = expenseCurrency != null && group != null && expenseCurrency != group.currency

    val expenseCurrency: Currency? get() = currency ?: group?.currency

    private fun parseOrZero(text: String?, parse: (String) -> Long?): Long? = if (text.isNullOrBlank()) 0L else parse(text)
}
