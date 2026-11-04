package de.consansri.unterhaltsrechner.types

/**
 * Ein eingegebener Betrag zusammen mit seinem Zeitraum (z. B. 1.200 € pro Jahr).
 * Für die Berechnung wird er mit [proMonat] kaufmännisch gerundet auf den Monat umgerechnet.
 */
data class ZBetrag(
    val betrag: Euro = Euro.NULL,
    val zeitraum: Zeitraum = Zeitraum.MONAT,
) {
    val proMonat: Euro get() = betrag.geteiltDurch(zeitraum.monate)

    override fun toString(): String = "$betrag / ${zeitraum.bezeichnung}"

    companion object {
        val NULL = ZBetrag()
        fun monatlich(betrag: Euro) = ZBetrag(betrag, Zeitraum.MONAT)
    }
}
