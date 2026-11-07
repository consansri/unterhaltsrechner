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
import de.consansri.unterhaltsrechner.berechnung.AusbildungsAufwand
import de.consansri.unterhaltsrechner.berechnung.Eingaben
import de.consansri.unterhaltsrechner.berechnung.Elternteil
import de.consansri.unterhaltsrechner.berechnung.ElternteilEingabe
import de.consansri.unterhaltsrechner.berechnung.KindergeldEmpfaenger
import de.consansri.unterhaltsrechner.berechnung.NebenjobRegel
import de.consansri.unterhaltsrechner.core.BetragFeld
import de.consansri.unterhaltsrechner.core.Erlaeuterung
import de.consansri.unterhaltsrechner.core.HinweisArt
import de.consansri.unterhaltsrechner.core.HinweisBox
import de.consansri.unterhaltsrechner.core.Karte
import de.consansri.unterhaltsrechner.core.OptionsAuswahl
import de.consansri.unterhaltsrechner.core.SegmentAuswahl
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.types.ZBetrag

@Composable
fun EingabeBereich(
    eingaben: Eingaben,
    jahr: Jahr,
    onEingabenChange: (Eingaben) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BedarfKarte(eingaben, jahr, onEingabenChange)
        KindKarte(eingaben, jahr, onEingabenChange)
        ElternKarte(eingaben, jahr, onEingabenChange)
    }
}

/** Speichert `null`, wenn der Wert dem Tabellenwert entspricht – so folgt das Feld dem Jahr. */
private fun ueberschreibung(neu: ZBetrag, tabellenwert: Euro): ZBetrag? =
    if (neu.proMonat == tabellenwert) null else ZBetrag.monatlich(neu.betrag)

@Composable
private fun BedarfKarte(eingaben: Eingaben, jahr: Jahr, onChange: (Eingaben) -> Unit) {
    Karte("1. Bedarf des Kindes", untertitel = "Was das Kind monatlich zum Leben und Studieren braucht.") {
        BetragFeld(
            label = "Regelbedarf (Düsseldorfer Tabelle $jahr)",
            wert = eingaben.grundbedarf ?: ZBetrag.monatlich(jahr.bedarfStudent),
            onWertChange = { onChange(eingaben.copy(grundbedarf = ueberschreibung(it, jahr.bedarfStudent))) },
            zeitraumWaehlbar = false,
            tabellenwert = jahr.bedarfStudent,
            onZuruecksetzen = { onChange(eingaben.copy(grundbedarf = null)) },
            erlaeuterung = "Enthält bis ${jahr.bedarfWarmmiete} Warmmiete. Bei erhöhtem Bedarf oder mit Rücksicht " +
                "auf die Lebensstellung der Eltern kann nach oben abgewichen werden.",
        )
        BetragFeld(
            label = "Kranken- und Pflegeversicherung",
            wert = eingaben.krankenversicherung,
            onWertChange = { onChange(eingaben.copy(krankenversicherung = it)) },
            erlaeuterung = "Nur eintragen, wenn das Kind nicht beitragsfrei familienversichert ist " +
                "(z. B. ab 25 Jahren). Nicht im Regelbedarf enthalten.",
        )
        BetragFeld(
            label = "Studiengebühren",
            wert = eingaben.studiengebuehren,
            onWertChange = { onChange(eingaben.copy(studiengebuehren = it)) },
            erlaeuterung = "Studiengebühren kommen zum Regelbedarf hinzu; der normale Semesterbeitrag ist bereits enthalten.",
        )
    }
}

