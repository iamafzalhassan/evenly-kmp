package org.example.evenly.data.sources

import androidx.room.Entity

@Entity(primaryKeys = ["fromCurrencyCode", "toCurrencyCode"], tableName = "exchange_rates")
data class ExchangeRateEntity(val rateMicros: Long, val updatedAtEpochMillis: Long, val fromCurrencyCode: String, val toCurrencyCode: String)
