package de.consansri.unterhaltsrechner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp

/**
 * Children in equal-width columns, as many per row as fit above the widest child's minimum width; rows are as high as their tallest child.
 *
 * Unlike a weighted `FlowRow`, whose minimum intrinsic width is all children side by side, this reports only the widest child.
 * A parent that wraps on intrinsic widths therefore treats it as one column wide and does not stack too early.
 */
@Composable
internal fun EqualColumns(modifier: Modifier = Modifier, spacing: Dp = 0.dp, content: @Composable () -> Unit) {
    Layout(content, modifier, remember(spacing) { EqualColumnsMeasurePolicy(spacing) })
}

private class EqualColumnsMeasurePolicy(private val spacing: Dp) : MeasurePolicy {

    private fun IntrinsicMeasureScope.columnCount(items: List<IntrinsicMeasurable>, available: Int): Int {
        if (items.isEmpty() || available == Constraints.Infinity) return items.size.coerceAtLeast(1)
        val gap = spacing.roundToPx()
        val minItemWidth = items.maxOf { it.minIntrinsicWidth(Constraints.Infinity) }
        return ((available + gap) / (minItemWidth + gap).coerceAtLeast(1)).coerceIn(1, items.size)
    }

    private fun IntrinsicMeasureScope.itemWidth(columns: Int, available: Int): Int =
        if (available == Constraints.Infinity) available else (available - spacing.roundToPx() * (columns - 1)) / columns

    private fun IntrinsicMeasureScope.stackedHeight(
        items: List<IntrinsicMeasurable>,
        available: Int,
        heightAt: IntrinsicMeasurable.(width: Int) -> Int,
    ): Int {
        if (items.isEmpty()) return 0
        val columns = columnCount(items, available)
        val width = itemWidth(columns, available)
        val rows = items.chunked(columns)
        return rows.sumOf { row -> row.maxOf { it.heightAt(width) } } + spacing.roundToPx() * (rows.size - 1)
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        measurables.maxOfOrNull { it.minIntrinsicWidth(height) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        measurables.sumOf { it.maxIntrinsicWidth(height) } + spacing.roundToPx() * (measurables.size - 1).coerceAtLeast(0)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        stackedHeight(measurables, width) { minIntrinsicHeight(it) }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        stackedHeight(measurables, width) { maxIntrinsicHeight(it) }

    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val gap = spacing.roundToPx()
        val available = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            measurables.sumOf { it.maxIntrinsicWidth(Constraints.Infinity) } + gap * (measurables.size - 1).coerceAtLeast(0)
        }
        val columns = columnCount(measurables, available)
        val itemWidth = itemWidth(columns, available)

        val rows = measurables.chunked(columns).map { row ->
            val natural = row.map { it.measure(Constraints(minWidth = itemWidth, maxWidth = itemWidth, maxHeight = constraints.maxHeight)) }
            val rowHeight = natural.maxOf { it.height }
            if (natural.all { it.height == rowHeight }) natural else row.map { it.measure(Constraints.fixed(itemWidth, rowHeight)) }
        }
        val height = rows.sumOf { row -> row.maxOf(Placeable::height) } + gap * (rows.size - 1).coerceAtLeast(0)

        return layout(constraints.constrainWidth(available), constraints.constrainHeight(height)) {
            var y = 0
            rows.forEach { row ->
                row.forEachIndexed { column, placeable -> placeable.placeRelative(column * (itemWidth + gap), y) }
                y += row.maxOf(Placeable::height) + gap
            }
        }
    }
}
