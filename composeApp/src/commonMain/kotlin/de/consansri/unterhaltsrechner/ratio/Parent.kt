package de.consansri.unterhaltsrechner.ratio

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.consansri.unterhaltsrechner.Leistungen
import de.consansri.unterhaltsrechner.core.HLine
import de.consansri.unterhaltsrechner.core.Result
import de.consansri.unterhaltsrechner.core.Section
import de.consansri.unterhaltsrechner.core.ZeitraumInput
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Prozent
import de.consansri.unterhaltsrechner.types.Zeitraum
import de.consansri.unterhaltsrechner.types.ZeitraumBetrag

@Composable
fun RowScope.Parent(
    name: String,
    ratio: Prozent,
    leistungen: Leistungen,
    onEinkommenChange: (ZeitraumBetrag) -> Unit
) {

    var einkommenEingabe by remember { mutableStateOf(ZeitraumBetrag(Euro.NULL, Zeitraum.JAHR)) }

    Section(name, modifier = Modifier.weight(1f)) {
        ZeitraumInput(
            label = "Einkommen",
            value = einkommenEingabe.betrag.toString(),
            zeitraum = einkommenEingabe.zeitraum,
            onValueChange = {
                Euro.parse(it)?.let { e -> einkommenEingabe = einkommenEingabe.copy(betrag = e); true } ?: false
            },
            onZeitraumChange = { einkommenEingabe = einkommenEingabe.copy(zeitraum = it) }
        )

        Result("Anteil", ratio, "%")

        HLine()

        Result("Unterhalt", leistungen.unterhalt)
        Result("Studiengebühren", leistungen.studiengebuehren)
        Result("Krankenversicherung", leistungen.krankenversicherung)

        HLine()

        Result("Gesamt", leistungen.sum)
    }

    LaunchedEffect(einkommenEingabe) {
        // Formel für Einkommensglättung
        onEinkommenChange(einkommenEingabe)
    }

}