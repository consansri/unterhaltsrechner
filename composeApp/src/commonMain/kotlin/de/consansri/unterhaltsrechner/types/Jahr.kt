package de.consansri.unterhaltsrechner.types

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

enum class Rechtsstatus { AMTLICH, VORLAEUFIG }

/**
 * Rechtsstand je Kalenderjahr (Düsseldorfer Tabelle, Kindergeld, Minijob-Grenze).
 *
 * Quellen: Düsseldorfer Tabelle des OLG Düsseldorf (Stand jeweils 1. Januar),
 * Bundeskindergeldgesetz, Minijob-Zentrale.
 *
 * Neues Jahr: einen Eintrag am Ende ergänzen – [fuer] wählt automatisch den
 * neuesten Eintrag, der am jeweiligen Datum gilt.
 */
enum class Jahr(
    val jahr: Int,
    /** Bedarf eines studierenden Kindes mit eigenem Haushalt: davon Warmmiete … */
    val bedarfWarmmiete: Euro,
    /** … und sonstiger Bedarf. */
    val bedarfSonstiges: Euro,
    /** Kindergeld je Kind und Monat (§ 66 EStG). */
    val kindergeld: Euro,
    /** Angemessener Eigenbedarf der Eltern gegenüber nicht privilegierten volljährigen Kindern (§ 1603 Abs. 1 BGB). */
    val selbstbehalt: Euro,
    /** Im Selbstbehalt enthaltene Warmmiete; höhere angemessene Wohnkosten erhöhen den Selbstbehalt. */
    val selbstbehaltWarmmiete: Euro,
    /** Minijob-Verdienstgrenze pro Monat. */
    val minijobGrenze: Euro,
    val status: Rechtsstatus,
    /** Erläuterung bei vorläufigen Werten. */
    val hinweis: String? = null,
) {
    J2023(2023, 410.euro, 520.euro, 250.euro, 1650.euro, 650.euro, 520.euro, Rechtsstatus.AMTLICH),
    J2024(2024, 410.euro, 520.euro, 250.euro, 1750.euro, 650.euro, 538.euro, Rechtsstatus.AMTLICH),
    J2025(2025, 440.euro, 550.euro, 255.euro, 1750.euro, 650.euro, 556.euro, Rechtsstatus.AMTLICH),
    J2026(2026, 440.euro, 550.euro, 259.euro, 1750.euro, 650.euro, 603.euro, Rechtsstatus.AMTLICH),
    J2027(
        2027, 440.euro, 550.euro, 259.euro, 1750.euro, 650.euro, 633.euro, Rechtsstatus.VORLAEUFIG,
        hinweis = "Die Düsseldorfer Tabelle 2027 ist noch nicht veröffentlicht – es gelten vorläufig die Werte von 2026. " +
            "Kindergeld nach geltendem Recht 259 € (eine Erhöhung auf 267 € ist geplant, aber noch nicht beschlossen). " +
            "Die Minijob-Grenze von 633 € steht fest.",
    );

    val gueltigAb: LocalDate get() = LocalDate(jahr, 1, 1)

    /** Bedarf eines studierenden Kindes mit eigenem Haushalt (ohne KV/PV und Studiengebühren). */
    val bedarfStudent: Euro get() = bedarfWarmmiete + bedarfSonstiges

    override fun toString(): String = jahr.toString()

    companion object {
        /** Der Rechtsstand, der am [datum] gilt (vor 2023: ältester verfügbarer Stand). */
        fun fuer(datum: LocalDate): Jahr = entries.lastOrNull { it.gueltigAb <= datum } ?: entries.first()
    }
}

/** Datum im deutschen Format, z. B. `01.01.2026`. */
fun LocalDate.alsDatum(): String =
    "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"
