package de.consansri.unterhaltsrechner.types

enum class Einkommensregelung(val uiName: (jahr: Jahr) -> String) {
    ALLES ({ "0 % frei" }),
    BIS_100({ "10 % frei (bis 100 €)" }),
    BIS_MINIJOB_GRENZE({ "${it.minijobGrenze} frei" }),
    NICHTS({ "100 % frei" });

    fun next(): Einkommensregelung {
        val values = Einkommensregelung.entries.toTypedArray()
        return values[(this.ordinal + 1) % values.size]
    }

    fun calc(einkommen: ZBetrag, jahr: Jahr): ZBetrag = when (this) {
        ALLES -> einkommen
        BIS_100 -> {
            val freibetrag = (einkommen * Prozent(0.1)).max(ZBetrag(Euro(100.0), Zeitraum.MONAT))
            einkommen - freibetrag
        }

        BIS_MINIJOB_GRENZE -> (einkommen - jahr.minijobGrenze).min(ZBetrag.NULL)
        NICHTS -> ZBetrag(Euro.NULL, Zeitraum.MONAT)
    }
}