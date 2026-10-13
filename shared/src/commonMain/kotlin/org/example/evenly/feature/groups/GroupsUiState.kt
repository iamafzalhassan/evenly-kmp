package org.example.evenly.feature.groups

import androidx.compose.runtime.Immutable
import org.example.evenly.core.model.Group
import org.example.evenly.core.model.GroupId

@Immutable
data class GroupsUiState(
    val isJoining: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val groups: List<Group> = emptyList(),
    val joinedGroupId: GroupId? = null,
    val feedback: GroupsFeedback? = null,
)
