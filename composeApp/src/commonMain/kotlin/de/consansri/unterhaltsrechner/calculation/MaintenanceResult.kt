package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.Percent

/** Monthly result of [calculate], including the full [derivation] that the UI shows unchanged. */
internal data class MaintenanceResult(
    val year: LegalYear,
    val need: Euro,
    val childIncome: Euro,
    val childBenefit: Euro,
    val remainingNeed: Euro,
    val father: ParentShare,
    val mother: ParentShare,
    val shortfall: Euro,
    val notices: List<Notice>,
    val derivation: List<Section>,
) {
    val parents: List<ParentShare> get() = listOf(father, mother)

    data class ParentShare(
        val parent: Parent,
        val selfReserve: Euro,
        /** Income available for maintenance: income − prior obligations − self-reserve, at least 0. */
        val available: Euro,
        /** Liability quota (§ 1606 Abs. 3 BGB); `null` if no parent is able to pay. */
        val quota: Percent?,
        val maintenance: Euro,
        /** Child benefit this parent receives and has to pass on to the child. */
        val forwardedChildBenefit: Euro,
    ) {
        /** Total monthly payment to the child. */
        val payment: Euro get() = maintenance + forwardedChildBenefit
    }
}
