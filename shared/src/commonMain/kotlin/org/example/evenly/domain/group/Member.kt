package org.example.evenly.domain.group

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@JvmInline
value class MemberId(val raw: String)

@Immutable
data class Member(val name: String, val id: MemberId) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 100
    }
}
