package org.example.evenly.feature.creategroup

import org.example.evenly.core.model.Currency

sealed interface CreateGroupEvent {
    data object AddMember : CreateGroupEvent

    data class ChangeCurrency(val currency: Currency) : CreateGroupEvent

    data class ChangeMemberName(val index: Int, val name: String) : CreateGroupEvent

    data class ChangeName(val name: String) : CreateGroupEvent

    data object Create : CreateGroupEvent

    data object DismissSaveFailure : CreateGroupEvent

    data class RemoveMember(val index: Int) : CreateGroupEvent
}
