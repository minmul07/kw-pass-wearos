package minmul.kwpass.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.wearable.DataClient
import dagger.hilt.android.AndroidEntryPoint
import minmul.kwpass.ui.theme.KWPassTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KWPassTheme {
                 MainScreen()
            }
        }
    }
}