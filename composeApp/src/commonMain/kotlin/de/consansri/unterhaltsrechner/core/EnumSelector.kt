package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
inline fun <reified T : Enum<T>> EnumSelector(
    selectedOption: T,
    crossinline name: @Composable (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    crossinline onOptionSelected: (T) -> Unit,
) {

    val options = enumValues<T>()

    // Custom Styling Variablen
    val backgroundColor = Color(0xFFF0F0F0)
    val activeColor = Color(0xFF007AFF) // Ein schönes "iOS-ähnliches" Blau
    val inactiveTextColor = Color.DarkGray

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption

            val bgColor by animateColorAsState(
                if (isSelected) activeColor else Color.Transparent,
            )
            val fgColor by animateColorAsState(
                if(isSelected) Color.White else inactiveTextColor
            )

            val interactionSource = remember { MutableInteractionSource() }

            Text(
                text = name(option),
                fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                color = fgColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                softWrap = false,
                modifier = modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(bgColor)
                    .padding(4.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(indication = null, interactionSource = interactionSource) { onOptionSelected(option) }
            )
        }
    }
}