package de.consansri.unterhaltsrechner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.money.Euro
import de.consansri.unterhaltsrechner.money.PeriodicAmount
import de.consansri.unterhaltsrechner.money.PeriodicAmount.Period

/**
 * Input field for a euro amount with a period.
 *
 * The text is held locally, so intermediate states such as `12,` or an empty field survive typing.
 * Valid input is applied immediately; invalid input is explained, and until corrected the app keeps using the last valid value.
 *
 * @param tableValue Monthly table value; if [value] differs from it, the field offers [onReset].
 * @param periodSelectable `false` for amounts that are always monthly, such as table values.
 */
@Composable
internal fun AmountField(
    label: String,
    value: PeriodicAmount,
    onChange: (PeriodicAmount) -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
    tableValue: Euro? = null,
    onReset: (() -> Unit)? = null,
    periodSelectable: Boolean = true,
) {
    var text by remember { mutableStateOf(value.amount.toInputText()) } // initial-only; the effect below syncs later changes
    val parsed = Euro.parse(text)

    // Takes over external changes (reset, year change) without overwriting input in progress.
    LaunchedEffect(value.amount) {
        if (Euro.parse(text).validOrNull() != value.amount) text = value.amount.toInputText()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            if (periodSelectable) {
                SegmentedPicker(
                    options = Period.entries,
                    selected = value.period,
                    onSelect = { onChange(value.copy(period = it)) },
                    label = { if (it == Period.SEMESTER) "Sem." else it.label },
                    modifier = Modifier.widthIn(max = 210.dp),
                )
            } else {
                Explanation("pro Monat")
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { new ->
                text = new
                val newAmount = Euro.parse(new).validOrNull()
                if (newAmount != null && newAmount != value.amount) onChange(value.copy(amount = newAmount))
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    // Normalizes the text on leaving the field, e.g. "1.234,5" -> "1234,50".
                    if (!focus.isFocused && Euro.parse(text) is Euro.ParseResult.Valid) text = value.amount.toInputText()
                },
            singleLine = true,
            isError = parsed is Euro.ParseResult.Invalid,
            suffix = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        when (parsed) {
            is Euro.ParseResult.Invalid -> Text(
                parsed.error.message(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            is Euro.ParseResult.Valid -> if (value.period != Period.MONTH && value.amount != Euro.ZERO) {
                Text(
                    "= ${value.perMonth} / Monat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (tableValue != null && onReset != null && value.perMonth != tableValue) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Explanation("Tabellenwert: $tableValue", Modifier.weight(1f, fill = false))
                TextButton(onClick = onReset) { Text("Zurücksetzen") }
            }
        }

        if (note != null) Explanation(note)
    }
}

private fun Euro.ParseResult.validOrNull(): Euro? = (this as? Euro.ParseResult.Valid)?.value

private fun Euro.ParseError.message(): String = when (this) {
    Euro.ParseError.NEGATIVE -> "Kein negativer Betrag."
    Euro.ParseError.MALFORMED -> "Format: 1.234,56"
    Euro.ParseError.TOO_MANY_DECIMALS -> "Max. 2 Nachkommastellen."
    Euro.ParseError.TOO_LARGE -> "Zu groß."
}
