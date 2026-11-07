package de.consansri.unterhaltsrechner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.consansri.unterhaltsrechner.ui.CalculatorScreen
import de.consansri.unterhaltsrechner.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CalculatorScreen()
        }
    }
}
