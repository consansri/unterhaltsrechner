package de.consansri.unterhaltsrechner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Warning colors for notices, matching the light and dark scheme. */
@Immutable
internal data class SignalColors(
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
) {
    companion object {
        val Light = SignalColors(
            warning = Color(0xFF8A5100),
            warningContainer = Color(0xFFFFE8C7),
            onWarningContainer = Color(0xFF2C1700),
        )

        val Dark = SignalColors(
            warning = Color(0xFFFFB95C),
            warningContainer = Color(0xFF4A3000),
            onWarningContainer = Color(0xFFFFDDB3),
        )

        val Local = staticCompositionLocalOf { Light }
    }
}
