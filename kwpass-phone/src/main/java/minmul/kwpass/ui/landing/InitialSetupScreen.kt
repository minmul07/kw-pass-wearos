package minmul.kwpass.ui.landing

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.main.InputFormState
import minmul.kwpass.ui.main.ProcessState
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun InitialSetupScreen(
    onNextClicked: () -> Unit,
    processState: ProcessState,
    inputFormState: InputFormState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(colorScheme.secondaryContainer),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = stringResource(R.string.initial_account_setup),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.initial_account_setup_desc),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.inverseOnSurface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                AccountInputFieldSet(
                    processState = processState,
                    inputFormState = inputFormState,
                    onRidChange = onRidChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityChange = onPasswordVisibilityChange,
                    onTelChange = onTelChange,
                    onButtonClicked = { if (!processState.fetchSucceeded) onSave() else onNextClicked() },
                    buttonLabel = if (!processState.fetchSucceeded) stringResource(R.string.login)
                    else stringResource(R.string.start),
                    buttonOnWork = stringResource(R.string.checking),
                    isInitialSetup = true, colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.inverseOnSurface,
                        unfocusedContainerColor = colorScheme.inverseOnSurface,
                        disabledContainerColor = colorScheme.inverseOnSurface,
                        errorContainerColor = colorScheme.inverseOnSurface
                    ),
                    modifier = Modifier.padding(16.dp),
                    buttonEnabled = (processState.fetchSucceeded) || (inputFormState.isAllValidInput && !processState.isFetching)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InitialSetupScreenPreview() {
    KWPassTheme {
        // 프리뷰를 위한 가상 데이터 생성
        val mockInputForm = InputFormState(
            ridInput = "2023203000",
            passwordInput = "abcdef12345678",
            telInput = "01012345678",
            isRidValid = true,
            isPasswordValid = true,
            isTelValid = true,
            passwordVisible = false,
            fieldErrorStatus = false
        )

        val mockProcess = ProcessState(
            isFetching = false,
            fetchFailed = false,
            fetchSucceeded = false,
            initialStatus = false
        )

        InitialSetupScreen(
            onNextClicked = { },
            processState = mockProcess,
            inputFormState = mockInputForm,
            onRidChange = { },
            onPasswordChange = { },
            onPasswordVisibilityChange = { },
            onTelChange = { },
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DarkInitialSetupScreenPreview() {
    KWPassTheme {
        val mockInputForm = InputFormState(
            ridInput = "2023203000",
            passwordInput = "abcdef12345678",
            telInput = "01012345678",
            isRidValid = true,
            isPasswordValid = true,
            isTelValid = true,
            passwordVisible = false,
            fieldErrorStatus = false
        )

        val mockProcess = ProcessState(
            isFetching = false,
            fetchFailed = false,
            fetchSucceeded = false,
            initialStatus = false
        )

        InitialSetupScreen(
            onNextClicked = { },
            processState = mockProcess,
            inputFormState = mockInputForm,
            onRidChange = { },
            onPasswordChange = { },
            onPasswordVisibilityChange = { },
            onTelChange = { },
            onSave = {}
        )
    }
}