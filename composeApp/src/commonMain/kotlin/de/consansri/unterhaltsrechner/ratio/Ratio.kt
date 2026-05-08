package de.consansri.unterhaltsrechner.ratio

import androidx.compose.runtime.*
import de.consansri.unterhaltsrechner.Leistungen
import de.consansri.unterhaltsrechner.core.Parallel
import de.consansri.unterhaltsrechner.core.Section
import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Prozent
import de.consansri.unterhaltsrechner.types.Zeitraum
import de.consansri.unterhaltsrechner.types.ZeitraumBetrag

@Composable
fun Ratio(
    leistungen: Leistungen
) {

    var p1EinkommenGeglaetted by remember { mutableStateOf(ZeitraumBetrag(Euro.NULL, Zeitraum.JAHR)) }
    var p2EinkommenGeglaetted by remember { mutableStateOf(ZeitraumBetrag(Euro.NULL, Zeitraum.JAHR)) }
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

    Section("Verhältnis", valid = !ratioInvalid) {
        Parallel {
            Parent("Vater", p1Ratio, leistungen * p1Ratio) { p1EinkommenGeglaetted = it }
            Parent("Mutter", p2Ratio, leistungen * p2Ratio) { p2EinkommenGeglaetted = it }
        }
    }
}

