package io.designtoswiftui.cookmode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.designtoswiftui.cookmode.ui.navigation.CookModeNavGraph
import io.designtoswiftui.cookmode.ui.theme.CookModeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookModeTheme {
                CookModeNavGraph()
            }
        }
    }
}
