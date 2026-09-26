package org.example.evenly.util

import org.example.evenly.model.Currency
import org.example.evenly.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    @Test
    fun formatsACurrencyWithoutMinorUnits() {
        assertEquals("JPY 1,234,567", MoneyFormat.format(Money(minorUnits = 1_234_567L, currency = Currency.JPY)))
    }

    @Test
    fun groupsThousandsAndKeepsTheFraction() {
        assertEquals("USD 1,234,567.89", MoneyFormat.format(Money(minorUnits = 123_456_789L, currency = Currency.USD)))
    }

    @Test
    fun padsSmallFractionsAndSignsNegativeAmounts() {
        assertEquals("-EUR 0.05", MoneyFormat.format(Money(minorUnits = -5L, currency = Currency.EUR)))
    }

    @Test
    fun parsesGroupedInputBackToMinorUnits() {
        assertEquals(123_456_789L, MoneyFormat.parseMinorUnits(currency = Currency.USD, text = "1,234,567.89"))
    }
}
