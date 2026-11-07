package de.consansri.unterhaltsrechner.money

import kotlin.jvm.JvmInline

/**
 * Euro amount with exactly two decimal places.
 *
 * All arithmetic runs on cents, never on floating point, so results carry no rounding error.
 * Divisions round half away from zero (kaufmännische Rundung).
 */
@JvmInline
internal value class Euro(val cents: Long) : Comparable<Euro> {

    operator fun plus(other: Euro) = Euro(cents + other.cents)
    operator fun minus(other: Euro) = Euro(cents - other.cents)

    override fun compareTo(other: Euro): Int = cents.compareTo(other.cents)

    /** Divides by [divisor], e.g. 1.000 € / 12 = 83,33 €. */
    fun dividedBy(divisor: Int): Euro = Euro(roundedDivision(cents, divisor.toLong()))

    /** Computes `this * numerator / denominator` with a single rounding step. */
    fun proportion(numerator: Euro, denominator: Euro): Euro {
        require(denominator.cents != 0L) { "denominator must not be zero" }
        return Euro(roundedDivision(cents * numerator.cents, denominator.cents))
    }

    fun atLeast(minimum: Euro) = Euro(cents.coerceAtLeast(minimum.cents))
    fun atMost(maximum: Euro) = Euro(cents.coerceAtMost(maximum.cents))

    /** Display format, e.g. `1.234,56 €`. */
    override fun toString(): String = "${format(thousandsSeparator = true)} €"

    /** Text-field format, e.g. `990` or `1234,50`. */
    fun toInputText(): String =
        if (cents % 100 == 0L) (cents / 100).toString() else format(thousandsSeparator = false)

    private fun format(thousandsSeparator: Boolean): String {
        val sign = if (cents < 0) "-" else ""
        val magnitude = if (cents < 0) -cents else cents
        val whole = (magnitude / 100).toString()
        val fraction = (magnitude % 100).toString().padStart(2, '0')
        val wholeFormatted = if (thousandsSeparator) whole.reversed().chunked(3).joinToString(".").reversed() else whole
        return "$sign$wholeFormatted,$fraction"
    }

    sealed interface ParseResult {
        data class Valid(val value: Euro) : ParseResult
        data class Invalid(val error: ParseError) : ParseResult
    }

    enum class ParseError { NEGATIVE, MALFORMED, TOO_MANY_DECIMALS, TOO_LARGE }

    companion object {

        val ZERO = Euro(0)

        /** Largest accepted number of whole-euro digits; keeps multiplications from overflowing. */
        private const val MAX_WHOLE_DIGITS = 7

        private val WITH_THOUSANDS = Regex("""^(\d{1,3}(?:\.\d{3})+)(?:,(\d*))?$""")
        private val PLAIN = Regex("""^(\d*)(?:[,.](\d*))?$""")

        /**
         * Parses user input such as `990`, `12,50`, `1.234,56`, `12.5` or `83,33 €`.
         *
         * - Empty input is 0 €.
         * - A dot is a thousands separator when followed by exactly three digits (`1.234`), otherwise a decimal separator (`12.5`).
         * - Negative amounts and more than two decimals are rejected.
         */
        fun parse(input: String): ParseResult {
            val text = input.replace(' ', ' ').trim().removeSuffix("€").trim()
            if (text.isEmpty()) return ParseResult.Valid(ZERO)
            if (text.startsWith("-")) return ParseResult.Invalid(ParseError.NEGATIVE)

            val (whole, decimals) = WITH_THOUSANDS.matchEntire(text)
                ?.let { it.groupValues[1].replace(".", "") to it.groupValues[2] }
                ?: PLAIN.matchEntire(text)?.let { it.groupValues[1] to it.groupValues[2] }
                ?: return ParseResult.Invalid(ParseError.MALFORMED)

            if (whole.isEmpty() && decimals.isEmpty()) return ParseResult.Invalid(ParseError.MALFORMED)
            if (decimals.length > 2) return ParseResult.Invalid(ParseError.TOO_MANY_DECIMALS)
            val wholeTrimmed = whole.trimStart('0')
            if (wholeTrimmed.length > MAX_WHOLE_DIGITS) return ParseResult.Invalid(ParseError.TOO_LARGE)

            val euros = wholeTrimmed.ifEmpty { "0" }.toLong()
            val cents = decimals.padEnd(2, '0').toLong()
            return ParseResult.Valid(Euro(euros * 100 + cents))
        }

        /** Integer division rounding half away from zero. */
        fun roundedDivision(numerator: Long, denominator: Long): Long {
            require(denominator != 0L) { "division by zero" }
            val negative = (numerator < 0) != (denominator < 0)
            val n = if (numerator < 0) -numerator else numerator
            val d = if (denominator < 0) -denominator else denominator
            val result = (n + d / 2) / d
            return if (negative) -result else result
        }
    }
}

/** Whole euros, e.g. `990.euro`. */
internal val Int.euro: Euro get() = Euro(this * 100L)
