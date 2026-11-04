package de.consansri.unterhaltsrechner.types

import kotlin.jvm.JvmInline

/**
 * Prozentwert mit zwei Nachkommastellen, intern in Hundertstel Prozent (10000 = 100,00 %).
 * Dient nur der Anzeige – gerechnet wird immer mit den exakten Euro-Beträgen.
 */
@JvmInline
value class Prozent(val inHundredths: Long) {

    override fun toString(): String {
        val ganz = inHundredths / 100
        val nachkomma = (if (inHundredths < 0) -inHundredths else inHundredths) % 100
        return "$ganz,${nachkomma.toString().padStart(2, '0')} %"
    }

    companion object {
        /** Anteil von [teil] an [gesamt], gerundet; `null`, wenn [gesamt] 0 ist. */
        fun anteil(teil: Euro, gesamt: Euro): Prozent? =
            if (gesamt.inCents == 0L) null
            else Prozent(Euro.rundeDivision(teil.inCents * 10_000, gesamt.inCents))
    }
}
