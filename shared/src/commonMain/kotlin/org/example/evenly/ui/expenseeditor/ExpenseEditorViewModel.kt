package org.example.evenly.ui.expenseeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.evenly.data.ExchangeRateRepository
import org.example.evenly.data.ExpenseRepository
import org.example.evenly.data.GroupRepository
import org.example.evenly.model.Currency
import org.example.evenly.model.Expense
import org.example.evenly.model.ExpenseId
import org.example.evenly.model.Group
import org.example.evenly.model.GroupId
import org.example.evenly.model.MemberId
import org.example.evenly.model.SplitRule
import org.example.evenly.model.memberIds
import org.example.evenly.util.LocalDates
import org.example.evenly.util.MoneyFormat
import org.example.evenly.util.PercentFormat
import org.example.evenly.util.RateFormat
import org.example.evenly.util.succeeds
import kotlin.time.Clock

class ExpenseEditorViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
    expenseId: ExpenseId?,
    private val expenseRepository: ExpenseRepository,
    private val groupId: GroupId,
    groupRepository: GroupRepository,
) : ViewModel() {
    private val mutableState: MutableStateFlow<ExpenseEditorUiState> = MutableStateFlow(ExpenseEditorUiState())

    val state: StateFlow<ExpenseEditorUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val group = groupRepository.observeGroup(groupId).first()
            val expense = expenseId?.let { expenseRepository.findExpense(it) }
            mutableState.update { state ->
                when {
                    expense != null -> state.editDraft(expense = expense, group = group)
                    expenseId != null -> state.copy(isLoading = false, isMissing = true, group = group)
                    else -> state.newDraft(group)
                }
            }
        }
    }

    fun onEvent(event: ExpenseEditorEvent) {
        when (event) {
            is ExpenseEditorEvent.ChangeAmount -> mutableState.update { state -> state.copy(amountText = event.text) }
            is ExpenseEditorEvent.ChangeCurrency -> changeCurrency(event.currency)
            is ExpenseEditorEvent.ChangeDate -> mutableState.update { state -> state.copy(spentAt = LocalDates.withDate(instant = state.spentAt ?: Clock.System.now(), utcDateMillis = event.utcDateMillis)) }
            is ExpenseEditorEvent.ChangeExactAmount -> mutableState.update { state -> state.copy(exactAmountTexts = state.exactAmountTexts + (event.memberId to event.text)) }
            is ExpenseEditorEvent.ChangePaidBy -> mutableState.update { state -> state.copy(paidBy = event.memberId) }
            is ExpenseEditorEvent.ChangePercentage -> mutableState.update { state -> state.copy(percentageTexts = state.percentageTexts + (event.memberId to event.text)) }
            is ExpenseEditorEvent.ChangeRate -> mutableState.update { state -> state.copy(rateText = event.text) }
            is ExpenseEditorEvent.ChangeSplitMode -> mutableState.update { state -> state.copy(splitMode = event.mode) }
            is ExpenseEditorEvent.ChangeTitle -> mutableState.update { state -> state.copy(title = event.text) }
            ExpenseEditorEvent.Delete -> delete()
            ExpenseEditorEvent.DismissDeleteFailure -> mutableState.update { state -> state.copy(isDeleteFailed = false) }
            ExpenseEditorEvent.DismissSaveFailure -> mutableState.update { state -> state.copy(isSaveFailed = false) }
            ExpenseEditorEvent.Save -> save()
            is ExpenseEditorEvent.ToggleParticipant -> mutableState.update { state -> state.copy(participantIds = state.participantIds.toggled(event.memberId)) }
        }
    }

    private fun ExpenseEditorUiState.editDraft(expense: Expense, group: Group?): ExpenseEditorUiState {
        val currency = expense.amount.currency
        val rule = expense.split
        val groupMemberIds = group?.members.orEmpty().map { it.id }.toSet()
        return copy(
            isLoading = false,
            amountText = MoneyFormat.formatInput(currency = currency, minorUnits = expense.amount.minorUnits),
            rateText = expense.exchangeRate?.let { RateFormat.formatInput(it.micros) }.orEmpty(),
            title = expense.title,
            formerMemberIds = (listOf(expense.paidBy) + rule.memberIds).distinct().filterNot { it in groupMemberIds },
            exactAmountTexts = if (rule is SplitRule.Exact) rule.minorUnits.mapValues { (_, minorUnits) -> MoneyFormat.formatInput(currency = currency, minorUnits = minorUnits) } else emptyMap(),
            percentageTexts = if (rule is SplitRule.Percentage) rule.basisPoints.mapValues { (_, basisPoints) -> PercentFormat.formatInput(basisPoints) } else emptyMap(),
            participantIds = if (rule is SplitRule.Equal) rule.participants.toSet() else groupMemberIds,
            currency = currency,
            expenseId = expense.id,
            group = group,
            spentAt = expense.spentAt,
            paidBy = expense.paidBy,
            splitMode = when (rule) {
                is SplitRule.Equal -> SplitMode.EQUAL
                is SplitRule.Exact -> SplitMode.EXACT
                is SplitRule.Percentage -> SplitMode.PERCENTAGE
            },
        )
    }

    private fun ExpenseEditorUiState.newDraft(group: Group?): ExpenseEditorUiState {
        val memberIds = group?.members.orEmpty().map { it.id }
        return copy(isLoading = false, participantIds = memberIds.toSet(), currency = group?.currency, group = group, spentAt = Clock.System.now(), paidBy = memberIds.firstOrNull())
    }

    private fun changeCurrency(currency: Currency) {
        if (currency == mutableState.value.expenseCurrency) return
        mutableState.update { state -> state.copy(rateText = "", currency = currency) }
        val groupCurrency = mutableState.value.group?.currency ?: return
        if (currency == groupCurrency) return
        viewModelScope.launch {
            val rate = exchangeRateRepository.lastRate(from = currency, to = groupCurrency) ?: return@launch
            mutableState.update { state -> if (state.currency == currency && state.rateText.isBlank()) state.copy(rateText = RateFormat.formatInput(rate.micros)) else state }
        }
    }

    private fun delete() {
        val expenseId = mutableState.value.expenseId ?: return
        viewModelScope.launch {
            val isDeleted = succeeds { expenseRepository.deleteExpense(expenseId) }
            mutableState.update { state -> if (isDeleted) state.copy(isFinished = true) else state.copy(isDeleteFailed = true) }
        }
    }

    private fun save() {
        val draft = mutableState.value
        val amount = draft.amount
        val paidBy = draft.paidBy
        val split = draft.splitRule
        val exchangeRate = draft.exchangeRate
        if (!draft.canSave || amount == null || paidBy == null || split == null) return
        mutableState.update { state -> state.copy(isSaving = true) }
        viewModelScope.launch {
            val isStored = succeeds { expenseRepository.saveExpense(amount = amount, exchangeRate = exchangeRate, groupId = groupId, id = draft.expenseId, paidBy = paidBy, spentAt = draft.spentAt, split = split, title = draft.title) }
            if (isStored && exchangeRate != null) {
                succeeds { exchangeRateRepository.rememberRate(exchangeRate) }
            }
            mutableState.update { state -> if (isStored) state.copy(isFinished = true, isSaving = false) else state.copy(isSaveFailed = true, isSaving = false) }
        }
    }

    private fun Set<MemberId>.toggled(memberId: MemberId): Set<MemberId> = if (memberId in this) this - memberId else this + memberId
}
