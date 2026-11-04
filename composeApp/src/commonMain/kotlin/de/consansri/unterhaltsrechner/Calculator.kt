package de.consansri.unterhaltsrechner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.berechnung.Eingaben
import de.consansri.unterhaltsrechner.berechnung.berechne
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.ui.EingabeBereich
import de.consansri.unterhaltsrechner.ui.ErgebnisBereich
import de.consansri.unterhaltsrechner.ui.Kopfzeile
import de.consansri.unterhaltsrechner.ui.QuellenUndHinweise
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Ab dieser Breite stehen Eingaben und Ergebnis nebeneinander. */
private val BREIT = 900.dp

@Composable
fun Calculator(heute: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }) {
    val aktuellesJahr = remember(heute) { Jahr.fuer(heute) }
    var jahr by remember(aktuellesJahr) { mutableStateOf(aktuellesJahr) }
    var eingaben by remember { mutableStateOf(Eingaben()) }
    val ergebnis = remember(eingaben, jahr) { berechne(eingaben, jahr) }

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val breit = maxWidth >= BREIT
        val kopf: @Composable () -> Unit = {
            Kopfzeile(jahr = jahr, aktuellesJahr = aktuellesJahr, heute = heute, onJahrChange = { jahr = it })
        }
        val eingabe: @Composable () -> Unit = {
            EingabeBereich(eingaben = eingaben, jahr = jahr, onEingabenChange = { eingaben = it })
        }
        val ausgabe: @Composable () -> Unit = {
            ErgebnisBereich(ergebnis = ergebnis, kindergeldEmpfaenger = eingaben.kindergeldEmpfaenger)
            QuellenUndHinweise()
        }

        if (breit) {
            Row(
                Modifier.widthIn(max = 1280.dp).fillMaxHeight().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spalte(Modifier.weight(1f)) {
                    kopf()
                    eingabe()
                }
                // Eigene Scroll-Spalte: das Ergebnis bleibt beim Bearbeiten der Eingaben im Blick.
                Spalte(Modifier.weight(1f)) { ausgabe() }
            }
        } else {
            Spalte(Modifier.widthIn(max = 720.dp).padding(horizontal = 12.dp)) {
                kopf()
                eingabe()
                ausgabe()
            }
        }
    }
}

@Composable
private fun Spalte(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxHeight()) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}
