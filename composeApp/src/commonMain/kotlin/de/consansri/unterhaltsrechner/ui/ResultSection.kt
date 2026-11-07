package de.consansri.unterhaltsrechner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.calculation.ChildBenefitRecipient
import de.consansri.unterhaltsrechner.calculation.MaintenanceResult
import de.consansri.unterhaltsrechner.calculation.Notice
import de.consansri.unterhaltsrechner.calculation.Section.Step
import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.ui.components.Callout
import de.consansri.unterhaltsrechner.ui.components.CalloutTone
import de.consansri.unterhaltsrechner.ui.components.Explanation
import de.consansri.unterhaltsrechner.ui.components.SectionCard
import de.consansri.unterhaltsrechner.ui.theme.SignalColors

@Composable
internal fun ResultSection(
    result: MaintenanceResult,
    childBenefitRecipient: ChildBenefitRecipient,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ResultCard(result, childBenefitRecipient)
        DerivationCard(result)
    }
}

@Composable
private fun ResultCard(result: MaintenanceResult, childBenefitRecipient: ChildBenefitRecipient) {
    SectionCard("Ergebnis", subtitle = "Pro Monat · Rechtsstand ${result.year}") {
        result.notices.forEach { NoticeCallout(it) }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            result.parents.forEach { ParentShareCard(it, Modifier.weight(1f)) }
        }

        Coverage(result, childBenefitRecipient)
    }
}

@Composable
private fun NoticeCallout(notice: Notice) {
    when (notice) {
        is Notice.ProvisionalValues -> Callout(notice.note, CalloutTone.WARNING, title = "Vorläufige Werte")
        Notice.NeedCovered -> Callout(
            "Eigene Einkünfte und Kindergeld decken den Bedarf. Kein Unterhalt geschuldet.",
            CalloutTone.INFO,
            title = "Bedarf gedeckt",
        )
        Notice.ParentIncomeMissing -> Callout(
            "Unter „3. Einkommen der Eltern“ das Einkommen laut BAföG-Bescheid eintragen.",
            CalloutTone.INFO,
            title = "Einkommen der Eltern fehlt",
        )
        Notice.NoParentAbleToPay -> Callout(
            "Kein Elternteil verdient mehr als den Selbstbehalt – keine Unterhaltspflicht.",
            CalloutTone.WARNING,
            title = "Nicht leistungsfähig",
        )
        is Notice.Shortfall -> Callout(
            "Jeder zahlt höchstens sein Einkommen über dem Selbstbehalt. ${notice.amount} pro Monat bleiben ungedeckt.",
            CalloutTone.ERROR,
            title = "Mangelfall",
        )
    }
}

@Composable
private fun ParentShareCard(share: MaintenanceResult.ParentShare, modifier: Modifier = Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${share.parent} zahlt", style = MaterialTheme.typography.labelLarge)
            Text(share.payment.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("pro Monat", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            SmallRow("Unterhaltsanteil", share.maintenance.toString())
            SmallRow("Haftungsquote", share.quota?.toString() ?: "–")
            if (share.forwardedChildBenefit != Euro.ZERO) {
                SmallRow("+ Kindergeld weiterleiten", share.forwardedChildBenefit.toString())
            }
        }
    }
}

@Composable
private fun SmallRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

private class CoverageSlice(val label: String, val amount: Euro, val color: Color)

/** Shows which sources cover the child's need, as a bar and a legend. */
@Composable
private fun Coverage(result: MaintenanceResult, childBenefitRecipient: ChildBenefitRecipient) {
    val colors = MaterialTheme.colorScheme
    val signal = SignalColors.Local.current
    val slices = listOf(
        CoverageSlice("Unterhalt ${result.father.parent}", result.father.maintenance, colors.primary),
        CoverageSlice("Unterhalt ${result.mother.parent}", result.mother.maintenance, colors.tertiary),
        CoverageSlice(
            "Kindergeld" + (childBenefitRecipient.parent?.let { " (über $it)" } ?: ""),
            result.childBenefit,
            colors.secondary,
        ),
        CoverageSlice("Einkünfte des Kindes", result.childIncome, colors.outline),
        CoverageSlice("Ungedeckt", result.shortfall, signal.warning),
    ).filter { it.amount > Euro.ZERO }
    if (slices.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Deckung des Bedarfs von ${result.need}", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
            slices.forEach { Box(Modifier.weight(it.amount.cents.toFloat()).fillMaxHeight().background(it.color)) }
        }
        slices.forEach { LegendRow(it) }
        val covered = slices.fold(Euro.ZERO) { sum, slice -> sum + slice.amount }
        if (covered > result.need) Explanation("Überschuss: ${covered - result.need}.")
    }
}

@Composable
private fun LegendRow(slice: CoverageSlice) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(slice.color))
        Text(slice.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(slice.amount.toString(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
    }
}

@Composable
private fun DerivationCard(result: MaintenanceResult) {
    var open by remember { mutableStateOf(true) }
    SectionCard("Rechenweg") {
        TextButton(onClick = { open = !open }) { Text(if (open) "Ausblenden" else "Anzeigen") }
        if (open) {
            result.derivation.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(section.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    section.steps.forEach { DerivationRow(it) }
                }
            }
        }
    }
}

@Composable
private fun DerivationRow(step: Step) {
    val isTotal = step.kind == Step.Kind.TOTAL
    if (isTotal) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(
                step.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (step.legalBasis != null) {
                Text(step.legalBasis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (step.note != null) Explanation(step.note)
        }
        val sign = when (step.kind) {
            Step.Kind.ITEM -> ""
            Step.Kind.SURCHARGE -> "+ "
            Step.Kind.DEDUCTION -> "− "
            Step.Kind.TOTAL -> "= "
        }
        Text(
            "$sign${step.amount}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.End,
        )
    }
}
