package org.example.evenly.core.data

import org.example.evenly.core.database.ExchangeRateDao
import org.example.evenly.core.database.ExchangeRateEntity
import org.example.evenly.core.model.Currency
import org.example.evenly.core.model.ExchangeRate
import kotlin.time.Clock

class ExchangeRateRepository internal constructor(private val exchangeRateDao: ExchangeRateDao) {
    suspend fun lastRate(from: Currency, to: Currency): ExchangeRate? {
        val entity = exchangeRateDao.findRate(fromCurrencyCode = from.name, toCurrencyCode = to.name) ?: return null
        return ExchangeRate(micros = entity.rateMicros, from = from, to = to)
    }

    suspend fun rememberRate(rate: ExchangeRate) {
        val entity = ExchangeRateEntity(rateMicros = rate.micros, updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(), fromCurrencyCode = rate.from.name, toCurrencyCode = rate.to.name)
        exchangeRateDao.upsertRate(entity)
    }
}
