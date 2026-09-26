package org.example.evenly.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.evenly.data.ExpenseRepository
import org.example.evenly.data.GroupRepository
import org.example.evenly.data.SettlementRepository
import org.example.evenly.data.sync.SyncCoordinator
import org.example.evenly.domain.GroupLedger
import org.example.evenly.model.GroupId
import org.example.evenly.model.MemberId
import org.example.evenly.ui.STATE_STOP_TIMEOUT_MILLIS
import org.example.evenly.util.succeeds

class GroupViewModel(
    expenseRepository: ExpenseRepository,
    private val groupId: GroupId,
    private val groupRepository: GroupRepository,
    private val settlementRepository: SettlementRepository,
    private val syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val ledger: Flow<GroupLedger?> = combine(groupRepository.observeGroup(groupId), expenseRepository.observeExpenses(groupId), settlementRepository.observeSettlements(groupId)) { group, expenses, settlements ->
        group?.let { GroupLedger.build(expenses = expenses, group = it, settlements = settlements) }
    }.flowOn(Dispatchers.Default)

    private val screenState: MutableStateFlow<GroupUiState> = MutableStateFlow(GroupUiState())

    val state: StateFlow<GroupUiState> = combine(ledger, screenState) { currentLedger, screen ->
        screen.copy(
            isLoading = false,
            balances = currentLedger?.balances.orEmpty(),
            expenses = currentLedger?.expenses.orEmpty(),
            settlements = currentLedger?.settlements.orEmpty(),
            transfers = currentLedger?.transfers.orEmpty(),
            group = currentLedger?.group,
            totalPaidBack = currentLedger?.totalPaidBack,
            totalSpent = currentLedger?.totalSpent,
        )
    }.stateIn(initialValue = GroupUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.AddMember -> addMember(event.name)
            is GroupEvent.ChangeExpenseQuery -> screenState.update { current -> current.copy(expenseQuery = event.text) }
            GroupEvent.DeleteGroup -> deleteGroup()
            is GroupEvent.DeleteSettlement -> runAction(success = GroupFeedback.PAYMENT_DELETED) { settlementRepository.deleteSettlement(event.settlementId) }
            GroupEvent.DismissFeedback -> setFeedback(null)
            is GroupEvent.RecordTransfer -> runAction(success = GroupFeedback.PAYMENT_RECORDED) { settlementRepository.recordTransfer(groupId = groupId, transfer = event.transfer) }
            GroupEvent.Refresh -> refresh()
            is GroupEvent.RemoveMember -> removeMember(event.memberId)
            is GroupEvent.RenameGroup -> renameGroup(event.name)
        }
    }

    private fun addMember(name: String) {
        if (!state.value.isMemberNameAvailable(name)) return
        runAction(success = GroupFeedback.MEMBER_ADDED) { groupRepository.addMember(groupId = groupId, name = name) }
    }

    private fun deleteGroup() {
        screenState.update { current -> current.copy(isDeleting = true) }
        viewModelScope.launch {
            val isDeleted = succeeds { groupRepository.deleteGroup(groupId) }
            screenState.update { current -> current.copy(isDeleting = isDeleted, feedback = if (isDeleted) GroupFeedback.GROUP_DELETED else GroupFeedback.ACTION_FAILED) }
        }
    }

    private fun refresh() {
        screenState.update { current -> current.copy(isRefreshing = true) }
        viewModelScope.launch {
            val isSynced = syncCoordinator.syncNow()
            screenState.update { current -> current.copy(isRefreshing = false, feedback = if (isSynced) current.feedback else GroupFeedback.SYNC_FAILED) }
        }
    }

    private fun removeMember(memberId: MemberId) {
        if (state.value.memberRemoval(memberId) != MemberRemoval.ALLOWED) return
        runAction(success = GroupFeedback.MEMBER_REMOVED) { check(groupRepository.removeMember(memberId)) { "Member is still referenced" } }
    }

    private fun renameGroup(name: String) {
        if (name.isBlank()) return
        runAction(success = GroupFeedback.GROUP_RENAMED) { groupRepository.renameGroup(id = groupId, name = name) }
    }

    private fun runAction(success: GroupFeedback, write: suspend () -> Unit) {
        viewModelScope.launch {
            setFeedback(if (succeeds(write)) success else GroupFeedback.ACTION_FAILED)
        }
    }

    private fun setFeedback(feedback: GroupFeedback?) {
        screenState.update { current -> current.copy(feedback = feedback) }
    }
}
