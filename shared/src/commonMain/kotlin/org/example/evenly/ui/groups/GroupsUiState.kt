package org.example.evenly.ui.groups

import androidx.compose.runtime.Immutable
import org.example.evenly.model.Group
import org.example.evenly.model.GroupId

@Immutable
data class GroupsUiState(
    val isJoining: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val groups: List<Group> = emptyList(),
    val joinedGroupId: GroupId? = null,
    val feedback: GroupsFeedback? = null,
)
