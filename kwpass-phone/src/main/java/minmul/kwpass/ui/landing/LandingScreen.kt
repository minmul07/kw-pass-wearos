package minmul.kwpass.ui.landing

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.main.MainViewModel
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun LandingScreen(
    onFinished: () -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity
    val mainViewModel: MainViewModel = hiltViewModel(activity)
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LandingContent(
        onFinished = onFinished,
        uiState = uiState,
        onRidChange = mainViewModel::updateRidInput,
        onPasswordChange = mainViewModel::updatePasswordInput,
        onPasswordVisibilityChange = mainViewModel::updatePasswordVisibility,
        onTelChange = mainViewModel::updateTelInput,
        onAccountSave = mainViewModel::setAccountData
    )
}

@Composable
fun LandingContent(
    onFinished: () -> Unit,
    uiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onAccountSave: () -> Unit
) {
    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    val onNextClicked = {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {}
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomeScreen(
                    onStartClicked = { onNextClicked() }
                )

                1 -> IntroducePhoneScreen(
                    onNextClicked = { onNextClicked() }
                )

                2 -> IntroduceWatchScreen(
                    onNextClicked = { onNextClicked() }
                )

                3 -> InitialSetupScreen(
                    onNextClicked = onFinished,
                    uiState = uiState,
                    onRidChange = onRidChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityChange = onPasswordVisibilityChange,
                    onTelChange = onTelChange,
                    onSave = onAccountSave
                )
            }
        }
    }
}

@Preview
@Composable
fun LandingScreenPreview() {
    KWPassTheme {
        LandingScreen(
            onFinished = {})
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DarkLandingScreenPreview() {
    KWPassTheme {
        LandingScreen(
            onFinished = {})
    }
}