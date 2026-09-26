package org.example.evenly.data.sources

import androidx.room.Embedded
import androidx.room.Relation

data class GroupWithMembers(@Relation(entityColumn = "groupId", parentColumn = "id") val members: List<MemberEntity>, @Embedded val group: GroupEntity)
