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
import de.consansri.unterhaltsrechner.LocalSignalfarben
import de.consansri.unterhaltsrechner.berechnung.ElternteilErgebnis
import de.consansri.unterhaltsrechner.berechnung.Ergebnis
import de.consansri.unterhaltsrechner.berechnung.Hinweis
import de.consansri.unterhaltsrechner.berechnung.KindergeldEmpfaenger
import de.consansri.unterhaltsrechner.berechnung.Schritt
import de.consansri.unterhaltsrechner.core.Erlaeuterung
import de.consansri.unterhaltsrechner.core.HinweisArt
import de.consansri.unterhaltsrechner.core.HinweisBox
import de.consansri.unterhaltsrechner.core.Karte
import de.consansri.unterhaltsrechner.types.Euro

@Composable
fun ErgebnisBereich(
    ergebnis: Ergebnis,
    kindergeldEmpfaenger: KindergeldEmpfaenger,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ErgebnisKarte(ergebnis, kindergeldEmpfaenger)
        RechenwegKarte(ergebnis)
    }
}

@Composable
private fun ErgebnisKarte(ergebnis: Ergebnis, kindergeldEmpfaenger: KindergeldEmpfaenger) {
    Karte("Ergebnis", untertitel = "Monatlicher Unterhalt nach Rechtsstand ${ergebnis.jahr}") {
        ergebnis.hinweise.forEach { HinweisAnzeige(it) }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ergebnis.eltern.forEach { ElternteilErgebnisKarte(it, Modifier.weight(1f)) }
        }

        Bilanz(ergebnis, kindergeldEmpfaenger)
    }
}

@Composable
private fun HinweisAnzeige(hinweis: Hinweis) {
    when (hinweis) {
        is Hinweis.VorlaeufigeWerte -> HinweisBox(hinweis.text, HinweisArt.WARNUNG, titel = "Vorläufige Werte")
        Hinweis.BedarfGedeckt -> HinweisBox(
            "Die eigenen Einkünfte des Kindes und das Kindergeld decken den Bedarf vollständig. " +
                "Die Eltern schulden keinen Barunterhalt.",
            HinweisArt.INFO,
            titel = "Bedarf gedeckt",
        )
        Hinweis.EinkommenFehlt -> HinweisBox(
            "Tragen Sie unter „3. Einkommen der Eltern“ die Einkommen aus dem BAföG-Bescheid ein, " +
                "um die Anteile von Vater und Mutter zu berechnen.",
            HinweisArt.INFO,
            titel = "Einkommen der Eltern fehlt",
        )
        Hinweis.KeinElternteilLeistungsfaehig -> HinweisBox(
            "Kein Elternteil verdient mehr als den angemessenen Selbstbehalt. " +
                "Ohne einsetzbares Einkommen besteht keine Unterhaltspflicht.",
            HinweisArt.WARNUNG,
            titel = "Keine Leistungsfähigkeit",
        )
        is Hinweis.Mangelfall -> HinweisBox(
            "Die Eltern können den Restbedarf auch zusammen nicht vollständig decken. Jeder zahlt höchstens sein " +
                "Einkommen über dem Selbstbehalt; ${hinweis.fehlbetrag} pro Monat bleiben ungedeckt.",
            HinweisArt.FEHLER,
            titel = "Mangelfall",
        )
    }
}

@Composable
private fun ElternteilErgebnisKarte(e: ElternteilErgebnis, modifier: Modifier = Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${e.elternteil} zahlt", style = MaterialTheme.typography.labelLarge)
            Text(e.zahlbetrag.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("pro Monat", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            ZeileKlein("Unterhaltsanteil", e.unterhalt.toString())
            ZeileKlein("Haftungsquote", e.quote?.toString() ?: "–")
            if (e.kindergeldWeiterleiten != Euro.NULL) {
                ZeileKlein("+ Kindergeld weiterleiten", e.kindergeldWeiterleiten.toString())
            }
        }
    }
}

