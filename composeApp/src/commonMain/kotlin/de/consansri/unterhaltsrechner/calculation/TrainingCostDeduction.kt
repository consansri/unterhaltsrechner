package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.euro

/** How training-related expenses reduce an apprenticeship pay (Ausbildungsvergütung) before it is counted. */
internal enum class TrainingCostDeduction(val label: String, val note: String) {
    INCLUDED_IN_NEED(
        "voll anrechnen",
        "Bei eigenem Haushalt schon im Bedarf enthalten (Leitlinien NRW Nr. 13.1 Abs. 2, 13.2).",
    ),
    FLAT_100(
        "− 100 € Pauschale",
        "Pauschal 100 € Mehrbedarf (Leitlinien vieler OLG).",
    ),
    TEN_PERCENT(
        "− 10 %, max. 100 €",
        "10 %, höchstens 100 € (OLG Naumburg).",
    ),
    ;

    override fun toString() = label

    fun countable(pay: Euro): Euro = when (this) {
        INCLUDED_IN_NEED -> pay
        FLAT_100 -> (pay - 100.euro).atLeast(Euro.ZERO)
        TEN_PERCENT -> pay - pay.dividedBy(10).atMost(100.euro)
    }
}
