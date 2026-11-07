package de.consansri.unterhaltsrechner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.consansri.unterhaltsrechner.ui.theme.SignalColors

/** Tinted box for information, warnings and errors. */
@Composable
internal fun Callout(text: String, tone: CalloutTone, modifier: Modifier = Modifier, title: String? = null) {
    val signal = SignalColors.Local.current
    val (background, foreground) = when (tone) {
        CalloutTone.INFO -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        CalloutTone.WARNING -> signal.warningContainer to signal.onWarningContainer
        CalloutTone.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = foreground)
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = foreground)
    }
}
