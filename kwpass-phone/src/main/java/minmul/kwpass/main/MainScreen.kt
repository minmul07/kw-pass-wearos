package minmul.kwpass.main

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.ui.HomeScreen
import minmul.kwpass.ui.InformationScreen
import minmul.kwpass.ui.LandingScreen
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.SettingScreen


@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    backStackEntry?.destination?.route ?: ScreenDestination.Home
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        mainViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    NavHost(
        navController = navController,
        startDestination = ScreenDestination.Home,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<ScreenDestination.Landing> {
            LandingScreen()
        }

        composable<ScreenDestination.Home> {
            HomeScreen(
                uiState = uiState,
                refreshQR = { mainViewModel.refreshQR() },
                navController = navController
            )
        }

        composable<ScreenDestination.Setting> {
            SettingScreen(
                uiState = uiState,
                navController = navController,
                focusManager = focusManager,
                onRidChange = { mainViewModel.updateRidInput(it) },
                onPasswordChange = { mainViewModel.updatePasswordInput(it) },
                onPasswordVisibilityChange = { mainViewModel.updatePasswordVisibility() },
                onTelChange = { mainViewModel.updateTelInput(it) },
                onSave = { mainViewModel.saveUserData() },
            )
        }

        composable<ScreenDestination.Information> {
            InformationScreen()
        }
    }
}