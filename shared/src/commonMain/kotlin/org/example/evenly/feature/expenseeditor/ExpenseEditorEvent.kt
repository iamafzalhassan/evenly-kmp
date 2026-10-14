package org.example.evenly.feature.expenseeditor

import org.example.evenly.domain.group.MemberId
import org.example.evenly.domain.money.Currency

sealed interface ExpenseEditorEvent {
    data class ChangeAmount(val text: String) : ExpenseEditorEvent

    data class ChangeCurrency(val currency: Currency) : ExpenseEditorEvent

    data class ChangeDate(val utcDateMillis: Long) : ExpenseEditorEvent

    data class ChangeExactAmount(val text: String, val memberId: MemberId) : ExpenseEditorEvent

    data class ChangePaidBy(val memberId: MemberId) : ExpenseEditorEvent

    data class ChangePercentage(val text: String, val memberId: MemberId) : ExpenseEditorEvent

    data class ChangeRate(val text: String) : ExpenseEditorEvent

    data class ChangeSplitMode(val mode: SplitMode) : ExpenseEditorEvent

    data class ChangeTitle(val text: String) : ExpenseEditorEvent

    data object Delete : ExpenseEditorEvent

    data object DismissDeleteFailure : ExpenseEditorEvent

    data object DismissSaveFailure : ExpenseEditorEvent

    data object Save : ExpenseEditorEvent

    data class ToggleParticipant(val memberId: MemberId) : ExpenseEditorEvent
}
