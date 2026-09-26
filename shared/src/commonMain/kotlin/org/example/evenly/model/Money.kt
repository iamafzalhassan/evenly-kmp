package org.example.evenly.model

import androidx.compose.runtime.Immutable
import kotlin.math.abs

@Immutable
data class Money(val minorUnits: Long, val currency: Currency) {
    companion object {
        fun zero(currency: Currency): Money = Money(minorUnits = 0L, currency = currency)
    }

    val isNegative: Boolean get() = minorUnits < 0L
    val isPositive: Boolean get() = minorUnits > 0L

    val absolute: Money get() = copy(minorUnits = abs(minorUnits))

    private fun sameCurrency(other: Money): Money {
        require(other.currency == currency) { "Cannot combine ${other.currency} with $currency" }
        return other
    }

    operator fun minus(other: Money): Money = copy(minorUnits = minorUnits - sameCurrency(other).minorUnits)

    operator fun plus(other: Money): Money = copy(minorUnits = minorUnits + sameCurrency(other).minorUnits)

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)
}

fun Iterable<Money>.total(currency: Currency): Money = fold(Money.zero(currency)) { sum, money -> sum + money }
