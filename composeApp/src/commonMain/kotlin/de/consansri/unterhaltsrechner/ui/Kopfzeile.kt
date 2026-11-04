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
import de.consansri.unterhaltsrechner.core.Erlaeuterung
import de.consansri.unterhaltsrechner.core.HinweisArt
import de.consansri.unterhaltsrechner.core.HinweisBox
import de.consansri.unterhaltsrechner.core.Karte
import de.consansri.unterhaltsrechner.core.SegmentAuswahl
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.types.Rechtsstatus
import de.consansri.unterhaltsrechner.types.alsDatum
import kotlinx.datetime.LocalDate

@Composable
fun Kopfzeile(
    jahr: Jahr,
    aktuellesJahr: Jahr,
    heute: LocalDate,
    onJahrChange: (Jahr) -> Unit,
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

        Karte("Rechtsstand") {
            SegmentAuswahl(
                optionen = Jahr.entries,
                ausgewaehlt = jahr,
                onAuswahl = onJahrChange,
                modifier = Modifier.fillMaxWidth(),
            )
            val status = when (jahr.status) {
                Rechtsstatus.AMTLICH -> "amtlich"
                Rechtsstatus.VORLAEUFIG -> "vorläufig"
            }
            Text(
                "Düsseldorfer Tabelle $jahr · gültig ab ${jahr.gueltigAb.alsDatum()} · $status",
                style = MaterialTheme.typography.bodyMedium,
            )
            Erlaeuterung(
                "Bedarf ${jahr.bedarfStudent} · Kindergeld ${jahr.kindergeld} · " +
                    "Selbstbehalt der Eltern ${jahr.selbstbehalt} · Minijob-Grenze ${jahr.minijobGrenze}",
            )
            if (jahr != aktuellesJahr) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Erlaeuterung(
                        "Heute (${heute.alsDatum()}) gilt der Rechtsstand $aktuellesJahr.",
                        Modifier.weight(1f),
                    )
                    TextButton(onClick = { onJahrChange(aktuellesJahr) }) { Text("Zum aktuellen Stand") }
                }
            }
            if (jahr.status == Rechtsstatus.VORLAEUFIG && jahr.hinweis != null) {
                HinweisBox(jahr.hinweis, HinweisArt.WARNUNG, titel = "Vorläufige Werte")
            }
        }
    }
}
