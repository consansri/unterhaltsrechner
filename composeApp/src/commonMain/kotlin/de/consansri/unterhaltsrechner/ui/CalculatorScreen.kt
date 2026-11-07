package de.consansri.unterhaltsrechner.ui

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
import de.consansri.unterhaltsrechner.calculation.Inputs
import de.consansri.unterhaltsrechner.calculation.LegalYear
import de.consansri.unterhaltsrechner.calculation.calculate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** From this width on, inputs and result are shown side by side. */
private val WideLayoutMinWidth = 900.dp

@Composable
internal fun CalculatorScreen(today: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }) {
    val currentYear = remember(today) { LegalYear.at(today) }
    var year by remember(currentYear) { mutableStateOf(currentYear) }
    var inputs by remember { mutableStateOf(Inputs()) }
    val result = remember(inputs, year) { calculate(inputs, year) }

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val wide = maxWidth >= WideLayoutMinWidth
        val header: @Composable () -> Unit = {
            Header(year = year, currentYear = currentYear, onYearChange = { year = it })
        }
        val inputSection: @Composable () -> Unit = {
            InputSection(inputs = inputs, year = year, onInputsChange = { inputs = it })
        }
        val output: @Composable () -> Unit = {
            ResultSection(result = result, childBenefitRecipient = inputs.childBenefitRecipient)
            SourcesCard()
        }

        if (wide) {
            Row(
                Modifier.widthIn(max = 1280.dp).fillMaxHeight().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScrollColumn(Modifier.weight(1f)) {
                    header()
                    inputSection()
                }
                // Own scroll column: the result stays in view while the inputs are edited.
                ScrollColumn(Modifier.weight(1f)) { output() }
            }
        } else {
            ScrollColumn(Modifier.widthIn(max = 720.dp).padding(horizontal = 12.dp)) {
                header()
                inputSection()
                output()
            }
        }
    }
}

@Composable
private fun ScrollColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxHeight()) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}
