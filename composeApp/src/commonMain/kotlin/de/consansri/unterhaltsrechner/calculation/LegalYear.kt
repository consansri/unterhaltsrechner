package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.euro
import kotlinx.datetime.LocalDate

/**
 * Legal parameters per calendar year (Düsseldorfer Tabelle, child benefit, minijob limit).
 *
 * Sources: Düsseldorfer Tabelle of the OLG Düsseldorf (as of 1 January), Bundeskindergeldgesetz, Minijob-Zentrale.
 *
 * To add a year, append an entry: [at] picks the newest entry valid on a given date.
 */
internal enum class LegalYear(
    val year: Int,
    /** Warm rent part of the need of a student with own household. */
    val needWarmRent: Euro,
    /** Remaining part of that need. */
    val needOther: Euro,
    /** Child benefit per child and month (§ 66 EStG). */
    val childBenefit: Euro,
    /** Appropriate self-reserve (angemessener Selbstbehalt) towards non-privileged adult children (§ 1603 Abs. 1 BGB). */
    val selfReserve: Euro,
    /** Warm rent contained in [selfReserve]; higher appropriate rent raises the self-reserve. */
    val selfReserveWarmRent: Euro,
    val minijobLimit: Euro,
    val status: Status = Status.Official,
) {
    Y2023(2023, 410.euro, 520.euro, 250.euro, 1650.euro, 650.euro, 520.euro),
    Y2024(2024, 410.euro, 520.euro, 250.euro, 1750.euro, 650.euro, 538.euro),
    Y2025(2025, 440.euro, 550.euro, 255.euro, 1750.euro, 650.euro, 556.euro),
    Y2026(2026, 440.euro, 550.euro, 259.euro, 1750.euro, 650.euro, 603.euro),
    Y2027(
        2027, 440.euro, 550.euro, 259.euro, 1750.euro, 650.euro, 633.euro,
        Status.Provisional("Düsseldorfer Tabelle 2027 steht aus: Werte von 2026. Kindergeld-Erhöhung auf 267 € geplant, nicht beschlossen."),
    ),
    ;

    val validFrom: LocalDate get() = LocalDate(year, 1, 1)

    /** Need of a student with own household, without health insurance and tuition fees. */
    val studentNeed: Euro get() = needWarmRent + needOther

    override fun toString(): String = year.toString()

    sealed interface Status {
        data object Official : Status

        /** Values are not yet official; [note] explains what applies instead. */
        data class Provisional(val note: String) : Status
    }

    companion object {
        /** The legal year valid on [date]; the oldest one for earlier dates. */
        fun at(date: LocalDate): LegalYear = entries.lastOrNull { it.validFrom <= date } ?: entries.first()
    }
}
