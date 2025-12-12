package minmul.kwpass.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.theme.KWPassTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isFirstRun.value == null
        }
        setContent {
            KWPassTheme {
                val isFirstRun by mainViewModel.isFirstRun.collectAsState()

                if (isFirstRun != null) {
                    Log.d("isFirstRun", "$isFirstRun")
                    val startDestination = if (isFirstRun == true) {
                        ScreenDestination.Landing
                    } else {
                        ScreenDestination.Home
                    }
                    MainScreen(startDestination = startDestination)
                }
            }
        }
    }
}