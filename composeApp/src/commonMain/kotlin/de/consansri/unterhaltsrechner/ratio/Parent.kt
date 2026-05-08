package de.consansri.unterhaltsrechner.ratio

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.consansri.unterhaltsrechner.Leistungen
import de.consansri.unterhaltsrechner.core.HLine
import de.consansri.unterhaltsrechner.core.Input
import de.consansri.unterhaltsrechner.core.Result
import de.consansri.unterhaltsrechner.core.Section
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Prozent

@Composable
fun RowScope.Parent(
    name: String,
    ratio: Prozent,
    leistungen: Leistungen,
    onEinkommenChange: (Euro) -> Unit
) {

    var einkommen by remember { mutableStateOf(Euro(0)) }

    Section(name, modifier = Modifier.weight(1f)) {
        Input("Einkommen", einkommen.toString()) {
            Euro.parse(it)?.let {
                einkommen = it
                true
            } ?: false
        }

        Result("Anteil", ratio, "%")

        HLine()

        Result("Unterhalt", leistungen.unterhalt)
        Result("Studiengebühren", leistungen.studiengebuehren)
        Result("Krankenversicherung", leistungen.krankenversicherung)

        HLine()

        Result("Gesamt", leistungen.sum)
    }

    LaunchedEffect(einkommen) {
        // Formel für Einkommensglättung
        onEinkommenChange(einkommen)
    }

}