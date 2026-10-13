package org.example.evenly.core.domain

import org.example.evenly.core.model.Expense
import org.example.evenly.core.model.MemberId
import org.example.evenly.core.model.Settlement
import org.example.evenly.core.model.memberIds

object MemberUsage {
    fun isReferenced(expenses: List<Expense>, settlements: List<Settlement>, memberId: MemberId): Boolean =
        expenses.any { expense -> expense.paidBy == memberId || memberId in expense.split.memberIds } || settlements.any { settlement -> settlement.from == memberId || settlement.to == memberId }
}
