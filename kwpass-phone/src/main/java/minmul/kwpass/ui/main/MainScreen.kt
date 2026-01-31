package minmul.kwpass.ui.main

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.ui.RoundedClippingScreenWarper
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.home.HomeScreen
import minmul.kwpass.ui.landing.LandingScreen
import minmul.kwpass.ui.setting.LanguageScreen
import minmul.kwpass.ui.setting.SettingMainScreen


@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    startDestination: ScreenDestination
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    backStackEntry?.destination?.route ?: ScreenDestination.Home
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val mainUiState by mainViewModel.mainUiState.collectAsStateWithLifecycle()

    val animationDuration = 350
    val tweenSpec = tween<IntOffset>(durationMillis = animationDuration)

    LaunchedEffect(key1 = true) {
        mainViewModel.toastEvent.collect { uiText ->
            val message = uiText.asString(context)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<ScreenDestination.Landing>(
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tweenSpec) +
                        fadeOut(animationSpec = tween(animationDuration)) +
                        scaleOut(animationSpec = tween(animationDuration), targetScale = 0.86f)
            }
        ) {
            RoundedClippingScreenWarper(
                animationDuration = animationDuration.toLong()
            ) {
                LandingScreen(
                    onFinished = {
                        mainViewModel.completeInitialSetup()
                        navController.navigate(ScreenDestination.Home) {
                            popUpTo<ScreenDestination.Landing> {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable<ScreenDestination.Home>(
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tweenSpec) +
                        fadeOut(animationSpec = tween(animationDuration)) +
                        scaleOut(animationSpec = tween(animationDuration), targetScale = 0.86f)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tweenSpec) +
                        fadeIn(animationSpec = tween(animationDuration)) +
                        scaleIn(animationSpec = tween(animationDuration), initialScale = 0.86f)
            }
        ) {
            RoundedClippingScreenWarper(
                animationDuration = animationDuration.toLong()
            ) {
                HomeScreen(
                    processState = mainUiState.process,
                    refreshQR = { mainViewModel.refreshQR() },
                    navController = navController,
                    snackbarEvent = mainViewModel.snackbarEvent,
                    stopTimer = { mainViewModel.stopRefreshTimer() },
                    resumeTimer = { mainViewModel.resumeRefreshTimer() }
                )
            }
        }

        composable<ScreenDestination.Setting>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tweenSpec) +
                        scaleIn(animationSpec = tween(animationDuration), initialScale = 0.92f)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tweenSpec) +
                        fadeOut(animationSpec = tween(animationDuration)) +
                        scaleOut(animationSpec = tween(animationDuration), targetScale = 0.86f)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tweenSpec) +
                        fadeIn(animationSpec = tween(animationDuration)) +
                        scaleIn(animationSpec = tween(animationDuration), initialScale = 0.86f)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tweenSpec) +
                        scaleOut(animationSpec = tween(animationDuration), targetScale = 0.86f)
            }
        ) {
            RoundedClippingScreenWarper(
                animationDuration =animationDuration.toLong()
            ) {
                SettingMainScreen(
                    mainUiState = mainUiState,
                    navController = navController,
                    focusManager = focusManager,
                    onRidChange = { mainViewModel.updateRidInput(it) },
                    onPasswordChange = { mainViewModel.updatePasswordInput(it) },
                    onPasswordVisibilityChange = { mainViewModel.updatePasswordVisibility() },
                    onTelChange = { mainViewModel.updateTelInput(it) },
                    onSave = { mainViewModel.setAccountData() },
                    context = context,
                    debugAuthKey = { mainViewModel.removeAuthKeyOnDisk() }
                )
            }
        }

        composable<ScreenDestination.Language>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tweenSpec) +
                        scaleIn(animationSpec = tween(animationDuration), initialScale = 0.92f)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tweenSpec) +
                        scaleOut(animationSpec = tween(animationDuration), targetScale = 0.86f)
            }
        ) {
            RoundedClippingScreenWarper(
                animationDuration = animationDuration.toLong()
            ) {
                LanguageScreen(
                    navController = navController,
                )
            }
        }
    }
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

fun Context.goToGithub() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://github.com/minmul07/kw-pass-wearos".toUri()
    }
    startActivity(intent)
}