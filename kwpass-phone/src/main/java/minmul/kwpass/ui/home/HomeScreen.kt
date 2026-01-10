package minmul.kwpass.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import minmul.kwpass.BuildConfig
import minmul.kwpass.R
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.UiText
import minmul.kwpass.ui.components.QrView
import minmul.kwpass.ui.main.MainUiState

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
    uiState: MainUiState,
    refreshQR: () -> Unit,
    navController: NavController,
    snackbarEvent: Flow<UiText>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(snackbarEvent) {
        snackbarEvent.collect { uiText ->
            val message = uiText.asString(context)
            snackbarHostState.showSnackbar(message)
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
                isFetching = uiState.fetchingData,
                qrBitmap = uiState.qrBitmap,
                unavailable = uiState.failedToGetQr,
                refresh = refreshQR
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = refreshQR,
                enabled = uiState.isAllValidInput && !uiState.fetchingData,
            ) {
                AnimatedContent(
                    targetState = uiState.fetchingData,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 300))
                    }
                ) { isFetching ->
                    if (!isFetching) {
                        Text(text = stringResource(R.string.fetch))
                    } else {
                        Text(text = stringResource(R.string.fetching))
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
