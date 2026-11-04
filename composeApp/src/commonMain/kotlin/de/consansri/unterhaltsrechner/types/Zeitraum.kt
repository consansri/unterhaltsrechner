package de.consansri.unterhaltsrechner.types

/** Zeitraum, auf den sich ein eingegebener Betrag bezieht. Gerechnet wird immer monatlich. */
enum class Zeitraum(val monate: Int, val bezeichnung: String) {
    MONAT(1, "Monat"),
    SEMESTER(6, "Semester"),
    JAHR(12, "Jahr");

    override fun toString(): String = bezeichnung
}
