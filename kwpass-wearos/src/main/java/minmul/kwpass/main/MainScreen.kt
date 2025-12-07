package minmul.kwpass.main

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.ui.QrScreen
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.WarningScreen

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        mainViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = ScreenDestination.QR,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<ScreenDestination.QR> {
            QrScreen(
                uiState = uiState,
                onRefresh = {
                    if (uiState.allDataReady) {
                        mainViewModel.refreshQR()
                    } else {
                        mainViewModel.requestForcedAccountDataSync()
                    }
                },
                navController = navController
            )
        }

        composable<ScreenDestination.Warning> {
            WarningScreen()
        }
    }


}