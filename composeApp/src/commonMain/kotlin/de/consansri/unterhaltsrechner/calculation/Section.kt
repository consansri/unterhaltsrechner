package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro

/** A titled block of the derivation, e.g. "1. Bedarf des Kindes". */
internal data class Section(val title: String, val steps: List<Step>) {

    /** One line of the derivation. */
    data class Step(
        val label: String,
        val amount: Euro,
        val kind: Kind,
        val legalBasis: String? = null,
        val note: String? = null,
    ) {
        enum class Kind { ITEM, SURCHARGE, DEDUCTION, TOTAL }
    }
}
