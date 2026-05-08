package de.consansri.unterhaltsrechner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.core.Input
import de.consansri.unterhaltsrechner.core.Parallel
import de.consansri.unterhaltsrechner.core.Result
import de.consansri.unterhaltsrechner.core.Section
import de.consansri.unterhaltsrechner.core.ZeitraumInput
import de.consansri.unterhaltsrechner.ratio.Ratio
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.ZeitraumBetrag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator() {

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var grundbedarfEingabe by remember { mutableStateOf(ZeitraumBetrag(Euro(1000.00))) }
        var foerderungEingabe by remember { mutableStateOf(ZeitraumBetrag(Euro(0))) }
        // Für die interne Rechnungen nutzen wir konsequent die auf den Monat normierten Werte
        val unterhalt = grundbedarfEingabe - foerderungEingabe

        var studiengebuehrenEingabe by remember { mutableStateOf(ZeitraumBetrag(Euro(0))) }
        var krankenversicherungEingabe by remember { mutableStateOf(ZeitraumBetrag(Euro(0))) }

        Section("Leistungen") {

            Parallel {

                Section("Unterhalt", Modifier.weight(1f)) {
                    ZeitraumInput(
                        label = "Grundbedarf",
                        value = grundbedarfEingabe.betrag.toString(),
                        zeitraum = grundbedarfEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)?.let { e -> grundbedarfEingabe = grundbedarfEingabe.copy(betrag = e); true } ?: false
                        },
                        onZeitraumChange = { grundbedarfEingabe = grundbedarfEingabe.copy(zeitraum = it) }
                    )

                    ZeitraumInput(
                        label = "Förderung",
                        value = foerderungEingabe.betrag.toString(),
                        zeitraum = foerderungEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)?.let { e -> foerderungEingabe = foerderungEingabe.copy(betrag = e); true } ?: false
                        },
                        onZeitraumChange = { foerderungEingabe = foerderungEingabe.copy(zeitraum = it) }
                    )
                    Result("Unterhalt", unterhalt)
                }

                Section("Studiengebühren", Modifier.weight(1f)) {
                    ZeitraumInput(
                        label = "Gebühren",
                        value = studiengebuehrenEingabe.betrag.toString(),
                        zeitraum = studiengebuehrenEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)?.let { e -> studiengebuehrenEingabe = studiengebuehrenEingabe.copy(betrag = e); true } ?: false
                        },
                        onZeitraumChange = { studiengebuehrenEingabe = studiengebuehrenEingabe.copy(zeitraum = it) }
                    )
                }

                Section("Krankenversicherung", Modifier.weight(1f)) {
                    ZeitraumInput(
                        label = "Gebühren",
                        value = krankenversicherungEingabe.betrag.toString(),
                        zeitraum = krankenversicherungEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)?.let { e -> krankenversicherungEingabe = krankenversicherungEingabe.copy(betrag = e); true } ?: false
                        },
                        onZeitraumChange = { krankenversicherungEingabe = krankenversicherungEingabe.copy(zeitraum = it) }
                    )
                }
            }
        }

        val leistungen = remember(
            unterhalt,
            studiengebuehrenEingabe,
            krankenversicherungEingabe
        ) {
            Leistungen(
                unterhalt = unterhalt,
                studiengebuehren = studiengebuehrenEingabe,
                krankenversicherung = krankenversicherungEingabe
            )
        }

        Ratio(leistungen)
    }

}








