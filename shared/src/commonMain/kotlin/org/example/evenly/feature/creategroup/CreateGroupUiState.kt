package org.example.evenly.feature.creategroup

import androidx.compose.runtime.Immutable
import org.example.evenly.domain.group.Group
import org.example.evenly.domain.group.GroupId
import org.example.evenly.domain.money.Currency

@Immutable
data class CreateGroupUiState(
    val isSaveFailed: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val memberNames: List<String> = listOf(""),
    val currency: Currency = Currency.USD,
    val createdGroupId: GroupId? = null,
) {
    val canCreate: Boolean get() = !isSaving && name.isNotBlank() && trimmedMemberNames.size >= Group.MIN_MEMBERS && !hasDuplicateMembers
    val canRemoveMember: Boolean get() = memberNames.size > 1
    val hasDuplicateMembers: Boolean get() = trimmedMemberNames.map { it.lowercase() }.toSet().size != trimmedMemberNames.size

    val trimmedMemberNames: List<String> get() = memberNames.map { it.trim() }.filter { it.isNotEmpty() }
}
