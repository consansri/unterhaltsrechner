package de.consansri.unterhaltsrechner.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.consansri.unterhaltsrechner.ui.components.Explanation
import de.consansri.unterhaltsrechner.ui.components.SectionCard

@Composable
internal fun SourcesCard(modifier: Modifier = Modifier) {
    SectionCard("Hinweise & Quellen", modifier) {
        Explanation("Regelfall nach Düsseldorfer Tabelle und OLG-Leitlinien. Keine Rechtsberatung; Gerichte können abweichen.")
        Explanation(
            "§§ 1601, 1603, 1606 Abs. 3, 1609, 1610, 1612b BGB, § 1577 Abs. 2 BGB entspr. (Nebenverdienst) · " +
                "Düsseldorfer Tabelle · Unterhaltsleitlinien der OLG (Nr. 2.4, 10.2.3, 13.1–13.3)",
        )
    }
}
