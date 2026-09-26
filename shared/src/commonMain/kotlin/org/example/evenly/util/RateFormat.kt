package org.example.evenly.util

object RateFormat {
    const val FRACTION_DIGITS: Int = 6
    const val MAX_WHOLE_DIGITS: Int = 6

    fun formatInput(micros: Long): String = DecimalInput.format(fractionDigits = FRACTION_DIGITS, scaled = micros).trimEnd('0').trimEnd('.')

    fun maxMicros(): Long = DecimalInput.scaleFor(FRACTION_DIGITS + MAX_WHOLE_DIGITS) - 1L

    fun parseMicros(text: String): Long? = DecimalInput.parse(fractionDigits = FRACTION_DIGITS, maxWholeDigits = MAX_WHOLE_DIGITS, text = text)?.takeIf { it > 0L }
}
