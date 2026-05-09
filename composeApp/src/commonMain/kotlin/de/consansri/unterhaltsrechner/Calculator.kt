package de.consansri.unterhaltsrechner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.core.*
import de.consansri.unterhaltsrechner.ratio.Deckung
import de.consansri.unterhaltsrechner.types.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator() {

    var jahr by remember { mutableStateOf(Jahr.J2026) }

    var grundbedarfEingabe by remember(jahr) { mutableStateOf(jahr.grundbedarf.sum) }
    var foerderungEingabe by remember { mutableStateOf(ZBetrag(Euro(0))) }
    var einkommenEingabe by remember { mutableStateOf(ZBetrag(Euro(0))) }
    var einkommensregelung by remember { mutableStateOf(Einkommensregelung.NICHTS) }
    // Für die interne Rechnungen nutzen wir konsequent die auf den Monat normierten Werte
    val unterhalt =
        (grundbedarfEingabe - foerderungEingabe - einkommensregelung.calc(einkommenEingabe, jahr)).min(ZBetrag.NULL)

    var studiengebuehrenEingabe by remember { mutableStateOf(ZBetrag.NULL.copy(zeitraum = Zeitraum.SEMESTER)) }
    var krankenversicherungEingabe by remember { mutableStateOf(ZBetrag.NULL) }

    val scrollState = rememberScrollState()

    Column(
        Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        EnumSelector(jahr) { jahr = it }

        Section("Bedarf") {

            Descr(
                "§ 1610 Maß des Unterhalts", """
                (1) Das Maß des zu gewährenden Unterhalts bestimmt sich nach der Lebensstellung des Bedürftigen (angemessener Unterhalt).
                (2) Der Unterhalt umfasst den gesamten Lebensbedarf einschließlich der Kosten einer angemessenen Vorbildung zu einem Beruf, 
                    bei einer der Erziehung bedürftigen Person auch die Kosten der Erziehung.
            """.trimIndent()
            )

            Parallel {

                Section("Unterhalt", Modifier.weight(1f)) {

                    Descr(
                        "DT $jahr",
                        """
                        Grundbedarf (ohne KV und Studiengebühren): ${jahr.grundbedarf.sum} = ${jahr.grundbedarf.wohnpauschale} (wohnpauschale) + ${jahr.grundbedarf.sonstiges} (sonstiges)
                    """.trimIndent()
                    )

                    ZBetragInput(
                        label = "Grundbedarf",
                        value = grundbedarfEingabe.betrag.toString(),
                        zeitraum = grundbedarfEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)
                                ?.let { e -> grundbedarfEingabe = grundbedarfEingabe.copy(betrag = e); true }
                                ?: false
                        },
                        onZeitraumChange = { grundbedarfEingabe = grundbedarfEingabe.copy(zeitraum = it) }
                    )

                    ZBetragInput(
                        label = "- Förderung (BAFÖG)",
                        value = foerderungEingabe.betrag.toString(),
                        zeitraum = foerderungEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)
                                ?.let { e -> foerderungEingabe = foerderungEingabe.copy(betrag = e); true }
                                ?: false
                        },
                        onZeitraumChange = { foerderungEingabe = foerderungEingabe.copy(zeitraum = it) }
                    )

                    ZBetragInput(
                        label = "- Einnahmen",
                        value = einkommenEingabe.betrag.toString(),
                        zeitraum = einkommenEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)
                                ?.let { e -> einkommenEingabe = einkommenEingabe.copy(betrag = e); true }
                                ?: false
                        },
                        onZeitraumChange = { einkommenEingabe = einkommenEingabe.copy(zeitraum = it) }
                    )

                    EnumSelector(einkommensregelung, name = {
                        it.uiName(jahr)
                    }) { einkommensregelung = it }

                    HLine()

                    Result("Unterhalt", unterhalt)
                }

                Section("Studium", Modifier.weight(1f)) {

                    Spacer(Modifier.weight(1f))

                    HLine()

                    ZBetragInput(
                        label = "Gebühren",
                        value = studiengebuehrenEingabe.betrag.toString(),
                        zeitraum = studiengebuehrenEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)
                                ?.let { e ->
                                    studiengebuehrenEingabe = studiengebuehrenEingabe.copy(betrag = e); true
                                }
                                ?: false
                        },
                        onZeitraumChange = { studiengebuehrenEingabe = studiengebuehrenEingabe.copy(zeitraum = it) }
                    )
                }

                Section("Versicherung", Modifier.weight(1f)) {

                    Spacer(Modifier.weight(1f))

                    HLine()
                    ZBetragInput(
                        label = "Gebühren",
                        value = krankenversicherungEingabe.betrag.toString(),
                        zeitraum = krankenversicherungEingabe.zeitraum,
                        onValueChange = {
                            Euro.parse(it)?.let { e ->
                                krankenversicherungEingabe = krankenversicherungEingabe.copy(betrag = e); true
                            } ?: false
                        },
                        onZeitraumChange = {
                            krankenversicherungEingabe = krankenversicherungEingabe.copy(zeitraum = it)
                        }
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

        Deckung(leistungen, jahr)

    }

}








