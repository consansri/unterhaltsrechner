package de.consansri.unterhaltsrechner

import de.consansri.unterhaltsrechner.types.Prozent
import de.consansri.unterhaltsrechner.types.ZBetrag

data class Leistungen(
    val unterhalt: ZBetrag,
    val studiengebuehren: ZBetrag,
    val krankenversicherung: ZBetrag
) {
    val sum: ZBetrag get() = unterhalt + studiengebuehren + krankenversicherung

    operator fun times(other: Prozent) = copy(
        unterhalt = unterhalt * other,
        studiengebuehren = studiengebuehren * other,
        krankenversicherung = krankenversicherung * other
    )
}
