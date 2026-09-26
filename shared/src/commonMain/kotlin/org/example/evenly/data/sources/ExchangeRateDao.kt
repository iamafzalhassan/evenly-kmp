package org.example.evenly.data.sources

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
abstract class ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE fromCurrencyCode = :fromCurrencyCode AND toCurrencyCode = :toCurrencyCode")
    abstract suspend fun findRate(fromCurrencyCode: String, toCurrencyCode: String): ExchangeRateEntity?

    @Upsert
    abstract suspend fun upsertRate(rate: ExchangeRateEntity)
}
