package org.example.evenly.domain.group

import org.example.evenly.domain.expense.Expense
import org.example.evenly.domain.expense.memberIds
import org.example.evenly.domain.settlement.Settlement

object MemberUsage {
    fun isReferenced(expenses: List<Expense>, settlements: List<Settlement>, memberId: MemberId): Boolean =
        expenses.any { expense -> expense.paidBy == memberId || memberId in expense.split.memberIds } || settlements.any { settlement -> settlement.from == memberId || settlement.to == memberId }
}
