package de.consansri.unterhaltsrechner.calculation

/**
 * How income from a side or holiday job counts towards the student's need.
 *
 * Students have no duty to work, so such income only counts if equitable
 * (§ 1577 Abs. 2 BGB analogously, Leitlinien Nr. 13.2).
 */
internal enum class SideJobRule(val label: String, val note: String) {
    NOT_COUNTED(
        "nicht anrechnen",
        "Regelfall bei geringfügiger Tätigkeit (Ferienjob, Minijob).",
    ),
    EXEMPT_UP_TO_MINIJOB(
        "frei bis Minijob-Grenze",
        "Bis zur Minijob-Grenze frei, der Rest zählt.",
    ),
    CUSTOM_ALLOWANCE(
        "eigener Freibetrag",
        "Bis zum Freibetrag frei, der Rest zählt.",
    ),
    FULLY_COUNTED(
        "voll anrechnen",
        "Z. B. bei zumutbarer, üblicher Tätigkeit.",
    ),
    ;

    override fun toString() = label
}
