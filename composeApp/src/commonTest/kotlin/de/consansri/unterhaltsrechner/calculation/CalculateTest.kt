package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.calculation.Inputs.ParentFinances
import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.PeriodicAmount
import de.consansri.unterhaltsrechner.money.PeriodicAmount.Period
import de.consansri.unterhaltsrechner.money.euro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateTest {

    private fun monthly(euro: Int) = PeriodicAmount.monthly(euro.euro)

    private fun parents(father: Int, mother: Int) = Inputs(
        father = ParentFinances(income = monthly(father)),
        mother = ParentFinances(income = monthly(mother)),
    )

    private fun totalMaintenance(result: MaintenanceResult) = result.father.maintenance + result.mother.maintenance

    /** Holds for every calculation: maintenance + shortfall = remaining need, and no share exceeds the available income. */
    private fun assertInvariants(result: MaintenanceResult) {
        assertEquals(result.remainingNeed, totalMaintenance(result) + result.shortfall, "maintenance + shortfall = remaining need")
        result.parents.forEach { assertTrue(it.maintenance <= it.available, "${it.parent}: share <= available income") }
    }

    @Test
    fun childBenefitIsCreditedInFullAndRemainderSplitAfterSelfReserve() {
        val result = calculate(parents(father = 3_000, mother = 2_000), LegalYear.Y2026)

        assertEquals(990.euro, result.need)
        assertEquals(259.euro, result.childBenefit)
        assertEquals(731.euro, result.remainingNeed)
        assertEquals(1_250.euro, result.father.available)
        assertEquals(250.euro, result.mother.available)
        assertEquals(Euro(60_917), result.father.maintenance)
        assertEquals(Euro(12_183), result.mother.maintenance)
        assertEquals("83,33 %", result.father.quota.toString())
        assertEquals(Euro.ZERO, result.shortfall)
        assertTrue(result.notices.isEmpty())
        assertInvariants(result)
    }

    @Test
    fun yearlyIncomeIsConvertedToMonthly() {
        val result = calculate(
            Inputs(
                father = ParentFinances(income = PeriodicAmount(36_000.euro, Period.YEAR)),
                mother = ParentFinances(income = PeriodicAmount(24_000.euro, Period.YEAR)),
            ),
            LegalYear.Y2026,
        )
        assertEquals(Euro(60_917), result.father.maintenance)
        assertEquals(Euro(12_183), result.mother.maintenance)
    }

    @Test
    fun shortfallWhenParentsCannotCoverRemainingNeed() {
        val result = calculate(parents(father = 2_000, mother = 1_500), LegalYear.Y2026)

        assertEquals(250.euro, result.father.maintenance)
        assertEquals(Euro.ZERO, result.mother.maintenance)
        assertEquals(481.euro, result.shortfall)
        assertEquals(listOf<Notice>(Notice.Shortfall(481.euro)), result.notices)
        assertInvariants(result)
    }

    @Test
    fun noParentAbleToPay() {
        val result = calculate(parents(father = 1_700, mother = 1_000), LegalYear.Y2026)

        assertEquals(Euro.ZERO, totalMaintenance(result))
        assertEquals(731.euro, result.shortfall)
        assertEquals(null, result.father.quota)
        assertEquals(listOf<Notice>(Notice.NoParentAbleToPay), result.notices)
        assertInvariants(result)
    }

    @Test
    fun studentGrantAndHealthInsurance() {
        val result = calculate(
            parents(father = 4_000, mother = 4_000).copy(studentGrant = monthly(400), healthInsurance = monthly(120)),
            LegalYear.Y2026,
        )
        assertEquals(1_110.euro, result.need)
        assertEquals(400.euro, result.childIncome)
        assertEquals(451.euro, result.remainingNeed)
        assertEquals(Euro(22_550), result.father.maintenance)
        assertEquals(Euro(22_550), result.mother.maintenance)
        assertInvariants(result)
    }

    @Test
    fun childIncomeAlsoCoversHealthInsuranceAndTuitionFees() {
        val result = calculate(
            parents(father = 4_000, mother = 4_000).copy(
                healthInsurance = monthly(120),
                tuitionFees = PeriodicAmount(1_500.euro, Period.SEMESTER),
                otherIncome = monthly(1_000),
            ),
            LegalYear.Y2026,
        )
        assertEquals(1_360.euro, result.need)
        assertEquals(101.euro, result.remainingNeed, "1.360 − 1.000 − 259")
    }

    @Test
    fun studentGrantAdvanceDoesNotReduceMaintenance() {
        val result = calculate(
            parents(father = 3_000, mother = 2_000).copy(studentGrant = monthly(400), studentGrantIsAdvance = true),
            LegalYear.Y2026,
        )
        assertEquals(Euro.ZERO, result.childIncome)
        assertEquals(731.euro, result.remainingNeed)
    }

    @Test
    fun sideJobRules() {
        fun remainingNeed(rule: SideJobRule, allowance: Int = 0) = calculate(
            parents(father = 4_000, mother = 4_000).copy(
                sideJob = monthly(700),
                sideJobRule = rule,
                sideJobAllowance = monthly(allowance),
            ),
            LegalYear.Y2026,
        ).remainingNeed

        assertEquals(731.euro, remainingNeed(SideJobRule.NOT_COUNTED))
        assertEquals(634.euro, remainingNeed(SideJobRule.EXEMPT_UP_TO_MINIJOB), "700 − 603 = 97 counted")
        assertEquals(331.euro, remainingNeed(SideJobRule.CUSTOM_ALLOWANCE, allowance = 300))
        assertEquals(31.euro, remainingNeed(SideJobRule.FULLY_COUNTED))
    }

    @Test
    fun apprenticeshipPay() {
        assertEquals(800.euro, TrainingCostDeduction.INCLUDED_IN_NEED.countable(800.euro))
        assertEquals(700.euro, TrainingCostDeduction.FLAT_100.countable(800.euro))
        assertEquals(Euro.ZERO, TrainingCostDeduction.FLAT_100.countable(80.euro))
        assertEquals(720.euro, TrainingCostDeduction.TEN_PERCENT.countable(800.euro))
        assertEquals(1_400.euro, TrainingCostDeduction.TEN_PERCENT.countable(1_500.euro), "at most 100 €")
    }

    @Test
    fun childBenefitRecipientForwardsBenefitOnTopOfMaintenance() {
        val result = calculate(
            parents(father = 3_000, mother = 2_000).copy(childBenefitRecipient = ChildBenefitRecipient.MOTHER),
            LegalYear.Y2026,
        )
        assertEquals(Euro.ZERO, result.father.forwardedChildBenefit)
        assertEquals(259.euro, result.mother.forwardedChildBenefit)
        assertEquals(Euro(12_183) + 259.euro, result.mother.payment)
        assertEquals(Euro(60_917), result.father.payment)
    }

    @Test
    fun needCovered() {
        val result = calculate(parents(father = 4_000, mother = 4_000).copy(studentGrant = monthly(855)), LegalYear.Y2026)
        assertEquals(Euro.ZERO, result.remainingNeed)
        assertEquals(Euro.ZERO, totalMaintenance(result))
        assertEquals(listOf<Notice>(Notice.NeedCovered), result.notices)
    }

    @Test
    fun priorObligationsAndWarmRentRaiseSelfReserve() {
        val result = calculate(
            Inputs(
                father = ParentFinances(
                    income = monthly(3_000),
                    priorObligations = monthly(500),
                    warmRent = monthly(800),
                ),
                mother = ParentFinances(income = monthly(3_000)),
            ),
            LegalYear.Y2026,
        )
        assertEquals(1_900.euro, result.father.selfReserve, "1.750 + (800 − 650)")
        assertEquals(600.euro, result.father.available, "3.000 − 500 − 1.900")
        assertEquals(1_250.euro, result.mother.available)
        assertInvariants(result)
    }

    @Test
    fun tableValuesFollowYearButOwnValuesStay() {
        val standard = parents(father = 4_000, mother = 4_000)
        assertEquals(930.euro - 250.euro, calculate(standard, LegalYear.Y2024).remainingNeed)
        assertEquals(990.euro - 255.euro, calculate(standard, LegalYear.Y2025).remainingNeed)

        val ownNeed = standard.copy(baseNeed = monthly(1_100))
        assertEquals(1_100.euro - 250.euro, calculate(ownNeed, LegalYear.Y2024).remainingNeed)
        assertEquals(1_100.euro - 259.euro, calculate(ownNeed, LegalYear.Y2026).remainingNeed)
    }

    @Test
    fun missingParentIncomeIsReported() {
        val result = calculate(Inputs(), LegalYear.Y2026)
        assertEquals(listOf<Notice>(Notice.ParentIncomeMissing), result.notices)
        assertEquals(Euro.ZERO, totalMaintenance(result))
    }

    @Test
    fun provisionalYearAddsNotice() {
        val result = calculate(parents(father = 3_000, mother = 2_000), LegalYear.Y2027)
        assertTrue(result.notices.any { it is Notice.ProvisionalValues })
    }

    @Test
    fun distributionLosesNoCent() {
        // 1 € over three equal weights: 33 + 34 + 33 cents (cumulative rounding)
        val shares = distribute(1.euro, listOf(10.euro, 10.euro, 10.euro))
        assertEquals(1.euro, shares.fold(Euro.ZERO, Euro::plus))
        assertTrue(shares.all { it == Euro(33) || it == Euro(34) })

        // Awkward weights over many amounts: the sum always matches and no share exceeds its weight
        val weights = listOf(Euro(12_345), Euro(67_891))
        for (cents in 0L..80_236L step 997) {
            val amount = Euro(cents)
            val distributed = distribute(amount, weights)
            assertEquals(amount, distributed.fold(Euro.ZERO, Euro::plus), "sum for $amount")
            distributed.zip(weights).forEach { (share, weight) -> assertTrue(share <= weight, "$share <= $weight for $amount") }
        }
    }

    @Test
    fun derivationEndsWithTheShares() {
        val result = calculate(parents(father = 3_000, mother = 2_000), LegalYear.Y2026)
        val totals = result.derivation.last().steps.filter { it.kind == Section.Step.Kind.TOTAL }.map { it.amount }
        assertEquals(listOf(result.father.maintenance, result.mother.maintenance), totals)
        assertEquals(result.remainingNeed, result.derivation[1].steps.last().amount)
    }
}
