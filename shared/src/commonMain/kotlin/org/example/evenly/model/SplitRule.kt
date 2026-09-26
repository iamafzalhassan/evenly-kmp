package org.example.evenly.model

import androidx.compose.runtime.Immutable

sealed interface SplitRule {
    @Immutable
    data class Equal(val participants: List<MemberId>) : SplitRule

    @Immutable
    data class Exact(val minorUnits: Map<MemberId, Long>) : SplitRule

    @Immutable
    data class Percentage(val basisPoints: Map<MemberId, Int>) : SplitRule
}

val SplitRule.memberIds: List<MemberId>
    get() = when (this) {
        is SplitRule.Equal -> participants
        is SplitRule.Exact -> minorUnits.keys.toList()
        is SplitRule.Percentage -> basisPoints.keys.toList()
    }
