package org.example.evenly.util

object PercentFormat {
    const val FRACTION_DIGITS: Int = 2
    private const val MAX_WHOLE_DIGITS: Int = 3

    fun format(basisPoints: Int): String = "${formatInput(basisPoints)}%"

    fun formatInput(basisPoints: Int): String = DecimalInput.format(fractionDigits = FRACTION_DIGITS, scaled = basisPoints.toLong()).trimEnd('0').trimEnd('.')

    fun parseBasisPoints(text: String): Int? = DecimalInput.parse(fractionDigits = FRACTION_DIGITS, maxWholeDigits = MAX_WHOLE_DIGITS, text = text.removeSuffix("%"))?.toInt()
}
