package org.example.evenly.feature.group

import androidx.compose.runtime.Immutable
import org.example.evenly.core.domain.MemberUsage
import org.example.evenly.core.model.Balance
import org.example.evenly.core.model.Expense
import org.example.evenly.core.model.Group
import org.example.evenly.core.model.MemberId
import org.example.evenly.core.model.Money
import org.example.evenly.core.model.Settlement
import org.example.evenly.core.model.Transfer

@Immutable
data class GroupUiState(
    val isDeleting: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val expenseQuery: String = "",
    val balances: List<Balance> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val transfers: List<Transfer> = emptyList(),
    val group: Group? = null,
    val feedback: GroupFeedback? = null,
    val totalPaidBack: Money? = null,
    val totalSpent: Money? = null,
) {
    val visibleExpenses: List<Expense> by lazy {
        val query = expenseQuery.trim()
        if (query.isEmpty()) {
            expenses
        } else {
            expenses.filter { expense -> expense.title.contains(query, ignoreCase = true) || memberNames[expense.paidBy].orEmpty().contains(query, ignoreCase = true) }
        }
    }

    val memberNames: Map<MemberId, String> by lazy { group?.members.orEmpty().associate { it.id to it.name } }

    val hasActivity: Boolean get() = expenses.isNotEmpty() || settlements.isNotEmpty()
    val isGroupMissing: Boolean get() = !isLoading && !isDeleting && group == null

    fun isMemberNameAvailable(name: String): Boolean {
        val trimmed = name.trim()
        val members = group?.members ?: return false
        return trimmed.isNotEmpty() && members.none { it.name.equals(trimmed, ignoreCase = true) }
    }

    fun memberRemoval(memberId: MemberId): MemberRemoval = when {
        MemberUsage.isReferenced(expenses = expenses, memberId = memberId, settlements = settlements) -> MemberRemoval.IN_USE
        group?.members.orEmpty().size <= Group.MIN_MEMBERS -> MemberRemoval.TOO_FEW_MEMBERS
        else -> MemberRemoval.ALLOWED
    }
}
