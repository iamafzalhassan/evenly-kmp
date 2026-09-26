package org.example.evenly

import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.evenly.data.ExchangeRateRepository
import org.example.evenly.data.ExpenseRepository
import org.example.evenly.data.GroupRepository
import org.example.evenly.data.SettingsRepository
import org.example.evenly.data.SettlementRepository
import org.example.evenly.data.sources.EvenlyDatabase
import org.example.evenly.data.sources.buildEvenlyDatabase
import org.example.evenly.data.sync.SupabaseRemote
import org.example.evenly.data.sync.SyncCoordinator
import org.example.evenly.data.sync.SyncEngine
import org.example.evenly.data.sync.createEvenlySupabaseClient

class AppGraph internal constructor(databaseBuilder: RoomDatabase.Builder<EvenlyDatabase>) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: EvenlyDatabase = databaseBuilder.buildEvenlyDatabase()

    val exchangeRateRepository: ExchangeRateRepository = ExchangeRateRepository(database.exchangeRateDao())

    val expenseRepository: ExpenseRepository = ExpenseRepository(expenseDao = database.expenseDao(), onLocalChange = { syncCoordinator.requestSync() })

    val groupRepository: GroupRepository = GroupRepository(groupDao = database.groupDao(), onLocalChange = { syncCoordinator.requestSync() })

    val settingsRepository: SettingsRepository = SettingsRepository(database.settingsDao())

    val settlementRepository: SettlementRepository = SettlementRepository(onLocalChange = { syncCoordinator.requestSync() }, settlementDao = database.settlementDao())

    val syncCoordinator: SyncCoordinator = SyncCoordinator(engine = SyncEngine(remote = SupabaseRemote(createEvenlySupabaseClient()), settingsDao = database.settingsDao(), syncDao = database.syncDao()), scope = scope)
}
