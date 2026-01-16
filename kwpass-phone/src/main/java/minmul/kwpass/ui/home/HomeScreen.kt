package minmul.kwpass.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import minmul.kwpass.BuildConfig
import minmul.kwpass.R
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.UiText
import minmul.kwpass.ui.components.QrView
import minmul.kwpass.ui.main.ProcessState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenAppBar(
    navigateSetting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = navigateSetting
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.setting)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.primaryContainer
        ),
        modifier = modifier,

        )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    processState: ProcessState,
    refreshQR: () -> Unit,
    navController: NavController,
    snackbarEvent: Flow<UiText>,
    stopTimer: () -> Unit,
    resumeTimer: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ), label = "spinAngle"
    )

    LaunchedEffect(snackbarEvent) {
        snackbarEvent.collect { uiText ->
            val message = uiText.asString(context)
            snackbarHostState.showSnackbar(message)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> stopTimer()
                Lifecycle.Event.ON_RESUME -> resumeTimer()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            HomeScreenAppBar(
                navigateSetting = {
                    navController.navigate(ScreenDestination.Setting)
                },
                modifier = Modifier
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }

    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            QrView(
                isFetching = processState.isFetching,
                qrBitmap = processState.qrBitmap,
                unavailable = processState.fetchFailed,
                refresh = refreshQR
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = refreshQR,
                enabled = !processState.isFetching,
            ) {
                AnimatedContent(
                    targetState = processState.isFetching,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 300))
                    }
                ) { isFetching ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (isFetching) angle else 0f),
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (!isFetching) {
                            Text(
                                text = stringResource(
                                    R.string.qr_seconds,
                                    processState.refreshTimeLeft
                                )
                            )
                        } else {
                            Text(text = stringResource(R.string.fetching))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        throw RuntimeException("Test Crash")
                    }
                ) {
                    Text("Crashlytics 테스트")
                }
            }
        }
    }
}
