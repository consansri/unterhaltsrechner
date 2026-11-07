package de.consansri.unterhaltsrechner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Radio list for options that need an explanation; the note of the selected option is shown. */
@Composable
internal fun <T> RadioOptions(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    note: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            val active = option == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .selectable(selected = active, role = Role.RadioButton) { onSelect(option) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RadioButton(selected = active, onClick = null, modifier = Modifier.padding(8.dp))
                Column(Modifier.padding(top = 6.dp, bottom = 6.dp, end = 8.dp)) {
                    Text(label(option), style = MaterialTheme.typography.bodyMedium)
                    if (active) Explanation(note(option))
                }
            }
        }
    }
}
