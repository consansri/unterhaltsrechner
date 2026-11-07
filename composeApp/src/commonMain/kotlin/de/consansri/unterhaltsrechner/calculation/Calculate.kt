package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.calculation.MaintenanceResult.ParentShare
import de.consansri.unterhaltsrechner.calculation.Section.Step
import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.Percent

/**
 * Maintenance for an adult student living in their own household.
 *
 * 1. Need = standard need of the Düsseldorfer Tabelle + health insurance + tuition fees.
 * 2. The child's own income (including BAföG) is credited against it.
 * 3. Child benefit is credited in full (§ 1612b Abs. 1 Nr. 2 BGB).
 * 4. The parents bear the remaining need in proportion to their income minus the appropriate self-reserve
 *    (§ 1606 Abs. 3 S. 1 BGB), each at most up to that available amount.
 *
 * All amounts are monthly. The function is pure and returns the full derivation along with the result.
 */
internal fun calculate(inputs: Inputs, year: LegalYear): MaintenanceResult {
    val derivation = mutableListOf<Section>()

    val baseNeed = inputs.baseNeed?.perMonth ?: year.studentNeed
    val healthInsurance = inputs.healthInsurance.perMonth
    val tuitionFees = inputs.tuitionFees.perMonth
    val need = baseNeed + healthInsurance + tuitionFees
    derivation += Section(
        "1. Bedarf des Kindes",
        buildList {
            add(
                Step(
                    "Regelbedarf Studierende (eigener Haushalt)", baseNeed, Step.Kind.ITEM,
                    legalBasis = "Düsseldorfer Tabelle $year",
                    note = if (inputs.baseNeed == null) "Davon bis ${year.needWarmRent} Warmmiete." else "Tabellenwert: ${year.studentNeed}.",
                ),
            )
            if (healthInsurance != Euro.ZERO) {
                add(
                    Step(
                        "Kranken- und Pflegeversicherung", healthInsurance, Step.Kind.SURCHARGE,
                        legalBasis = "Düsseldorfer Tabelle, Anm. zu KV/PV",
                        note = "Nicht im Regelbedarf.",
                    ),
                )
            }
            if (tuitionFees != Euro.ZERO) {
                add(
                    Step(
                        "Studiengebühren", tuitionFees, Step.Kind.SURCHARGE,
                        legalBasis = "Düsseldorfer Tabelle, Anm. zu Studiengebühren",
                        note = "Ohne Semesterbeitrag (der ist im Regelbedarf).",
                    ),
                )
            }
            add(Step("Gesamtbedarf", need, Step.Kind.TOTAL))
        },
    )

    val studentGrant = if (inputs.studentGrantIsAdvance) Euro.ZERO else inputs.studentGrant.perMonth
    val apprenticeshipPay = inputs.apprenticeshipPay.perMonth
    val countedApprenticeshipPay = inputs.trainingCostDeduction.countable(apprenticeshipPay)
    val sideJob = inputs.sideJob.perMonth
    val countedSideJob = when (inputs.sideJobRule) {
        SideJobRule.NOT_COUNTED -> Euro.ZERO
        SideJobRule.EXEMPT_UP_TO_MINIJOB -> (sideJob - year.minijobLimit).atLeast(Euro.ZERO)
        SideJobRule.CUSTOM_ALLOWANCE -> (sideJob - inputs.sideJobAllowance.perMonth).atLeast(Euro.ZERO)
        SideJobRule.FULLY_COUNTED -> sideJob
    }
    val otherIncome = inputs.otherIncome.perMonth
    val childIncome = studentGrant + countedApprenticeshipPay + countedSideJob + otherIncome
    val childBenefit = inputs.childBenefit?.perMonth ?: year.childBenefit
    val uncoveredNeed = need - childIncome - childBenefit
    val remainingNeed = uncoveredNeed.atLeast(Euro.ZERO)

    derivation += Section(
        "2. Anrechnung auf den Bedarf",
        buildList {
            add(Step("Gesamtbedarf", need, Step.Kind.ITEM))
            if (inputs.studentGrant.amount != Euro.ZERO) {
                add(
                    if (inputs.studentGrantIsAdvance) {
                        Step(
                            "BAföG (Vorausleistung)", Euro.ZERO, Step.Kind.DEDUCTION,
                            legalBasis = "§§ 36, 37 BAföG",
                            note = "Mindert den Unterhalt nicht; der Anspruch geht auf das Land über.",
                        )
                    } else {
                        Step(
                            "BAföG", studentGrant, Step.Kind.DEDUCTION,
                            legalBasis = "Leitlinien Nr. 2.4, 13.2",
                            note = "Voll, auch das Darlehen.",
                        )
                    },
                )
            }
            if (apprenticeshipPay != Euro.ZERO) {
                add(
                    Step(
                        "Ausbildungsvergütung (${inputs.trainingCostDeduction.label})", countedApprenticeshipPay,
                        Step.Kind.DEDUCTION,
                        legalBasis = "Leitlinien Nr. 10.2.3, 13.2",
                        note = "Vergütung $apprenticeshipPay. ${inputs.trainingCostDeduction.note}",
                    ),
                )
            }
            if (sideJob != Euro.ZERO) {
                add(
                    Step(
                        "Nebenjob (${inputs.sideJobRule.label})", countedSideJob, Step.Kind.DEDUCTION,
                        legalBasis = "§ 1577 Abs. 2 BGB entspr., Leitlinien Nr. 13.2",
                        note = "Verdienst $sideJob. ${inputs.sideJobRule.note}",
                    ),
                )
            }
            if (otherIncome != Euro.ZERO) {
                add(Step("Sonstige Einkünfte", otherIncome, Step.Kind.DEDUCTION, legalBasis = "Leitlinien Nr. 13.2"))
            }
            add(
                Step(
                    "Kindergeld (voll)", childBenefit, Step.Kind.DEDUCTION,
                    legalBasis = "§ 1612b Abs. 1 Nr. 2 BGB",
                    note = "Bei Volljährigen voll anzurechnen.",
                ),
            )
            add(
                Step(
                    "Restbedarf", remainingNeed, Step.Kind.TOTAL,
                    note = if (uncoveredNeed < Euro.ZERO) "Anrechnung übersteigt den Bedarf." else null,
                ),
            )
        },
    )

    val capacities = Parent.entries.map { parent ->
        val finances = inputs.parent(parent)
        val income = finances.income.perMonth
        val priorObligations = finances.priorObligations.perMonth
        val rentSurcharge = (finances.warmRent.perMonth - year.selfReserveWarmRent).atLeast(Euro.ZERO)
        val selfReserve = year.selfReserve + rentSurcharge
        val available = (income - priorObligations - selfReserve).atLeast(Euro.ZERO)
        ParentCapacity(parent, income, priorObligations, rentSurcharge, selfReserve, available)
    }
    val totalAvailable = capacities.fold(Euro.ZERO) { sum, capacity -> sum + capacity.available }
    val maintenanceShares = distribute(remainingNeed, capacities.map { it.available })
    val shortfall = remainingNeed - maintenanceShares.fold(Euro.ZERO, Euro::plus)

    val shares = capacities.zip(maintenanceShares) { capacity, maintenance ->
        ParentShare(
            parent = capacity.parent,
            selfReserve = capacity.selfReserve,
            available = capacity.available,
            quota = Percent.of(capacity.available, totalAvailable),
            maintenance = maintenance,
            forwardedChildBenefit = if (inputs.childBenefitRecipient.parent == capacity.parent) childBenefit else Euro.ZERO,
        )
    }

    capacities.forEach { capacity ->
        derivation += Section(
            "3. ${capacity.parent}: einsetzbares Einkommen",
            buildList {
                add(
                    Step(
                        "Einkommen", capacity.income, Step.Kind.ITEM,
                        legalBasis = "BAföG-Bescheid (§ 21 BAföG)",
                        note = "Monatsbetrag laut Bescheid.",
                    ),
                )
                if (capacity.priorObligations != Euro.ZERO) {
                    add(
                        Step(
                            "Vorrangige Unterhaltspflichten / Abzüge", capacity.priorObligations, Step.Kind.DEDUCTION,
                            legalBasis = "§ 1609 BGB, Leitlinien Nr. 13.3 Abs. 2",
                        ),
                    )
                }
                add(
                    Step(
                        "Angemessener Selbstbehalt", year.selfReserve, Step.Kind.DEDUCTION,
                        legalBasis = "§ 1603 Abs. 1 BGB, Düsseldorfer Tabelle $year",
                        note = "Davon ${year.selfReserveWarmRent} Warmmiete.",
                    ),
                )
                if (capacity.rentSurcharge != Euro.ZERO) {
                    add(
                        Step(
                            "Erhöhung wegen höherer Warmmiete", capacity.rentSurcharge, Step.Kind.DEDUCTION,
                            legalBasis = "Düsseldorfer Tabelle $year",
                            note = "Bei angemessenen, höheren Wohnkosten.",
                        ),
                    )
                }
                add(Step("Einsetzbares Einkommen", capacity.available, Step.Kind.TOTAL))
            },
        )
    }

    derivation += Section(
        "4. Aufteilung auf die Eltern",
        buildList {
            add(Step("Restbedarf", remainingNeed, Step.Kind.ITEM))
            add(
                Step(
                    "Summe einsetzbarer Einkommen", totalAvailable, Step.Kind.ITEM,
                    legalBasis = "§ 1606 Abs. 3 S. 1 BGB",
                    note = "Haftung im Verhältnis der einsetzbaren Einkommen.",
                ),
            )
            shares.forEach { share ->
                add(
                    Step(
                        "Anteil ${share.parent}", share.maintenance, Step.Kind.TOTAL,
                        note = when {
                            share.quota == null -> "Kein Elternteil leistungsfähig."
                            totalAvailable < remainingNeed -> "Mangelfall: ${share.available} (gesamtes einsetzbares Einkommen)."
                            else -> "$remainingNeed × ${share.available} ÷ $totalAvailable (Quote ${share.quota})"
                        },
                    ),
                )
            }
            if (shortfall != Euro.ZERO) {
                add(
                    Step(
                        "Ungedeckter Fehlbetrag", shortfall, Step.Kind.TOTAL,
                        note = "Eltern sind über den Selbstbehalt hinaus nicht leistungsfähig.",
                    ),
                )
            }
        },
    )

    val notices = buildList {
        when (val status = year.status) {
            LegalYear.Status.Official -> Unit
            is LegalYear.Status.Provisional -> add(Notice.ProvisionalValues(status.note))
        }
        when {
            remainingNeed == Euro.ZERO -> add(Notice.NeedCovered)
            capacities.all { it.income == Euro.ZERO } -> add(Notice.ParentIncomeMissing)
            totalAvailable == Euro.ZERO -> add(Notice.NoParentAbleToPay)
            shortfall > Euro.ZERO -> add(Notice.Shortfall(shortfall))
        }
    }

    return MaintenanceResult(
        year = year,
        need = need,
        childIncome = childIncome,
        childBenefit = childBenefit,
        remainingNeed = remainingNeed,
        father = shares.first { it.parent == Parent.FATHER },
        mother = shares.first { it.parent == Parent.MOTHER },
        shortfall = shortfall,
        notices = notices,
        derivation = derivation,
    )
}

private class ParentCapacity(
    val parent: Parent,
    val income: Euro,
    val priorObligations: Euro,
    val rentSurcharge: Euro,
    val selfReserve: Euro,
    val available: Euro,
)
