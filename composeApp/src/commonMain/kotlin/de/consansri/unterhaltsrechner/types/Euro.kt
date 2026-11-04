package de.consansri.unterhaltsrechner.types

import kotlin.jvm.JvmInline

/**
 * Euro-Betrag mit exakt zwei Nachkommastellen.
 *
 * Intern wird in Cent (als [Long]) gerechnet – es gibt keine Gleitkommazahlen,
 * damit keine Rundungsfehler entstehen. Wo geteilt werden muss, wird
 * kaufmännisch (halb auf) gerundet.
 */
@JvmInline
value class Euro(val inCents: Long) : Comparable<Euro> {

    operator fun plus(other: Euro) = Euro(inCents + other.inCents)
    operator fun minus(other: Euro) = Euro(inCents - other.inCents)
    operator fun unaryMinus() = Euro(-inCents)
    operator fun times(faktor: Int) = Euro(inCents * faktor)

    override fun compareTo(other: Euro): Int = inCents.compareTo(other.inCents)

    /** Teilt kaufmännisch gerundet, z. B. 1.000 € / 12 = 83,33 €. */
    fun geteiltDurch(teiler: Int): Euro = Euro(rundeDivision(inCents, teiler.toLong()))

    /** Berechnet `this * zaehler / nenner` kaufmännisch gerundet, ohne Zwischenrundung. */
    fun anteil(zaehler: Euro, nenner: Euro): Euro {
        require(nenner.inCents != 0L) { "Nenner darf nicht 0 sein" }
        return Euro(rundeDivision(inCents * zaehler.inCents, nenner.inCents))
    }

    fun atLeast(minimum: Euro) = Euro(inCents.coerceAtLeast(minimum.inCents))
    fun atMost(maximum: Euro) = Euro(inCents.coerceAtMost(maximum.inCents))

    /** Anzeigeformat, z. B. `1.234,56 €`. */
    override fun toString(): String = "${formatiere(tausenderpunkte = true)} €"

    /** Format für Eingabefelder, z. B. `1234,5` → `1234,50`, `990` → `990`. */
    fun alsEingabe(): String =
        if (inCents % 100 == 0L) (inCents / 100).toString() else formatiere(tausenderpunkte = false)

    private fun formatiere(tausenderpunkte: Boolean): String {
        val vorzeichen = if (inCents < 0) "-" else ""
        val betrag = if (inCents < 0) -inCents else inCents
        val ganz = (betrag / 100).toString()
        val cent = (betrag % 100).toString().padStart(2, '0')
        val ganzFormatiert = if (tausenderpunkte) ganz.reversed().chunked(3).joinToString(".").reversed() else ganz
        return "$vorzeichen$ganzFormatiert,$cent"
    }

    /** Ergebnis der Prüfung einer Texteingabe. */
    sealed interface Eingabe {
        data class Gueltig(val wert: Euro) : Eingabe
        data class Ungueltig(val grund: String) : Eingabe
    }

    companion object {

        val NULL = Euro(0)

        /** Größter erlaubter ganzer Euro-Anteil (verhindert Überläufe bei Multiplikationen). */
        private const val MAX_STELLEN = 7

        private val MIT_TAUSENDERPUNKTEN = Regex("""^(\d{1,3}(?:\.\d{3})+)(?:,(\d*))?$""")
        private val EINFACH = Regex("""^(\d*)(?:[,.](\d*))?$""")

        /**
         * Prüft eine Eingabe wie `990`, `12,50`, `1.234,56`, `12.5` oder `83,33 €`.
         *
         * - Eine leere Eingabe gilt als 0 €.
         * - Punkte sind Tausendertrennzeichen, wenn sie in Dreiergruppen stehen (`1.234`),
         *   sonst Dezimaltrennzeichen (`12.5`).
         * - Negative Beträge und mehr als zwei Nachkommastellen werden abgelehnt.
         */
        fun pruefe(eingabe: String): Eingabe {
            val text = eingabe.replace(' ', ' ').trim().removeSuffix("€").trim()
            if (text.isEmpty()) return Eingabe.Gueltig(NULL)
            if (text.startsWith("-")) return Eingabe.Ungueltig("Negative Beträge sind nicht möglich.")

            val (ganz, nachkomma) = MIT_TAUSENDERPUNKTEN.matchEntire(text)
                ?.let { it.groupValues[1].replace(".", "") to it.groupValues[2] }
                ?: EINFACH.matchEntire(text)?.let { it.groupValues[1] to it.groupValues[2] }
                ?: return Eingabe.Ungueltig("Bitte einen Betrag wie 1.234,56 eingeben.")

            if (ganz.isEmpty() && nachkomma.isEmpty()) {
                return Eingabe.Ungueltig("Bitte einen Betrag wie 1.234,56 eingeben.")
            }
            if (nachkomma.length > 2) return Eingabe.Ungueltig("Höchstens zwei Nachkommastellen.")
            val ganzOhneNullen = ganz.trimStart('0')
            if (ganzOhneNullen.length > MAX_STELLEN) return Eingabe.Ungueltig("Betrag ist zu groß.")

            val euro = ganzOhneNullen.ifEmpty { "0" }.toLong()
            val cent = nachkomma.padEnd(2, '0').toLong()
            return Eingabe.Gueltig(Euro(euro * 100 + cent))
        }

        /** Wie [pruefe], liefert aber `null` bei ungültiger Eingabe. */
        fun parse(eingabe: String): Euro? = (pruefe(eingabe) as? Eingabe.Gueltig)?.wert

        /**
         * Ganzzahlige Division mit kaufmännischer Rundung (halb vom Nullpunkt weg).
         */
        internal fun rundeDivision(zaehler: Long, nenner: Long): Long {
            require(nenner != 0L) { "Division durch 0" }
            val negativ = (zaehler < 0) != (nenner < 0)
            val z = if (zaehler < 0) -zaehler else zaehler
            val n = if (nenner < 0) -nenner else nenner
            val ergebnis = (z + n / 2) / n
            return if (negativ) -ergebnis else ergebnis
        }
    }
}

/** Ganze Euro, z. B. `990.euro`. */
val Int.euro: Euro get() = Euro(this * 100L)
