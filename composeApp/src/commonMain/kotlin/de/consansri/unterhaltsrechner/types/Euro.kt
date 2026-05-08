package de.consansri.unterhaltsrechner.types

import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Repräsentiert einen Euro-Betrag mit genau 2 Nachkommastellen.
 * Intern wird der Wert in Cent (als Long) gespeichert, um ungenaue
 * Kommazahlen-Rundungen zu vermeiden.
 */
@JvmInline
value class Euro(val inCents: Long) {

    constructor(value: Double) : this((value * 100).roundToLong())

    // Formatiert den Wert immer zu einem String mit zwei Nachkommastellen und einem Komma (z.B. "12,50")
    override fun toString(): String {
        // 1. Vorzeichen bestimmen: Wenn wir im Minus sind, brauchen wir ein "-"
        val sign = if (inCents < 0) "-" else ""

        // 2. Ab hier rechnen wir nur noch mit dem positiven Betrag, um Probleme bei der Division zu vermeiden
        val absoluteCents = abs(inCents)

        // 3. Volle Euros und restliche Cents berechnen
        val whole = absoluteCents / 100
        val fraction = absoluteCents % 100

        // 4. Alles zusammensetzen: Vorzeichen + volle Euros + Komma + Cents (mit führender Null) + €
        return "$sign$whole,${fraction.toString().padStart(2, '0')} €"
    }

    // Praktische Operatoren zum Rechnen mit Euro-Werten
    // Neue Operatoren für die Zeitraum-Berechnung
    operator fun div(months: Int): Euro = Euro(this.inCents / months)
    operator fun times(months: Int): Euro = Euro(this.inCents * months)
    operator fun plus(other: Euro) = Euro(this.inCents + other.inCents)
    operator fun minus(other: Euro) = Euro(this.inCents - other.inCents)
    operator fun times(other: Prozent) = Euro((this.inCents * other.toDouble()).roundToLong())
    operator fun div(other: Euro): Prozent? {
        val first = this.inCents.toDouble()
        val second = other.inCents.toDouble()
        if (second == 0.0) return null

        return Prozent(first / second)
    }

    companion object {

        val NULL = Euro(0)

        /**
         * Parst einen String (z.B. "12,50" oder "12.5") zurück in einen Euro-Typ.
         * Gibt null zurück, wenn die Eingabe ungültig ist.
         */
        fun parse(input: String): Euro? {
            if (input.isBlank()) return null

            // Wir wandeln das deutsche Komma in einen Punkt um, damit Kotlin es als Double lesen kann
            val normalizedInput = input.replace(',', '.').removeSuffix("€").trim()
            val doubleValue = normalizedInput.toDoubleOrNull() ?: return null

            // Wir multiplizieren mit 100 für die Cent und runden auf den nächsten Long-Wert
            return Euro((doubleValue * 100).roundToLong())
        }
    }
}