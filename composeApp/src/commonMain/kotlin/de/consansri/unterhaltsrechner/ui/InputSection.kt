package de.consansri.unterhaltsrechner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.calculation.ChildBenefitRecipient
import de.consansri.unterhaltsrechner.calculation.Inputs
import de.consansri.unterhaltsrechner.calculation.LegalYear
import de.consansri.unterhaltsrechner.calculation.Parent
import de.consansri.unterhaltsrechner.calculation.SideJobRule
import de.consansri.unterhaltsrechner.calculation.TrainingCostDeduction
import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.PeriodicAmount
import de.consansri.unterhaltsrechner.ui.components.AmountField
import de.consansri.unterhaltsrechner.ui.components.Callout
import de.consansri.unterhaltsrechner.ui.components.CalloutTone
import de.consansri.unterhaltsrechner.ui.components.Explanation
import de.consansri.unterhaltsrechner.ui.components.RadioOptions
import de.consansri.unterhaltsrechner.ui.components.SectionCard
import de.consansri.unterhaltsrechner.ui.components.SegmentedPicker

/** From this width on, the two parents' cards are shown side by side. */
private val ParentsSideBySideMinWidth = 560.dp

@Composable
internal fun InputSection(
    inputs: Inputs,
    year: LegalYear,
    onInputsChange: (Inputs) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NeedCard(inputs, year, onInputsChange)
        ChildCard(inputs, year, onInputsChange)
        ParentsCard(inputs, year, onInputsChange)
    }
}

/** Stores `null` if the value equals the table value, so the field keeps following the year. */
private fun overrideOrNull(new: PeriodicAmount, tableValue: Euro): PeriodicAmount? =
    if (new.perMonth == tableValue) null else PeriodicAmount.monthly(new.amount)

@Composable
private fun NeedCard(inputs: Inputs, year: LegalYear, onChange: (Inputs) -> Unit) {
    SectionCard("1. Bedarf des Kindes") {
        AmountField(
            label = "Regelbedarf (Düsseldorfer Tabelle $year)",
            value = inputs.baseNeed ?: PeriodicAmount.monthly(year.studentNeed),
            onChange = { onChange(inputs.copy(baseNeed = overrideOrNull(it, year.studentNeed))) },
            periodSelectable = false,
            tableValue = year.studentNeed,
            onReset = { onChange(inputs.copy(baseNeed = null)) },
            note = "Inkl. bis ${year.needWarmRent} Warmmiete. Abweichung nach oben möglich.",
        )
        AmountField(
            label = "Kranken- und Pflegeversicherung",
            value = inputs.healthInsurance,
            onChange = { onChange(inputs.copy(healthInsurance = it)) },
            note = "Nur ohne beitragsfreie Familienversicherung (z. B. ab 25 Jahren).",
        )
        AmountField(
            label = "Studiengebühren",
            value = inputs.tuitionFees,
            onChange = { onChange(inputs.copy(tuitionFees = it)) },
            note = "Ohne Semesterbeitrag (im Regelbedarf).",
        )
    }
}

