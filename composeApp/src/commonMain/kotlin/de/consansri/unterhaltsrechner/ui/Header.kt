package de.consansri.unterhaltsrechner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.calculation.LegalYear
import de.consansri.unterhaltsrechner.ui.components.Callout
import de.consansri.unterhaltsrechner.ui.components.CalloutTone
import de.consansri.unterhaltsrechner.ui.components.Explanation
import de.consansri.unterhaltsrechner.ui.components.SectionCard
import de.consansri.unterhaltsrechner.ui.components.SegmentedPicker

@Composable
internal fun Header(
    year: LegalYear,
    currentYear: LegalYear,
    onYearChange: (LegalYear) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Unterhaltsrechner", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Kindesunterhalt für volljährige Studierende mit eigenem Haushalt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionCard("Rechtsstand") {
            SegmentedPicker(
                options = LegalYear.entries,
                selected = year,
                onSelect = onYearChange,
                modifier = Modifier.fillMaxWidth(),
            )
            val statusLabel = when (year.status) {
                LegalYear.Status.Official -> "amtlich"
                is LegalYear.Status.Provisional -> "vorläufig"
            }
            Text(
                "Düsseldorfer Tabelle $year · ab ${year.validFrom.formatGerman()} · $statusLabel",
                style = MaterialTheme.typography.bodyMedium,
            )
            Explanation(
                "Bedarf ${year.studentNeed} · Kindergeld ${year.childBenefit} · " +
                    "Selbstbehalt ${year.selfReserve} · Minijob-Grenze ${year.minijobLimit}",
            )
            if (year != currentYear) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Explanation("Heute gilt der Rechtsstand $currentYear.", Modifier.weight(1f))
                    TextButton(onClick = { onYearChange(currentYear) }) { Text("Zu $currentYear") }
                }
            }
            when (val status = year.status) {
                LegalYear.Status.Official -> Unit
                is LegalYear.Status.Provisional -> Callout(status.note, CalloutTone.WARNING, title = "Vorläufige Werte")
            }
        }
    }
}
