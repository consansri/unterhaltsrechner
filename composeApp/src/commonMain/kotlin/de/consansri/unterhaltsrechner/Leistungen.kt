package de.consansri.unterhaltsrechner

import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Prozent

data class Leistungen(
    val unterhalt: Euro = Euro.NULL,
    val studiengebuehren: Euro = Euro.NULL,
    val krankenversicherung: Euro = Euro.NULL
) {
    val sum: Euro get() = unterhalt + studiengebuehren + krankenversicherung

    operator fun times(other: Prozent) = copy(
        unterhalt = unterhalt * other,
        studiengebuehren = studiengebuehren * other,
        krankenversicherung = krankenversicherung * other
    )
}
