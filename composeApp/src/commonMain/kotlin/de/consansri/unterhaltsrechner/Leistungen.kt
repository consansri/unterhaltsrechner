package de.consansri.unterhaltsrechner

import de.consansri.unterhaltsrechner.types.Prozent
import de.consansri.unterhaltsrechner.types.ZeitraumBetrag

data class Leistungen(
    val unterhalt: ZeitraumBetrag,
    val studiengebuehren: ZeitraumBetrag,
    val krankenversicherung: ZeitraumBetrag
) {
    val sum: ZeitraumBetrag get() = unterhalt + studiengebuehren + krankenversicherung

    operator fun times(other: Prozent) = copy(
        unterhalt = unterhalt * other,
        studiengebuehren = studiengebuehren * other,
        krankenversicherung = krankenversicherung * other
    )
}
