package org.example.evenly.data.repository

import org.example.evenly.data.local.dao.ExchangeRateDao
import org.example.evenly.data.local.entity.ExchangeRateEntity
import org.example.evenly.domain.money.Currency
import org.example.evenly.domain.money.ExchangeRate
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
