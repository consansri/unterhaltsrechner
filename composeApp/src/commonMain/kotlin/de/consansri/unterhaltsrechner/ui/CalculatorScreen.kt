package de.consansri.unterhaltsrechner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.calculation.Inputs
import de.consansri.unterhaltsrechner.calculation.LegalYear
import de.consansri.unterhaltsrechner.calculation.calculate
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Layout state that is only read by [derivedStateOf]; the bounds are relative to the scrolled content. */
private class ResultBounds {
    var content: LayoutCoordinates? = null
    var top by mutableIntStateOf(UNKNOWN)
    var bottom by mutableIntStateOf(UNKNOWN)

    fun onResultPlaced(result: LayoutCoordinates) {
        val content = content?.takeIf { it.isAttached } ?: return
        if (!result.isAttached) return
        val position = content.localPositionOf(result, Offset.Zero)
        top = position.y.toInt()
        bottom = top + result.size.height
    }

    companion object {
        const val UNKNOWN = -1
    }
}

/**
 * The page scrolls as one. The inputs and the result are two panes in a [FlowRow], which puts them side by side
 * as soon as both fit and stacks them otherwise. While the result card is out of view, a summary bar offers a jump to it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CalculatorScreen(today: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }) {
    val currentYear = remember(today) { LegalYear.at(today) }
    var year by remember(currentYear) { mutableStateOf(currentYear) }
    var inputs by remember { mutableStateOf(Inputs()) }
    val result = remember(inputs, year) { calculate(inputs, year) }

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val bounds = remember { ResultBounds() }
    var summaryBarHeight by remember { mutableIntStateOf(0) }
    val resultOutOfView by remember(scroll, bounds) {
        derivedStateOf {
            val placed = bounds.top != ResultBounds.UNKNOWN && scroll.viewportSize > 0
            placed && (bounds.bottom <= scroll.value || bounds.top >= scroll.value + scroll.viewportSize)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .onGloballyPositioned { bounds.content = it }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Header(year = year, currentYear = currentYear, onYearChange = { year = it })
                    InputSection(inputs = inputs, year = year, onInputsChange = { inputs = it })
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ResultSection(
                        result = result,
                        childBenefitRecipient = inputs.childBenefitRecipient,
                        resultCardModifier = Modifier.onGloballyPositioned(bounds::onResultPlaced),
                    )
                    SourcesCard()
                }
            }
            // Keeps the last card reachable above the summary bar.
            Spacer(Modifier.height(with(LocalDensity.current) { summaryBarHeight.toDp() }))
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scroll),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )

        AnimatedVisibility(
            visible = resultOutOfView,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            ResultSummaryBar(
                result = result,
                onShowResult = { scope.launch { scroll.animateScrollTo(bounds.top) } },
                modifier = Modifier.onSizeChanged { summaryBarHeight = it.height },
            )
        }
    }
}
