package de.consansri.unterhaltsrechner.types

import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Repräsentiert einen Prozentwert mit genau 2 Nachkommastellen (z.B. 12,50 für 12,5%).
 * Intern wird der Wert in "Hundertstel Prozent" gespeichert (10000 = 100,00%).
 */
@JvmInline
value class Prozent(val inHundredths: Long) {

    constructor(value: Double): this((value * 10000).roundToLong())

    operator fun plus(other: Prozent) = Prozent(this.inHundredths + other.inHundredths)
    operator fun minus(other: Prozent) = Prozent(this.inHundredths - other.inHundredths)
    operator fun times(other: Euro) = other * this

    fun toDouble() = inHundredths.toDouble() / 10000

    override fun toString(): String {
        val whole = inHundredths / 100
        val fraction = abs(inHundredths) % 100
        return "$whole,${fraction.toString().padStart(2, '0')}"
    }

    companion object {

        fun parse(input: String): Prozent? {
            if (input.isBlank()) return null

            // Wir entfernen optional ein % Zeichen und ersetzen das Komma
            val normalizedInput = input.replace("%", "").replace(',', '.').trim()
            val doubleValue = normalizedInput.toDoubleOrNull() ?: return null

            return Prozent((doubleValue * 100).roundToLong())
        }
    }
}