package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.types.Zeitraum

@Composable
fun ZeitraumInput(
    label: String,
    value: String,
    zeitraum: Zeitraum,
    onValueChange: (String) -> Boolean,
    onZeitraumChange: (Zeitraum) -> Unit
) {
    var valid by remember { mutableStateOf(true) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)

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
                modifier = Modifier.weight(1f).border(1.dp, borderColor, CircleShape).padding(4.dp)
            )

            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            val isPressed by interactionSource.collectIsPressedAsState()

            val bgColor by animateColorAsState(
                when {
                    isPressed -> Color.Black.copy(alpha = 0.1f)
                    isHovered -> Color.Black.copy(alpha = 0.05f)
                    else -> Color.Transparent
                }
            )

            // Klickbarer Button zum Wechseln zwischen Monat, Semester, Jahr
            Text(
                text = zeitraum.suffix,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier
                    .background(bgColor, CircleShape)
                    .padding(4.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(indication = null, interactionSource = interactionSource) { onZeitraumChange(zeitraum.next()) }
            )
        }
    }
}