package org.example.evenly.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.evenly.data.GroupRepository
import org.example.evenly.data.sync.JoinResult
import org.example.evenly.data.sync.SyncCoordinator
import org.example.evenly.model.GroupId
import org.example.evenly.ui.STATE_STOP_TIMEOUT_MILLIS

class GroupsViewModel(groupRepository: GroupRepository, private val syncCoordinator: SyncCoordinator) : ViewModel() {
    private val screenState: MutableStateFlow<GroupsUiState> = MutableStateFlow(GroupsUiState())

    val state: StateFlow<GroupsUiState> = combine(groupRepository.observeGroups(), screenState) { groups, screen -> screen.copy(isLoading = false, groups = groups) }
        .stateIn(initialValue = GroupsUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: GroupsEvent) {
        when (event) {
            GroupsEvent.ConsumeJoinedGroup -> screenState.update { state -> state.copy(joinedGroupId = null) }
            GroupsEvent.DismissFeedback -> screenState.update { state -> state.copy(feedback = null) }
            is GroupsEvent.JoinGroup -> joinGroup(event.code)
            GroupsEvent.Refresh -> refresh()
        }
    }

    private fun joinGroup(code: String) {
        if (code.isBlank()) return
        screenState.update { state -> state.copy(isJoining = true) }
        viewModelScope.launch {
            val result = syncCoordinator.joinGroup(code)
            screenState.update { state ->
                when (result) {
                    JoinResult.Failed -> state.copy(isJoining = false, feedback = GroupsFeedback.JOIN_FAILED)
                    is JoinResult.Joined -> state.copy(isJoining = false, joinedGroupId = GroupId(result.groupId))
                    JoinResult.NotFound -> state.copy(isJoining = false, feedback = GroupsFeedback.INVITE_NOT_FOUND)
                    JoinResult.Offline -> state.copy(isJoining = false, feedback = GroupsFeedback.JOIN_OFFLINE)
                    JoinResult.RateLimited -> state.copy(isJoining = false, feedback = GroupsFeedback.JOIN_RATE_LIMITED)
                }
            }
        }
    }

    private fun refresh() {
        screenState.update { state -> state.copy(isRefreshing = true) }
        viewModelScope.launch {
            val isSynced = syncCoordinator.syncNow()
            screenState.update { state -> state.copy(isRefreshing = false, feedback = if (isSynced) state.feedback else GroupsFeedback.SYNC_FAILED) }
        }
    }
}
