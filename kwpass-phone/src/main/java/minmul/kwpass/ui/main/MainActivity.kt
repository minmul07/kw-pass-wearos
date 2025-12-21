package minmul.kwpass.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.theme.KWPassTheme
import minmul.kwpass.ui.widget.KwPassWidget

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            KwPassWidget().updateAll(applicationContext)
        }

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