package de.consansri.unterhaltsrechner.ratio

import androidx.compose.runtime.*
import de.consansri.unterhaltsrechner.Leistungen
import de.consansri.unterhaltsrechner.core.Descr
import de.consansri.unterhaltsrechner.core.Parallel
import de.consansri.unterhaltsrechner.core.Section
import de.consansri.unterhaltsrechner.core.ZBetragInput
import de.consansri.unterhaltsrechner.types.*

@Composable
fun Deckung(leistungen: Leistungen, jahr: Jahr) {

    var p1EinkommenGeglaetted by remember { mutableStateOf(ZBetrag(Euro.NULL, Zeitraum.JAHR)) }
    var p2EinkommenGeglaetted by remember { mutableStateOf(ZBetrag(Euro.NULL, Zeitraum.JAHR)) }
    val combined = remember(p1EinkommenGeglaetted, p2EinkommenGeglaetted) {
        p1EinkommenGeglaetted + p2EinkommenGeglaetted
    }
    val ratioInvalid = remember(combined) {
        combined.proMonat == Euro.NULL
    }

    val p1Ratio = remember(p1EinkommenGeglaetted, combined) {
        (p1EinkommenGeglaetted / combined) ?: Prozent(0)
    }
    val p2Ratio = remember(p2EinkommenGeglaetted, combined) {
        (p2EinkommenGeglaetted / combined) ?: Prozent(0)
    }

    var kindergeldEingabe by remember(jahr) { mutableStateOf(jahr.kindergeld) }

    Section("Durch Kindergeld") {
        ZBetragInput(
            label = "Kindergeld",
            value = kindergeldEingabe.betrag.toString(),
            zeitraum = kindergeldEingabe.zeitraum,
            onValueChange = {
                Euro.parse(it)?.let { e -> kindergeldEingabe = kindergeldEingabe.copy(betrag = e); true } ?: false
            },
            onZeitraumChange = { kindergeldEingabe = kindergeldEingabe.copy(zeitraum = it) }
        )

        Descr(
            "BGB § 1612b", """
            Das Kindergeld muss allen Elternteilen gleichermaßen angerechnet werden.
        """.trimIndent()
        )
    }

    Section("Durch Eltern", valid = !ratioInvalid) {

        Descr(
            "BGB § 1606 (3)", """
            Mehrere gleich nahe Verwandte haften anteilig nach ihren Erwerbs- und Vermögensverhältnissen. (...)
        """.trimIndent()
        )

        Parallel {
            Parent(
                "Vater",
                p1Ratio,
                leistungen * p1Ratio,
                kindergeldEingabe * Prozent(0.5)
            ) {
                p1EinkommenGeglaetted = it
            }
            Parent(
                "Mutter",
                p2Ratio,
                leistungen * p2Ratio,
                kindergeldEingabe * Prozent(0.5)
            ) {
                p2EinkommenGeglaetted = it
            }
        }
    }
}

