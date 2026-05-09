package de.consansri.unterhaltsrechner.types

data class Grundbedarf(
    val wohnpauschale: ZBetrag,
    val sonstiges: ZBetrag
) {
    val sum: ZBetrag get() = wohnpauschale + sonstiges
}
