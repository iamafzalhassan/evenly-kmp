package org.example.evenly.data.sources

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DATABASE_FILE_NAME: String = "evenly.db"

@Database(
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
    entities = [AppSettingEntity::class, ExchangeRateEntity::class, ExpenseEntity::class, ExpenseShareEntity::class, GroupEntity::class, MemberEntity::class, SettlementEntity::class],
    version = 3,
)
@ConstructedBy(EvenlyDatabaseConstructor::class)
abstract class EvenlyDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun groupDao(): GroupDao

    abstract fun settingsDao(): SettingsDao

    abstract fun settlementDao(): SettlementDao

    abstract fun syncDao(): SyncDao
}

internal fun RoomDatabase.Builder<EvenlyDatabase>.buildEvenlyDatabase(): EvenlyDatabase = setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

@Suppress("KotlinNoActualForExpect")
expect object EvenlyDatabaseConstructor : RoomDatabaseConstructor<EvenlyDatabase> {
    override fun initialize(): EvenlyDatabase
}
