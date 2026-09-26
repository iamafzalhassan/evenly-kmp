package org.example.evenly.util

import org.example.evenly.model.Currency
import org.example.evenly.model.Money
import kotlin.math.abs

object MoneyFormat {
    const val MAX_WHOLE_DIGITS: Int = 10

    fun format(money: Money): String {
        val digits = formatInput(currency = money.currency, minorUnits = abs(money.minorUnits))
        val whole = digits.substringBefore(DecimalInput.DECIMAL_SEPARATOR)
        val sign = if (money.isNegative) "-" else ""
        return "$sign${money.currency.name} ${DecimalInput.groupThousands(whole)}${digits.removePrefix(whole)}"
    }

    fun formatInput(minorUnits: Long, currency: Currency): String = DecimalInput.format(fractionDigits = currency.minorDigits, scaled = minorUnits)

    fun maxMinorUnits(currency: Currency): Long = DecimalInput.scaleFor(MAX_WHOLE_DIGITS) * currency.minorScale - 1L

    fun parseMinorUnits(text: String, currency: Currency): Long? = DecimalInput.parse(fractionDigits = currency.minorDigits, maxWholeDigits = MAX_WHOLE_DIGITS, text = text)
}
