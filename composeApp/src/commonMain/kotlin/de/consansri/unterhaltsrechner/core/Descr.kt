package de.consansri.unterhaltsrechner.core

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp

@Composable
fun Descr(paragraph: String, text: String) {

    Column {
        Text(paragraph, modifier = Modifier, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = Color.DarkGray)
        Text(text, modifier = Modifier, fontSize = 12.sp)
    }

}