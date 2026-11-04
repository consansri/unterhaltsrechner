package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.LocalSignalfarben

/** Karte mit Überschrift für einen Abschnitt der Seite. */
@Composable
fun Karte(
    titel: String,
    modifier: Modifier = Modifier,
    untertitel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (untertitel != null) Erlaeuterung(untertitel)
            }
            content()
        }
    }
}

/** Kleiner, zurückhaltender Erklärungstext, optional mit Rechtsgrundlage. */
@Composable
fun Erlaeuterung(text: String, modifier: Modifier = Modifier, grundlage: String? = null) {
    Text(
        text = if (grundlage != null) "$text ($grundlage)" else text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

enum class HinweisArt { INFO, WARNUNG, FEHLER }

/** Farbig hinterlegter Hinweis (Info, Warnung, Fehler). */
@Composable
fun HinweisBox(text: String, art: HinweisArt, modifier: Modifier = Modifier, titel: String? = null) {
    val signal = LocalSignalfarben.current
    val (hintergrund, vordergrund) = when (art) {
        HinweisArt.INFO -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HinweisArt.WARNUNG -> signal.warnungContainer to signal.onWarnungContainer
        HinweisArt.FEHLER -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(hintergrund)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (titel != null) {
            Text(titel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = vordergrund)
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = vordergrund)
    }
}

/** Segmentierte Auswahl für wenige, kurze Optionen (z. B. Jahr, Zeitraum). */
@Composable
fun <T> SegmentAuswahl(
    optionen: List<T>,
    ausgewaehlt: T,
    onAuswahl: (T) -> Unit,
    modifier: Modifier = Modifier,
    name: (T) -> String = { it.toString() },
) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        optionen.forEach { option ->
            val aktiv = option == ausgewaehlt
            val hintergrund by animateColorAsState(if (aktiv) MaterialTheme.colorScheme.primary else Color.Transparent)
            val schrift by animateColorAsState(
                if (aktiv) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = name(option),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (aktiv) FontWeight.SemiBold else FontWeight.Normal,
                color = schrift,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(hintergrund)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .selectable(selected = aktiv, role = Role.RadioButton) { onAuswahl(option) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

/** Auswahlliste mit Radiobuttons für Optionen, die eine Erklärung brauchen. */
@Composable
fun <T> OptionsAuswahl(
    optionen: List<T>,
    ausgewaehlt: T,
    onAuswahl: (T) -> Unit,
    name: (T) -> String,
    erlaeuterung: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.selectableGroup()) {
        optionen.forEach { option ->
            val aktiv = option == ausgewaehlt
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .selectable(selected = aktiv, role = Role.RadioButton) { onAuswahl(option) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RadioButton(selected = aktiv, onClick = null, modifier = Modifier.padding(8.dp))
                Column(Modifier.padding(top = 6.dp, bottom = 6.dp, end = 8.dp)) {
                    Text(name(option), style = MaterialTheme.typography.bodyMedium)
                    if (aktiv) Erlaeuterung(erlaeuterung(option))
                }
            }
        }
    }
}
