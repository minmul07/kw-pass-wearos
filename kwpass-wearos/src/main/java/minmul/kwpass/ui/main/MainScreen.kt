package minmul.kwpass.ui.main

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import minmul.kwpass.ui.QrScreen

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    val isVibrationActive by mainViewModel.isErrorVibrationActive.collectAsState()
    ErrorVibrationHandler(
        triggerError = isVibrationActive,
        onConsumed = { mainViewModel.stopErrorVibration() }
    )

    QrScreen(
        uiState = uiState,
        onRefresh = {
            if (uiState.allDataReady) {
                mainViewModel.refreshQR()
            } else {
                mainViewModel.requestForcedAccountDataSync(silent = false)
            }
        }
    )
}

@Composable
fun ErrorVibrationHandler(
    triggerError: Boolean,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    LaunchedEffect(triggerError) {
        if (triggerError) {
            val timings = longArrayOf(0, 50, 150, 50, 50, 50)
            val amplitudes = intArrayOf(0, 192, 0, 255, 0, 255)

            if (vibrator.hasVibrator()) {
                // API 26(O) 이상
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            }
            onConsumed() // 초기화
        }
    }
}