package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.consansri.unterhaltsrechner.types.Zeitraum

@Composable
fun ZBetragInput(
    label: String,
    value: String,
    zeitraum: Zeitraum,
    onValueChange: (String) -> Boolean,
    onZeitraumChange: (Zeitraum) -> Unit,
    inRow: @Composable RowScope.() -> Unit = {}
) {
    var valid by remember { mutableStateOf(true) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Light, fontSize = 12.sp, textAlign = TextAlign.Right)

        // Wir verpacken BasicTextField und den Klick-Text in eine gemeinsame Reihe mit dem Rahmen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            val borderColor by animateColorAsState(
                targetValue = if (valid) Color.Transparent else Color.Red,
            )

            BasicTextField(
                value = value,
                onValueChange = {
                    valid = onValueChange(it)
                },
                cursorBrush = SolidColor(Color.Black),
                textStyle = LocalTextStyle.current,
                modifier = Modifier.border(1.dp, borderColor, CircleShape).padding(4.dp)
            )

            Button(zeitraum.suffix) {
                onZeitraumChange(zeitraum.next())
            }

            inRow()
        }
    }
}