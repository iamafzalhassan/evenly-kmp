package org.example.evenly.ui.format

import org.example.evenly.domain.money.ExchangeRate

object RateFormat {
    const val FRACTION_DIGITS: Int = 6

    fun formatInput(micros: Long): String = DecimalInput.format(fractionDigits = FRACTION_DIGITS, scaled = micros).trimEnd('0').trimEnd('.')

    fun parseMicros(text: String): Long? = DecimalInput.parse(fractionDigits = FRACTION_DIGITS, maxWholeDigits = ExchangeRate.MAX_WHOLE_DIGITS, text = text)?.takeIf { it > 0L }
}
