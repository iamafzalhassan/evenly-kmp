package org.example.evenly.model

enum class Currency(val minorDigits: Int) {
    AED(minorDigits = 2),
    AUD(minorDigits = 2),
    CHF(minorDigits = 2),
    CZK(minorDigits = 2),
    DKK(minorDigits = 2),
    EUR(minorDigits = 2),
    GBP(minorDigits = 2),
    INR(minorDigits = 2),
    JPY(minorDigits = 0),
    LKR(minorDigits = 2),
    NOK(minorDigits = 2),
    PLN(minorDigits = 2),
    SEK(minorDigits = 2),
    USD(minorDigits = 2),
    ;

    companion object {
        fun fromCode(code: String): Currency? = entries.firstOrNull { it.name == code }
    }

    val minorScale: Long get() = (1..minorDigits).fold(1L) { scale, _ -> scale * 10L }
}
