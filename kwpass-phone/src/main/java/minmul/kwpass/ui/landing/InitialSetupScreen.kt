package minmul.kwpass.ui.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.components.GentleHorizontalDivider
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun InitialSetupScreen(
    onNextClicked: () -> Unit,
    uiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.initial_account_setup),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(text = stringResource(R.string.initial_account_setup_desc))
        GentleHorizontalDivider()

        AccountInputFieldSet(
            uiState = uiState,
            onRidChange = onRidChange,
            onPasswordChange = onPasswordChange,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            onTelChange = onTelChange,
            onButtonClicked = { if (!uiState.succeededForAccountVerification) onSave() else onNextClicked() },
            buttonLabel = if (!uiState.succeededForAccountVerification) stringResource(R.string.login)
            else stringResource(R.string.start),
            buttonOnWork = stringResource(R.string.checking),
        )
    }
}

@Preview
@Composable
fun InitialSetupScreenPreview() {
    KWPassTheme {
        InitialSetupScreen(
            onNextClicked = { },
            uiState = MainUiState(
                savedRid = "2023203000",
                savedPassword = "abcdef12345678",
                savedTel = "01012345678",
                savedQR = "asdasdasd",
                ridInput = "2023203000",
                passwordInput = "abcdef12345678",
                telInput = "01012345678",
                isRidValid = true,
                isPasswordValid = true,
                isTelValid = true,
                passwordVisible = false,
                fetchingData = false,
                fieldErrorStatus = false,
                initialStatus = false,
                failedForAccountVerification = false,
                succeededForAccountVerification = false,
            ),
            onRidChange = { },
            onPasswordChange = { },
            onPasswordVisibilityChange = { },
            onTelChange = { },
            onSave = {}
        )
    }
}