package de.consansri.unterhaltsrechner.types

/**
 * Kombiniert eine Euro-Eingabe mit dem gewählten Zeitraum und rechnet es sofort auf den Monat herunter.
 */
data class ZeitraumBetrag(
    val betrag: Euro = Euro.NULL,
    val zeitraum: Zeitraum = Zeitraum.MONAT
) {
    // Hier passiert die automatische Umrechnung:
    val proMonat: Euro get() = betrag / zeitraum.monate

    operator fun plus(other: ZeitraumBetrag) = ZeitraumBetrag(betrag = proMonat + other.proMonat, Zeitraum.MONAT)
    operator fun minus(other: ZeitraumBetrag) = ZeitraumBetrag(betrag = proMonat - other.proMonat, Zeitraum.MONAT)
    operator fun times(factor: Prozent) = ZeitraumBetrag(betrag = proMonat * factor, Zeitraum.MONAT)
    operator fun div(other: ZeitraumBetrag) = proMonat / other.proMonat

    override fun toString(): String = "$betrag ${zeitraum.suffix}"

}