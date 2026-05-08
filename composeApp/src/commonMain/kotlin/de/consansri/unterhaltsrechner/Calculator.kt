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
import de.consansri.unterhaltsrechner.ratio.Ratio
import de.consansri.unterhaltsrechner.types.Euro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator() {

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var grundbedarf by remember { mutableStateOf(Euro(1000.00)) }
        var foerderung by remember { mutableStateOf(Euro(0)) }
        val unterhalt = grundbedarf - foerderung

        var studiengebuehren by remember { mutableStateOf(Euro(0)) }

        var krankenversicherung by remember { mutableStateOf(Euro(0)) }

        Section("Leistungen") {

            Parallel {

                Section("Unterhalt", Modifier.weight(1f)) {

                    Input("Grundbedarf", grundbedarf.toString()) {
                        Euro.parse(it)?.let {
                            grundbedarf = it
                            true
                        } ?: false
                    }

                    Input("Förderung", foerderung.toString()) {
                        Euro.parse(it)?.let {
                            foerderung = it
                            true
                        } ?: false
                    }

                    Result("Unterhalt", unterhalt)
                }

                Section("Studiengebühren", Modifier.weight(1f)) {

                    Input("Gebühren", studiengebuehren.toString()) {
                        Euro.parse(it)?.let {
                            studiengebuehren = it
                            true
                        } ?: false
                    }

                }

                Section("Krankenversicherung", Modifier.weight(1f)) {
                    Input("Gebühren", krankenversicherung.toString()) {
                        Euro.parse(it)?.let {
                            krankenversicherung = it
                            true
                        } ?: false
                    }
                }
            }
        }

        val leistungen = remember(
            unterhalt,
            studiengebuehren,
            krankenversicherung
        ) { 
            Leistungen(
                unterhalt = unterhalt,
                studiengebuehren = studiengebuehren,
                krankenversicherung = krankenversicherung
            )
        }

        Ratio(leistungen)
    }

}








