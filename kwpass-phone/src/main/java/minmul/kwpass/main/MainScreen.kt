package minmul.kwpass.main

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.HomeScreen
import minmul.kwpass.ui.InformationScreen
import minmul.kwpass.ui.LandingScreen
import minmul.kwpass.ui.SettingScreen


@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: ScreenDestination.Home
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

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
            HomeScreen(mainViewModel = mainViewModel, navController = navController)
        }

        composable<ScreenDestination.Setting> {
            SettingScreen(
                mainViewModel = mainViewModel,
                navController = navController,
                focusManager = focusManager
            )
        }

        composable<ScreenDestination.Information> {
            InformationScreen()
        }
    }
}