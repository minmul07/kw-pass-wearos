package minmul.kwpass.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.theme.KWPassTheme
import minmul.kwpass.ui.widget.KwPassWidget
import timber.log.Timber

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isFirstRun by mainViewModel.isFirstRun.collectAsState()

                    if (isFirstRun != null) {
                        Timber.tag("isFirstRun").d("$isFirstRun")
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
}