package de.consansri.unterhaltsrechner

import kotlin.math.abs

data class TwoDigitDecimal(
    val amountInCents: Long
) {
    val whole: Long get() = amountInCents / 100
    val decimal: Long get() = abs(amountInCents) % 100

    override fun toString(): String = "$whole,${decimal.toString().padStart(2, '0')}"

    operator fun plus(other: TwoDigitDecimal) = TwoDigitDecimal(amountInCents + other.amountInCents)
    operator fun minus(other: TwoDigitDecimal) = TwoDigitDecimal(amountInCents - other.amountInCents)
    operator fun times(factor: TwoDigitDecimal) = TwoDigitDecimal((amountInCents * factor.amountInCents))
    operator fun div(factor: TwoDigitDecimal) = TwoDigitDecimal((amountInCents / factor.amountInCents))
    operator fun unaryMinus() = TwoDigitDecimal(-amountInCents)
}