@Composable
private fun ChildCard(inputs: Inputs, year: LegalYear, onChange: (Inputs) -> Unit) {
    SectionCard("2. Einkünfte des Kindes & Kindergeld") {
        AmountField(
            label = "BAföG",
            value = inputs.studentGrant,
            onChange = { onChange(inputs.copy(studentGrant = it)) },
            note = "Laut Bescheid, Zuschuss + Darlehen. Voll angerechnet.",
        )
        LabeledCheckbox(
            text = "Vorausleistung (§ 36 BAföG)",
            note = "Mindert den Unterhalt nicht; das Land fordert ihn von den Eltern.",
            checked = inputs.studentGrantIsAdvance,
            onChange = { onChange(inputs.copy(studentGrantIsAdvance = it)) },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        AmountField(
            label = "Kindergeld",
            value = inputs.childBenefit ?: PeriodicAmount.monthly(year.childBenefit),
            onChange = { onChange(inputs.copy(childBenefit = overrideOrNull(it, year.childBenefit))) },
            periodSelectable = false,
            tableValue = year.childBenefit,
            onReset = { onChange(inputs.copy(childBenefit = null)) },
            note = "Voll angerechnet (§ 1612b Abs. 1 Nr. 2 BGB).",
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Kindergeld geht an", style = MaterialTheme.typography.labelLarge)
            SegmentedPicker(
                options = ChildBenefitRecipient.entries,
                selected = inputs.childBenefitRecipient,
                onSelect = { onChange(inputs.copy(childBenefitRecipient = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (inputs.childBenefitRecipient.parent != null) {
                Explanation("Muss es zusätzlich zum Unterhalt an das Kind weitergeben.")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        AmountField(
            label = "Nebenjob / Ferienjob",
            value = inputs.sideJob,
            onChange = { onChange(inputs.copy(sideJob = it)) },
            note = "Netto neben dem Studium.",
        )
        if (inputs.sideJob.amount != Euro.ZERO) {
            RadioOptions(
                options = SideJobRule.entries,
                selected = inputs.sideJobRule,
                onSelect = { onChange(inputs.copy(sideJobRule = it)) },
                label = { if (it == SideJobRule.EXEMPT_UP_TO_MINIJOB) "${it.label} (${year.minijobLimit})" else it.label },
                note = { it.note },
            )
            if (inputs.sideJobRule == SideJobRule.CUSTOM_ALLOWANCE) {
                AmountField(
                    label = "Freibetrag",
                    value = inputs.sideJobAllowance,
                    onChange = { onChange(inputs.copy(sideJobAllowance = it)) },
                )
            }
        }

        AmountField(
            label = "Ausbildungsvergütung",
            value = inputs.apprenticeshipPay,
            onChange = { onChange(inputs.copy(apprenticeshipPay = it)) },
            note = "Netto, z. B. im dualen Studium.",
        )
        if (inputs.apprenticeshipPay.amount != Euro.ZERO) {
            RadioOptions(
                options = TrainingCostDeduction.entries,
                selected = inputs.trainingCostDeduction,
                onSelect = { onChange(inputs.copy(trainingCostDeduction = it)) },
                label = { it.label },
                note = { it.note },
            )
        }

        AmountField(
            label = "Sonstige Einkünfte",
            value = inputs.otherIncome,
            onChange = { onChange(inputs.copy(otherIncome = it)) },
            note = "Z. B. Waisenrente, Kapitalerträge. Voll angerechnet.",
        )
    }
}

@Composable
private fun ParentsCard(inputs: Inputs, year: LegalYear, onChange: (Inputs) -> Unit) {
    SectionCard("3. Einkommen der Eltern") {
        Callout(
            title = "Einkommen aus dem BAföG-Bescheid",
            text = "Nach Steuern und Sozialpauschale, meist als Jahresbetrag. Beruht auf dem vorletzten Kalenderjahr; " +
                "das unterhaltsrechtliche Einkommen kann abweichen.",
            tone = CalloutTone.INFO,
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val sideBySide = maxWidth >= ParentsSideBySideMinWidth
            val parentFields: @Composable (Modifier) -> Unit = { fieldModifier ->
                Parent.entries.forEach { parent ->
                    ParentFields(
                        parent = parent,
                        finances = inputs.parent(parent),
                        year = year,
                        onChange = { onChange(inputs.withParent(parent, it)) },
                        modifier = fieldModifier,
                    )
                }
            }
            if (sideBySide) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { parentFields(Modifier.weight(1f)) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { parentFields(Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun ParentFields(
    parent: Parent,
    finances: Inputs.ParentFinances,
    year: LegalYear,
    onChange: (Inputs.ParentFinances) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasExtras = finances.priorObligations.amount != Euro.ZERO || finances.warmRent.amount != Euro.ZERO
    var extrasOpen by remember { mutableStateOf(hasExtras) } // initial-only

    OutlinedCard(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(parent.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            AmountField(
                label = "Bereinigtes Nettoeinkommen",
                value = finances.income,
                onChange = { onChange(finances.copy(income = it)) },
                note = "Ersatzweise laut BAföG-Bescheid.",
            )
            // Fields holding a value stay visible so that nothing enters the calculation hidden.
            if (!hasExtras) {
                TextButton(onClick = { extrasOpen = !extrasOpen }) {
                    Text(if (extrasOpen) "Weniger Angaben" else "Weitere Angaben")
                }
            }
            if (extrasOpen || hasExtras) {
                AmountField(
                    label = "Vorrangige Unterhaltspflichten",
                    value = finances.priorObligations,
                    onChange = { onChange(finances.copy(priorObligations = it)) },
                    note = "Z. B. minderjährige Geschwister (§ 1609 BGB).",
                )
                AmountField(
                    label = "Eigene Warmmiete",
                    value = finances.warmRent,
                    onChange = { onChange(finances.copy(warmRent = it)) },
                    note = "Nur relevant über ${year.selfReserveWarmRent} (wenn angemessen): " +
                        "erhöht den Selbstbehalt (${year.selfReserve}) um die Differenz.",
                )
            }
        }
    }
}

@Composable
private fun LabeledCheckbox(text: String, note: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(8.dp))
        Column(Modifier.padding(vertical = 6.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            if (checked) Explanation(note)
        }
    }
}