@Composable
private fun ZeileKlein(bezeichnung: String, wert: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(bezeichnung, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(wert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

/** Zeigt, aus welchen Quellen der Bedarf des Kindes gedeckt wird – als Balken und als Legende. */
@Composable
private fun Bilanz(ergebnis: Ergebnis, kindergeldEmpfaenger: KindergeldEmpfaenger) {
    val farben = MaterialTheme.colorScheme
    val signal = LocalSignalfarben.current
    val teile = listOf(
        Triple("Unterhalt ${ergebnis.vater.elternteil}", ergebnis.vater.unterhalt, farben.primary),
        Triple("Unterhalt ${ergebnis.mutter.elternteil}", ergebnis.mutter.unterhalt, farben.tertiary),
        Triple(
            "Kindergeld" + (kindergeldEmpfaenger.elternteil?.let { " (über $it)" } ?: ""),
            ergebnis.kindergeld,
            farben.secondary,
        ),
        Triple("Angerechnete Einkünfte des Kindes", ergebnis.einkommenKind, farben.outline),
        Triple("Ungedeckt", ergebnis.fehlbetrag, signal.warnung),
    ).filter { it.second > Euro.NULL }
    if (teile.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("So wird der Bedarf von ${ergebnis.bedarf} gedeckt", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
            teile.forEach { (_, betrag, farbe) ->
                Box(Modifier.weight(betrag.inCents.toFloat()).fillMaxHeight().background(farbe))
            }
        }
        teile.forEach { (name, betrag, farbe) -> Legende(name, betrag, farbe) }
        val summe = teile.fold(Euro.NULL) { acc, t -> acc + t.second }
        if (summe > ergebnis.bedarf) {
            Erlaeuterung("Einkünfte und Kindergeld übersteigen den Bedarf um ${summe - ergebnis.bedarf}.")
        }
    }
}

@Composable
private fun Legende(name: String, betrag: Euro, farbe: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(farbe))
        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(betrag.toString(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
    }
}

@Composable
private fun RechenwegKarte(ergebnis: Ergebnis) {
    var offen by remember { mutableStateOf(true) }
    Karte("Rechenweg", untertitel = "Jeder Schritt der Berechnung mit Rechtsgrundlage.") {
        TextButton(onClick = { offen = !offen }) { Text(if (offen) "Rechenweg ausblenden" else "Rechenweg anzeigen") }
        if (offen) {
            ergebnis.rechenweg.forEach { abschnitt ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(abschnitt.titel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    abschnitt.schritte.forEach { RechenwegZeile(it) }
                }
            }
        }
    }
}

@Composable
private fun RechenwegZeile(schritt: Schritt) {
    val summe = schritt.art == Schritt.Art.SUMME
    if (summe) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(
                schritt.bezeichnung,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (summe) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (schritt.grundlage != null) {
                Text(schritt.grundlage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (schritt.erlaeuterung != null) Erlaeuterung(schritt.erlaeuterung)
        }
        val vorzeichen = when (schritt.art) {
            Schritt.Art.POSITION -> ""
            Schritt.Art.ZUSCHLAG -> "+ "
            Schritt.Art.ABZUG -> "− "
            Schritt.Art.SUMME -> "= "
        }
        Text(
            "$vorzeichen${schritt.betrag}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (summe) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun QuellenUndHinweise(modifier: Modifier = Modifier) {
    Karte("Hinweise & Quellen", modifier) {
        Erlaeuterung(
            "Dieser Rechner bildet den Regelfall nach der Düsseldorfer Tabelle und den Unterhaltsleitlinien der " +
                "Oberlandesgerichte ab. Er ersetzt keine Rechtsberatung: Gerichte können im Einzelfall abweichen, " +
                "etwa bei Sonderbedarf, Vermögen des Kindes oder anderen Einkommensverhältnissen.",
        )
        Erlaeuterung(
            "Rechtsgrundlagen: §§ 1601, 1603, 1606 Abs. 3, 1609, 1610, 1612b BGB · Düsseldorfer Tabelle (OLG Düsseldorf) · " +
                "Unterhaltsleitlinien der OLG (Nr. 2.4, 10.2.3, 13.1–13.3) · § 1577 Abs. 2 BGB entsprechend für " +
                "Nebenverdienste im Studium.",
        )
    }
}
