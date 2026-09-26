package org.example.evenly.ui.group

import org.example.evenly.model.MemberId
import org.example.evenly.model.SettlementId
import org.example.evenly.model.Transfer

sealed interface GroupEvent {
    data class AddMember(val name: String) : GroupEvent

    data class ChangeExpenseQuery(val text: String) : GroupEvent

    data object DeleteGroup : GroupEvent

    data class DeleteSettlement(val settlementId: SettlementId) : GroupEvent

    data object DismissFeedback : GroupEvent

    data class RecordTransfer(val transfer: Transfer) : GroupEvent

    data object Refresh : GroupEvent

    data class RemoveMember(val memberId: MemberId) : GroupEvent

    data class RenameGroup(val name: String) : GroupEvent
}
