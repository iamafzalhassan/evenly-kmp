package org.example.evenly.core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.example.evenly.core.database.GroupDao
import org.example.evenly.core.database.GroupEntity
import org.example.evenly.core.database.MemberEntity
import org.example.evenly.core.model.Currency
import org.example.evenly.core.model.Group
import org.example.evenly.core.model.GroupId
import org.example.evenly.core.model.MemberId
import kotlin.time.Clock
import kotlin.uuid.Uuid

class GroupRepository internal constructor(private val onLocalChange: () -> Unit, private val groupDao: GroupDao) {
    suspend fun addMember(groupId: GroupId, name: String) {
        groupDao.appendMember(groupId = groupId.raw, id = Uuid.random().toString(), modifiedAtEpochMillis = nowMillis(), name = name.trim())
        onLocalChange()
    }

    suspend fun createGroup(name: String, memberNames: List<String>, currency: Currency): GroupId {
        val groupId = GroupId(Uuid.random().toString())
        val now = nowMillis()
        val group = GroupEntity(isDeleted = false, isDirty = true, createdAtEpochMillis = now, modifiedAtEpochMillis = now, currencyCode = currency.name, id = groupId.raw, name = name.trim(), inviteCode = null)
        val members = memberNames.mapIndexed { index, memberName ->
            MemberEntity(isDeleted = false, isDirty = true, position = index, modifiedAtEpochMillis = now, groupId = groupId.raw, id = Uuid.random().toString(), name = memberName.trim())
        }
        groupDao.insertGroupWithMembers(group = group, members = members)
        onLocalChange()
        return groupId
    }

    suspend fun deleteGroup(id: GroupId) {
        groupDao.markGroupDeleted(id = id.raw, modifiedAtEpochMillis = nowMillis())
        onLocalChange()
    }

    fun observeGroup(id: GroupId): Flow<Group?> = groupDao.observeGroup(id.raw).map { it?.toGroupOrNull() }.flowOn(Dispatchers.Default)

    fun observeGroups(): Flow<List<Group>> = groupDao.observeGroups().map { groups -> groups.mapNotNull { it.toGroupOrNull() } }.flowOn(Dispatchers.Default)

    suspend fun removeMember(id: MemberId): Boolean = groupDao.deleteMemberIfUnused(id = id.raw, modifiedAtEpochMillis = nowMillis()).also { isRemoved -> if (isRemoved) onLocalChange() }

    suspend fun renameGroup(id: GroupId, name: String) {
        require(name.isNotBlank()) { "A group name cannot be blank" }
        groupDao.renameGroup(id = id.raw, modifiedAtEpochMillis = nowMillis(), name = name.trim())
        onLocalChange()
    }

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