@Composable
private fun KindKarte(eingaben: Eingaben, jahr: Jahr, onChange: (Eingaben) -> Unit) {
    Karte(
        "2. Einkünfte des Kindes & Kindergeld",
        untertitel = "Diese Beträge decken einen Teil des Bedarfs und verringern den Unterhalt.",
    ) {
        BetragFeld(
            label = "BAföG",
            wert = eingaben.bafoeg,
            onWertChange = { onChange(eingaben.copy(bafoeg = it)) },
            erlaeuterung = "Förderungsbetrag laut Bescheid – Zuschuss und Darlehen zusammen. Wird voll angerechnet.",
        )
        Ankreuzen(
            text = "BAföG wird als Vorausleistung gezahlt (§ 36 BAföG)",
            erlaeuterung = "Dann mindert es den Unterhalt nicht: Das Land holt sich den Unterhalt von den Eltern zurück.",
            ausgewaehlt = eingaben.bafoegVorausleistung,
            onChange = { onChange(eingaben.copy(bafoegVorausleistung = it)) },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        BetragFeld(
            label = "Kindergeld",
            wert = eingaben.kindergeld ?: ZBetrag.monatlich(jahr.kindergeld),
            onWertChange = { onChange(eingaben.copy(kindergeld = ueberschreibung(it, jahr.kindergeld))) },
            zeitraumWaehlbar = false,
            tabellenwert = jahr.kindergeld,
            onZuruecksetzen = { onChange(eingaben.copy(kindergeld = null)) },
            erlaeuterung = "Bei volljährigen Kindern wird das Kindergeld voll auf den Bedarf angerechnet (§ 1612b Abs. 1 Nr. 2 BGB).",
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Kindergeld wird ausgezahlt an", style = MaterialTheme.typography.labelLarge)
            SegmentAuswahl(
                optionen = KindergeldEmpfaenger.entries,
                ausgewaehlt = eingaben.kindergeldEmpfaenger,
                onAuswahl = { onChange(eingaben.copy(kindergeldEmpfaenger = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (eingaben.kindergeldEmpfaenger.elternteil != null) {
                Erlaeuterung("Dieser Elternteil muss das Kindergeld zusätzlich zu seinem Unterhaltsanteil an das Kind weiterleiten.")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        BetragFeld(
            label = "Nebenjob / Ferienjob",
            wert = eingaben.nebenjob,
            onWertChange = { onChange(eingaben.copy(nebenjob = it)) },
            erlaeuterung = "Nettoverdienst neben dem Studium.",
        )
        if (eingaben.nebenjob.betrag != Euro.NULL) {
            OptionsAuswahl(
                optionen = NebenjobRegel.entries,
                ausgewaehlt = eingaben.nebenjobRegel,
                onAuswahl = { onChange(eingaben.copy(nebenjobRegel = it)) },
                name = { if (it == NebenjobRegel.FREI_BIS_MINIJOB) "${it.bezeichnung} (${jahr.minijobGrenze})" else it.bezeichnung },
                erlaeuterung = { it.erlaeuterung },
            )
            if (eingaben.nebenjobRegel == NebenjobRegel.EIGENER_FREIBETRAG) {
                BetragFeld(
                    label = "Anrechnungsfreier Betrag",
                    wert = eingaben.nebenjobFreibetrag,
                    onWertChange = { onChange(eingaben.copy(nebenjobFreibetrag = it)) },
                )
            }
        }

        BetragFeld(
            label = "Ausbildungsvergütung",
            wert = eingaben.ausbildungsverguetung,
            onWertChange = { onChange(eingaben.copy(ausbildungsverguetung = it)) },
            erlaeuterung = "Z. B. im dualen Studium (netto).",
        )
        if (eingaben.ausbildungsverguetung.betrag != Euro.NULL) {
            OptionsAuswahl(
                optionen = AusbildungsAufwand.entries,
                ausgewaehlt = eingaben.ausbildungsAufwand,
                onAuswahl = { onChange(eingaben.copy(ausbildungsAufwand = it)) },
                name = { it.bezeichnung },
                erlaeuterung = { it.erlaeuterung },
            )
        }

        BetragFeld(
            label = "Sonstige Einkünfte",
            wert = eingaben.sonstigeEinkuenfte,
            onWertChange = { onChange(eingaben.copy(sonstigeEinkuenfte = it)) },
            erlaeuterung = "Z. B. Waisenrente oder Kapitalerträge. Werden voll angerechnet.",
        )
    }
}

@Composable
private fun ElternKarte(eingaben: Eingaben, jahr: Jahr, onChange: (Eingaben) -> Unit) {
    Karte("3. Einkommen der Eltern", untertitel = "Die Eltern teilen den Restbedarf im Verhältnis ihrer Einkommen.") {
        HinweisBox(
            titel = "Aus dem BAföG-Bescheid übernehmen",
            text = "Einzutragen ist das im Bescheid ausgewiesene Einkommen jedes Elternteils (nach Abzug von Steuern " +
                "und Sozialpauschale, meist als Jahresbetrag). Der Bescheid beruht auf dem vorletzten Kalenderjahr und " +
                "auf Pauschalen – das unterhaltsrechtlich bereinigte Einkommen kann davon abweichen.",
            art = HinweisArt.INFO,
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val nebeneinander = maxWidth >= 560.dp
            val karten: @Composable (Modifier) -> Unit = { m ->
                Elternteil.entries.forEach { e ->
                    ElternteilFelder(
                        elternteil = e,
                        eingabe = eingaben.elternteil(e),
                        jahr = jahr,
                        onChange = { onChange(eingaben.mitElternteil(e, it)) },
                        modifier = m,
                    )
                }
            }
            if (nebeneinander) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { karten(Modifier.weight(1f)) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { karten(Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun ElternteilFelder(
    elternteil: Elternteil,
    eingabe: ElternteilEingabe,
    jahr: Jahr,
    onChange: (ElternteilEingabe) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hatZusatz = eingabe.abzuege.betrag != Euro.NULL || eingabe.warmmiete.betrag != Euro.NULL
    var zusatzOffen by remember { mutableStateOf(hatZusatz) } // initial-only

    OutlinedCard(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(elternteil.bezeichnung, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            BetragFeld(
                label = "Bereinigtes Nettoeinkommen",
                wert = eingabe.einkommen,
                onWertChange = { onChange(eingabe.copy(einkommen = it)) },
                erlaeuterung = "Ersatzweise: Einkommen laut BAföG-Bescheid.",
            )
            // Felder mit eingetragenen Werten bleiben sichtbar, damit nichts verdeckt in die Berechnung eingeht.
            if (!hatZusatz) {
                TextButton(onClick = { zusatzOffen = !zusatzOffen }) {
                    Text(if (zusatzOffen) "Weitere Angaben ausblenden" else "Weitere Angaben (optional)")
                }
            }
            if (zusatzOffen || hatZusatz) {
                BetragFeld(
                    label = "Vorrangige Unterhaltspflichten",
                    wert = eingabe.abzuege,
                    onWertChange = { onChange(eingabe.copy(abzuege = it)) },
                    erlaeuterung = "Z. B. Unterhalt für minderjährige Geschwister – er geht dem Unterhalt des " +
                        "studierenden Kindes vor (§ 1609 BGB).",
                )
                BetragFeld(
                    label = "Eigene Warmmiete",
                    wert = eingabe.warmmiete,
                    onWertChange = { onChange(eingabe.copy(warmmiete = it)) },
                    erlaeuterung = "Nur relevant, wenn sie über ${jahr.selbstbehaltWarmmiete} liegt und angemessen ist: " +
                        "Dann erhöht sich der Selbstbehalt (${jahr.selbstbehalt}) um die Differenz.",
                )
            }
        }
    }
}

@Composable
private fun Ankreuzen(text: String, erlaeuterung: String, ausgewaehlt: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .toggleable(value = ausgewaehlt, role = Role.Checkbox, onValueChange = onChange),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = ausgewaehlt, onCheckedChange = null, modifier = Modifier.padding(8.dp))
        Column(Modifier.padding(vertical = 6.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            if (ausgewaehlt) Erlaeuterung(erlaeuterung)
        }
    }
}
