package org.example.evenly.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupDao {
    @Query(
        "SELECT (SELECT COUNT(*) FROM expenses WHERE paidByMemberId = :memberId AND isDeleted = 0) + " +
            "(SELECT COUNT(*) FROM expense_shares s INNER JOIN expenses e ON e.id = s.expenseId WHERE s.memberId = :memberId AND e.isDeleted = 0) + " +
            "(SELECT COUNT(*) FROM settlements WHERE (fromMemberId = :memberId OR toMemberId = :memberId) AND isDeleted = 0)",
    )
    abstract suspend fun countMemberReferences(memberId: String): Int

    @Insert
    abstract suspend fun insertGroup(group: GroupEntity)

    @Insert
    abstract suspend fun insertMember(member: MemberEntity)

    @Insert
    abstract suspend fun insertMembers(members: List<MemberEntity>)

    @Query("UPDATE expense_groups SET isDeleted = 1, isDirty = 1, modifiedAtEpochMillis = :modifiedAtEpochMillis WHERE id = :id")
    abstract suspend fun markGroupDeleted(modifiedAtEpochMillis: Long, id: String)

    @Query("UPDATE members SET isDeleted = 1, isDirty = 1, modifiedAtEpochMillis = :modifiedAtEpochMillis WHERE id = :id")
    abstract suspend fun markMemberDeleted(modifiedAtEpochMillis: Long, id: String)

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM members WHERE groupId = :groupId")
    abstract suspend fun nextMemberPosition(groupId: String): Int

    @Transaction
    @Query("SELECT * FROM expense_groups WHERE id = :id AND isDeleted = 0")
    abstract fun observeGroup(id: String): Flow<GroupWithMembers?>

    @Transaction
    @Query("SELECT * FROM expense_groups WHERE isDeleted = 0 ORDER BY createdAtEpochMillis DESC")
    abstract fun observeGroups(): Flow<List<GroupWithMembers>>

    @Query("UPDATE expense_groups SET name = :name, isDirty = 1, modifiedAtEpochMillis = :modifiedAtEpochMillis WHERE id = :id")
    abstract suspend fun renameGroup(modifiedAtEpochMillis: Long, id: String, name: String)

    @Transaction
    open suspend fun appendMember(modifiedAtEpochMillis: Long, groupId: String, id: String, name: String) {
        insertMember(MemberEntity(isDeleted = false, isDirty = true, position = nextMemberPosition(groupId), modifiedAtEpochMillis = modifiedAtEpochMillis, groupId = groupId, id = id, name = name))
    }

    @Transaction
    open suspend fun deleteMemberIfUnused(modifiedAtEpochMillis: Long, id: String): Boolean {
        if (countMemberReferences(id) > 0) return false
        markMemberDeleted(modifiedAtEpochMillis = modifiedAtEpochMillis, id = id)
        return true
    }

    @Transaction
    open suspend fun insertGroupWithMembers(group: GroupEntity, members: List<MemberEntity>) {
        insertGroup(group)
        insertMembers(members)
    }
}
