package minmul.kwpass.ui.landing

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.components.OnboardingHeader
import minmul.kwpass.ui.main.InputFormState
import minmul.kwpass.ui.main.ProcessState
import minmul.kwpass.ui.theme.KWPassTheme

// TODO: 비밀번호 오류 발생 시, 도서관 초기 비밀번호일 수 있음. 앱 사용을 위해 사용자에게 변경 권장

@Composable
fun InitialSetupScreen(
    onNextClicked: () -> Unit,
    processState: ProcessState,
    inputFormState: InputFormState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OnboardingHeader(
                title = stringResource(R.string.initial_account_setup),
                description = stringResource(R.string.initial_account_setup_desc),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceContainer
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    AccountInputFieldSet(
                        processState = processState,
                        inputFormState = inputFormState,
                        onRidChange = onRidChange,
                        onPasswordChange = onPasswordChange,
                        onPasswordVisibilityChange = onPasswordVisibilityChange,
                        onTelChange = onTelChange,
                        onButtonClicked = {
                            if (!processState.fetchSucceeded) onSave() else onNextClicked()
                        },
                        buttonLabel = if (!processState.fetchSucceeded) {
                            stringResource(R.string.login)
                        } else {
                            stringResource(R.string.start)
                        },
                        buttonOnWork = stringResource(R.string.checking),
                        isInitialSetup = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surfaceContainer,
                            unfocusedContainerColor = colorScheme.surfaceContainer,
                            disabledContainerColor = colorScheme.surfaceContainer,
                            errorContainerColor = colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.padding(16.dp),
                        buttonEnabled = processState.fetchSucceeded ||
                                (inputFormState.isAllValidInput && !processState.isFetching)
                    )
                }
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
