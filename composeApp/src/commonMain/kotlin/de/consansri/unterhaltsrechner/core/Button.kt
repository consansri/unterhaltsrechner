package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Button(value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {

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

    Text(
        text = value,
        fontWeight = FontWeight.Light,
        color = Color.DarkGray,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(4.dp)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(indication = null, interactionSource = interactionSource) { onClick() }
    )

}