package de.consansri.unterhaltsrechner.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.ZBetrag
import de.consansri.unterhaltsrechner.types.Zeitraum

/**
 * Eingabefeld für einen Euro-Betrag mit Zeitraum.
 *
 * Der eingegebene Text wird lokal gehalten: Zwischenstände wie `12,` oder ein leeres Feld
 * bleiben beim Tippen erhalten. Gültige Eingaben werden sofort übernommen, ungültige werden
 * erklärt – bis zur Korrektur rechnet die App mit dem letzten gültigen Wert weiter.
 *
 * @param tabellenwert Monatlicher Tabellenwert; weicht [wert] davon ab, wird er mit
 *   „zurücksetzen“ angezeigt.
 * @param zeitraumWaehlbar `false` für Beträge, die immer monatlich sind (z. B. Tabellenwerte).
 */
@Composable
fun BetragFeld(
    label: String,
    wert: ZBetrag,
    onWertChange: (ZBetrag) -> Unit,
    modifier: Modifier = Modifier,
    erlaeuterung: String? = null,
    tabellenwert: Euro? = null,
    onZuruecksetzen: (() -> Unit)? = null,
    zeitraumWaehlbar: Boolean = true,
) {
    var text by remember { mutableStateOf(wert.betrag.alsEingabe()) } // initial-only, danach per Effekt synchronisiert
    val pruefung = Euro.pruefe(text)

    // Änderungen von außen (Zurücksetzen, Jahreswechsel) übernehmen, ohne laufende Eingaben zu überschreiben.
    LaunchedEffect(wert.betrag) {
        val aktuell = (Euro.pruefe(text) as? Euro.Eingabe.Gueltig)?.wert
        if (aktuell != wert.betrag) text = wert.betrag.alsEingabe()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            if (zeitraumWaehlbar) {
                SegmentAuswahl(
                    optionen = Zeitraum.entries,
                    ausgewaehlt = wert.zeitraum,
                    onAuswahl = { onWertChange(wert.copy(zeitraum = it)) },
                    name = { if (it == Zeitraum.SEMESTER) "Sem." else it.bezeichnung },
                    modifier = Modifier.widthIn(max = 210.dp),
                )
            } else {
                Erlaeuterung("pro Monat")
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { neu ->
                text = neu
                val ergebnis = Euro.pruefe(neu)
                if (ergebnis is Euro.Eingabe.Gueltig && ergebnis.wert != wert.betrag) {
                    onWertChange(wert.copy(betrag = ergebnis.wert))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { fokus ->
                    // Beim Verlassen des Feldes einheitlich formatieren (z. B. "1.234,5" → "1234,50").
                    if (!fokus.isFocused && Euro.pruefe(text) is Euro.Eingabe.Gueltig) text = wert.betrag.alsEingabe()
                },
            singleLine = true,
            isError = pruefung is Euro.Eingabe.Ungueltig,
            suffix = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        when (pruefung) {
            is Euro.Eingabe.Ungueltig -> Text(
                "${pruefung.grund} Gerechnet wird weiter mit ${wert.betrag}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            is Euro.Eingabe.Gueltig -> if (wert.zeitraum != Zeitraum.MONAT && wert.betrag != Euro.NULL) {
                Text(
                    "entspricht ${wert.proMonat} pro Monat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (tabellenwert != null && onZuruecksetzen != null && wert.proMonat != tabellenwert) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Erlaeuterung("Eigener Wert – Tabellenwert: $tabellenwert", Modifier.weight(1f, fill = false))
                TextButton(onClick = onZuruecksetzen) { Text("zurücksetzen") }
            }
        }

        if (erlaeuterung != null) Erlaeuterung(erlaeuterung)
    }
}
