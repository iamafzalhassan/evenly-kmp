package org.example.evenly.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.evenly.data.sources.SettlementDao
import org.example.evenly.model.GroupId
import org.example.evenly.model.Settlement
import org.example.evenly.model.SettlementId
import org.example.evenly.model.Transfer
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SettlementRepository internal constructor(private val onLocalChange: () -> Unit, private val settlementDao: SettlementDao) {
    suspend fun deleteSettlement(id: SettlementId) {
        settlementDao.markSettlementDeleted(id = id.raw, modifiedAtEpochMillis = Clock.System.now().toEpochMilliseconds())
        onLocalChange()
    }

    fun observeSettlements(groupId: GroupId): Flow<List<Settlement>> = settlementDao.observeSettlements(groupId.raw).map { settlements -> settlements.mapNotNull { it.toSettlementOrNull() } }

    suspend fun recordTransfer(groupId: GroupId, transfer: Transfer) {
        require(transfer.amount.isPositive) { "A payment must be a positive amount" }
        val now = Clock.System.now()
        val settlement = Settlement(settledAt = now, from = transfer.from, to = transfer.to, amount = transfer.amount, id = SettlementId(Uuid.random().toString()))
        settlementDao.insertSettlement(settlement.toEntity(groupId = groupId.raw, modifiedAtEpochMillis = now.toEpochMilliseconds()))
        onLocalChange()
    }
}
