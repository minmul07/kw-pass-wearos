package minmul.kwpass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import minmul.kwpass.R
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun AccountInputFieldSet(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onButtonClicked: () -> Unit,
    buttonLabel: String,
    buttonOnWork: String,
    isInitialSetup: Boolean = false,
    colors : TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.inverseOnSurface,
        unfocusedContainerColor = colorScheme.inverseOnSurface,
        disabledContainerColor = colorScheme.inverseOnSurface,
        errorContainerColor = colorScheme.inverseOnSurface
    )
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val fieldEnabled = !uiState.fetchingData && if (isInitialSetup) {
            !uiState.succeededForAccountVerification
        } else {
            true
        }

        // 학번
        AccountInputField(
            value = uiState.ridInput,
            onValueChange = onRidChange,
            label = stringResource(R.string.rid),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                imeAction = ImeAction.Done
            ),
            isError = uiState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 비밀번호
        AccountInputField(
            value = uiState.passwordInput,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.password),
            visualTransformationStatus = if (uiState.passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password).copy(
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                val passwordVisibilityIcon = if (uiState.passwordVisible) Icons.Default.Visibility
                else Icons.Default.VisibilityOff

                val description = if (uiState.passwordVisible) "비밀번호 보기"
                else "비밀번호 숨기기"

                IconButton(
                    onClick = onPasswordVisibilityChange, enabled = !uiState.fetchingData
                ) {
                    Icon(imageVector = passwordVisibilityIcon, description)
                }
            },
            isError = uiState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 전화번호
        AccountInputField(
            value = uiState.telInput,
            onValueChange = onTelChange,
            label = stringResource(R.string.tel),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                imeAction = ImeAction.Done
            ),
            isError = uiState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val message: String = if (uiState.initialStatus) " "
            else if (uiState.fetchingData) stringResource(R.string.verifying_account)
            else if (uiState.failedForAccountVerification) stringResource(R.string.error_verifying_account)
            else if (uiState.succeededForAccountVerification) stringResource(R.string.login_success)
            else if (!uiState.isAllValidInput) stringResource(R.string.field_not_satisfied) // TODO() not working
            else " "

            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Button(
                onClick = onButtonClicked,
                enabled = uiState.isAllValidInput && !uiState.fetchingData,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (!uiState.fetchingData) {
                    Text(text = buttonLabel)
                } else {
                    Text(text = buttonOnWork)
                }
            }
        }
    }
}


// TODO(): supportingText 적용하기
@Composable
fun AccountInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformationStatus: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean,
    enabled: Boolean,
    colors : TextFieldColors
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        colors = colors,
        onValueChange = onValueChange,
        visualTransformation = visualTransformationStatus,
        label = { Text(text = label) },
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        isError = isError,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun AccountInputFieldSetPreview() {
    KWPassTheme {
        AccountInputFieldSet(
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
            onRidChange = {},
            onPasswordChange = {},
            onPasswordVisibilityChange = {},
            onTelChange = {},
            onButtonClicked = {},
            buttonLabel = stringResource(R.string.login),
            buttonOnWork = stringResource(R.string.checking),
        )
    }
}