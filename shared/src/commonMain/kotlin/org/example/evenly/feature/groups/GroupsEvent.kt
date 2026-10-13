package org.example.evenly.feature.groups

sealed interface GroupsEvent {
    data object ConsumeJoinedGroup : GroupsEvent

    data object DismissFeedback : GroupsEvent

    data class JoinGroup(val code: String) : GroupsEvent

    data object Refresh : GroupsEvent
}
