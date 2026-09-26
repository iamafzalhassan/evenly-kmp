package org.example.evenly.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExchangeRateTest {
    @Test
    fun convertsBetweenCurrenciesWithTheSameDecimalPlaces() {
        val rate = ExchangeRate(micros = 330_123_456L, from = Currency.EUR, to = Currency.LKR)

        assertEquals(Money(minorUnits = 407_372L, currency = Currency.LKR), rate.convert(Money(minorUnits = 1_234L, currency = Currency.EUR)))
    }

    @Test
    fun convertsIntoACurrencyWithFewerDecimalPlacesRoundingHalfUp() {
        val rate = ExchangeRate(micros = 150_500_000L, from = Currency.USD, to = Currency.JPY)

        assertEquals(Money(minorUnits = 1_859L, currency = Currency.JPY), rate.convert(Money(minorUnits = 1_235L, currency = Currency.USD)))
    }

    @Test
    fun convertsIntoACurrencyWithMoreDecimalPlacesRoundingHalfUp() {
        val rate = ExchangeRate(micros = 6_789L, from = Currency.JPY, to = Currency.USD)

        assertEquals(Money(minorUnits = 679L, currency = Currency.USD), rate.convert(Money(minorUnits = 1_000L, currency = Currency.JPY)))
    }

    @Test
    fun rejectsTheWrongCurrencyAndNegativeAmounts() {
        val rate = ExchangeRate(micros = 330_000_000L, from = Currency.EUR, to = Currency.LKR)

        assertFailsWith<IllegalArgumentException> { rate.convert(Money(minorUnits = 100L, currency = Currency.USD)) }
        assertFailsWith<IllegalArgumentException> { rate.convert(Money(minorUnits = -100L, currency = Currency.EUR)) }
    }
}
