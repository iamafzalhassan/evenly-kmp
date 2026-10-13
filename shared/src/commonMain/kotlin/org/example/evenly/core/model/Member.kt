package org.example.evenly.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Member(val name: String, val id: MemberId) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 100
    }
}
