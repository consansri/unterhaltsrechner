package de.consansri.unterhaltsrechner.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Section(
    label: String,
    modifier: Modifier = Modifier,
    valid: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {

    val backgroundColor by animateColorAsState(
        targetValue = if (valid) Color(0x2076787A) else Color(0xFFF57c73),
    )

    Column(
        modifier.background(backgroundColor, RoundedCornerShape(8.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        content()
    }

}