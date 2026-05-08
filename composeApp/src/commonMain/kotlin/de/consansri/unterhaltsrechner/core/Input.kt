package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Input(
    label: String,
    value: String,
    onChange: (String) -> Boolean
) {

    var valid by remember { mutableStateOf(true) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)

        val borderColor by animateColorAsState(
            targetValue = if (valid) Color.LightGray else Color.Red,
        )

        BasicTextField(
            value = value,
            onValueChange = {
                valid = onChange(it)
            },
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier.weight(1f).border(1.dp, borderColor, CircleShape).padding(8.dp),
        )
    }

}