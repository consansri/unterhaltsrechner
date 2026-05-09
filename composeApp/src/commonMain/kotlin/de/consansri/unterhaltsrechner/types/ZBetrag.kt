package de.consansri.unterhaltsrechner.types

/**
 * Kombiniert eine Euro-Eingabe mit dem gewählten Zeitraum und rechnet es sofort auf den Monat herunter.
 */
data class ZBetrag(
    val betrag: Euro = Euro.NULL,
    val zeitraum: Zeitraum = Zeitraum.MONAT
) {
    val proMonat: Euro get() = betrag / zeitraum.monate

    operator fun plus(other: ZBetrag) = ZBetrag(betrag = proMonat + other.proMonat, Zeitraum.MONAT)
    operator fun minus(other: ZBetrag) = ZBetrag(betrag = proMonat - other.proMonat, Zeitraum.MONAT)
    operator fun times(factor: Prozent) = ZBetrag(betrag = proMonat * factor, Zeitraum.MONAT)
    operator fun div(other: ZBetrag) = proMonat / other.proMonat

    fun min(other: ZBetrag) = ZBetrag(
        betrag = proMonat.min(other.proMonat),
        zeitraum = Zeitraum.MONAT
    )

    fun max(other: ZBetrag) = ZBetrag(
        betrag = proMonat.max(other.proMonat),
        zeitraum = Zeitraum.MONAT
    )

    override fun toString(): String = "$betrag ${zeitraum.suffix}"

    companion object {
        val NULL = ZBetrag(Euro.NULL, Zeitraum.MONAT)
    }

}