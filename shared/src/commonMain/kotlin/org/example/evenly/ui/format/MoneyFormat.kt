package org.example.evenly.ui.format

import org.example.evenly.domain.money.Currency
import org.example.evenly.domain.money.Money
import kotlin.math.abs

object MoneyFormat {
    fun format(money: Money): String {
        val digits = formatInput(currency = money.currency, minorUnits = abs(money.minorUnits))
        val whole = digits.substringBefore(DecimalInput.DECIMAL_SEPARATOR)
        val sign = if (money.isNegative) "-" else ""
        return "$sign${money.currency.name} ${DecimalInput.groupThousands(whole)}${digits.removePrefix(whole)}"
    }

    fun formatInput(minorUnits: Long, currency: Currency): String = DecimalInput.format(fractionDigits = currency.minorDigits, scaled = minorUnits)

    fun parseMinorUnits(text: String, currency: Currency): Long? = DecimalInput.parse(fractionDigits = currency.minorDigits, maxWholeDigits = Money.MAX_WHOLE_DIGITS, text = text)
}
