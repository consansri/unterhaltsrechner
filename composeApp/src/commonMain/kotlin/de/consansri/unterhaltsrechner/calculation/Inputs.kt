package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.PeriodicAmount
import de.consansri.unterhaltsrechner.money.PeriodicAmount.Period

/**
 * All inputs of the calculation.
 *
 * Fields that are `null` follow the table value of the selected [LegalYear], so they adapt when the year changes.
 * Explicit values are kept.
 */
internal data class Inputs(
    val baseNeed: PeriodicAmount? = null,
    val healthInsurance: PeriodicAmount = PeriodicAmount.ZERO,
    val tuitionFees: PeriodicAmount = PeriodicAmount(Euro.ZERO, Period.SEMESTER),

    val studentGrant: PeriodicAmount = PeriodicAmount.ZERO,
    val studentGrantIsAdvance: Boolean = false,
    val apprenticeshipPay: PeriodicAmount = PeriodicAmount.ZERO,
    val trainingCostDeduction: TrainingCostDeduction = TrainingCostDeduction.INCLUDED_IN_NEED,
    val sideJob: PeriodicAmount = PeriodicAmount.ZERO,
    val sideJobRule: SideJobRule = SideJobRule.NOT_COUNTED,
    val sideJobAllowance: PeriodicAmount = PeriodicAmount.ZERO,
    val otherIncome: PeriodicAmount = PeriodicAmount.ZERO,

    val childBenefit: PeriodicAmount? = null,
    val childBenefitRecipient: ChildBenefitRecipient = ChildBenefitRecipient.CHILD,

    val father: ParentFinances = ParentFinances(),
    val mother: ParentFinances = ParentFinances(),
) {
    fun parent(parent: Parent): ParentFinances = when (parent) {
        Parent.FATHER -> father
        Parent.MOTHER -> mother
    }

    fun withParent(parent: Parent, finances: ParentFinances): Inputs = when (parent) {
        Parent.FATHER -> copy(father = finances)
        Parent.MOTHER -> copy(mother = finances)
    }

    data class ParentFinances(
        /** Income as stated in the BAföG notice (§ 21 BAföG, i.e. after tax and social-security allowance). */
        val income: PeriodicAmount = PeriodicAmount(Euro.ZERO, Period.YEAR),
        /** Maintenance for minor siblings and other prior obligations (§ 1609 BGB). */
        val priorObligations: PeriodicAmount = PeriodicAmount.ZERO,
        /** Actual warm rent; only relevant if it exceeds the warm rent contained in the self-reserve. */
        val warmRent: PeriodicAmount = PeriodicAmount.ZERO,
    )
}
