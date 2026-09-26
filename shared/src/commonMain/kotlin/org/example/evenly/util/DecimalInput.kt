package org.example.evenly.util

object DecimalInput {
    const val DECIMAL_SEPARATOR: Char = '.'
    const val GROUPING_SEPARATOR: Char = ','

    private const val GROUP_SIZE: Int = 3

    private val SEPARATORS: Set<Char> = setOf(GROUPING_SEPARATOR, DECIMAL_SEPARATOR)

    fun format(scaled: Long, fractionDigits: Int): String {
        val scale = scaleFor(fractionDigits)
        val whole = scaled / scale
        val fraction = (scaled % scale).toString().padStart(fractionDigits, '0')
        return if (fractionDigits == 0) whole.toString() else "$whole$DECIMAL_SEPARATOR$fraction"
    }

    fun groupThousands(digits: String): String = digits.reversed().chunked(GROUP_SIZE).joinToString(GROUPING_SEPARATOR.toString()).reversed()

    fun parse(text: String, fractionDigits: Int, maxWholeDigits: Int): Long? {
        val cleaned = text.filterNot { it.isWhitespace() }
        if (cleaned.isEmpty() || !cleaned.all { it in '0'..'9' || it in SEPARATORS }) return null
        val lastSeparatorIndex = cleaned.indexOfLast { it in SEPARATORS }
        if (lastSeparatorIndex < 0) return combine(fraction = "", fractionDigits = fractionDigits, maxWholeDigits = maxWholeDigits, whole = cleaned)
        val head = cleaned.substring(0, lastSeparatorIndex)
        val tail = cleaned.substring(lastSeparatorIndex + 1)
        val decimalSeparator = cleaned[lastSeparatorIndex]
        return if (tail.length <= fractionDigits && decimalSeparator !in head) {
            val whole = groupedDigits(head) ?: return null
            combine(fraction = tail, fractionDigits = fractionDigits, maxWholeDigits = maxWholeDigits, whole = whole)
        } else {
            val whole = groupedDigits(cleaned) ?: return null
            combine(fraction = "", fractionDigits = fractionDigits, maxWholeDigits = maxWholeDigits, whole = whole)
        }
    }

    fun sanitize(text: String, previousText: String, fractionDigits: Int): String {
        val separators = text.filter { it in SEPARATORS }
        val decimalIndex = when {
            fractionDigits == 0 || separators.isEmpty() -> -1
            DECIMAL_SEPARATOR in previousText -> text.indexOfFirst { it in SEPARATORS }
            separators.length == 1 || separators.toSet().size > 1 -> text.indexOfLast { it in SEPARATORS }
            else -> -1
        }
        val whole = text.substring(0, if (decimalIndex < 0) text.length else decimalIndex).filter { it in '0'..'9' }
        if (decimalIndex < 0) return whole
        val fraction = text.substring(decimalIndex + 1).filter { it in '0'..'9' }.take(fractionDigits)
        return "$whole$DECIMAL_SEPARATOR$fraction"
    }

    fun scaleFor(fractionDigits: Int): Long = (1..fractionDigits).fold(1L) { scale, _ -> scale * 10L }

    private fun combine(fraction: String, fractionDigits: Int, maxWholeDigits: Int, whole: String): Long? {
        val digits = whole.trimStart('0')
        if ((whole.isEmpty() && fraction.isEmpty()) || digits.length > maxWholeDigits || fraction.length > fractionDigits) return null
        return digits.ifEmpty { "0" }.toLong() * scaleFor(fractionDigits) + fraction.padEnd(fractionDigits, '0').ifEmpty { "0" }.toLong()
    }

    private fun groupedDigits(text: String): String? {
        if (text.none { it in SEPARATORS }) return text
        val groups = text.split(*SEPARATORS.toCharArray())
        val separators = text.filter { it in SEPARATORS }.toSet()
        val isGrouped = separators.size == 1 && groups.first().length in 1..GROUP_SIZE && groups.drop(1).all { it.length == GROUP_SIZE }
        return if (isGrouped) groups.joinToString(separator = "") else null
    }
}
