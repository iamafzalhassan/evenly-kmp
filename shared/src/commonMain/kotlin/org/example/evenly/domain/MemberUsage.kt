package org.example.evenly.domain

import org.example.evenly.model.Expense
import org.example.evenly.model.MemberId
import org.example.evenly.model.Settlement
import org.example.evenly.model.memberIds

object MemberUsage {
    fun isReferenced(expenses: List<Expense>, settlements: List<Settlement>, memberId: MemberId): Boolean =
        expenses.any { expense -> expense.paidBy == memberId || memberId in expense.split.memberIds } || settlements.any { settlement -> settlement.from == memberId || settlement.to == memberId }
}
