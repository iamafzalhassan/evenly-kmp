package org.example.evenly.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SettlementDao {
    @Insert
    abstract suspend fun insertSettlement(settlement: SettlementEntity)

    @Query("UPDATE settlements SET isDeleted = 1, isDirty = 1, modifiedAtEpochMillis = :modifiedAtEpochMillis WHERE id = :id")
    abstract suspend fun markSettlementDeleted(modifiedAtEpochMillis: Long, id: String)

    @Query("SELECT * FROM settlements WHERE groupId = :groupId AND isDeleted = 0 ORDER BY settledAtEpochMillis DESC")
    abstract fun observeSettlements(groupId: String): Flow<List<SettlementEntity>>
}
