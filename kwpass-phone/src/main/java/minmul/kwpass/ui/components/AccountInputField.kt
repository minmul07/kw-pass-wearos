package minmul.kwpass.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import minmul.kwpass.R
import minmul.kwpass.ui.main.AccountSubmitState
import minmul.kwpass.ui.main.InputFormState
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun AccountInputFieldSet(
    modifier: Modifier = Modifier,
    accountSubmitState: AccountSubmitState,
    inputFormState: InputFormState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onButtonClicked: () -> Unit,
    buttonLabel: String,
    buttonOnWork: String,
    buttonEnabled: Boolean,
    isInitialSetup: Boolean = false,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surfaceContainer,
        unfocusedContainerColor = colorScheme.surfaceContainer,
        disabledContainerColor = colorScheme.surfaceContainer,
        errorContainerColor = colorScheme.surfaceContainer
    )
) {
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(accountSubmitState.succeeded) {
        if (accountSubmitState.succeeded) {
            showSuccessMessage = true
            delay(3000L)
            showSuccessMessage = false
        } else {
            showSuccessMessage = false
        }
    }

    val fieldEnabled = !accountSubmitState.isSubmitting && if (isInitialSetup) {
        !accountSubmitState.succeeded
    } else {
        true
    }

    val statusMessage: String = if (accountSubmitState.initialStatus) ""
    else if (accountSubmitState.isSubmitting) stringResource(R.string.verifying_account)
    else if (accountSubmitState.failed) stringResource(R.string.error_verifying_account)
    else if (accountSubmitState.succeeded) stringResource(R.string.login_success)
    else ""

    val statusVisible = statusMessage.isNotBlank() &&
            (!accountSubmitState.succeeded || showSuccessMessage)

    val statusAlpha by animateFloatAsState(
        targetValue = if (statusVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (accountSubmitState.succeeded && !showSuccessMessage) {
                1000
            } else {
                150
            }
        ),
        label = "accountStatusAlpha"
    )

    val statusColor = when {
        accountSubmitState.failed -> MaterialTheme.colorScheme.error
        accountSubmitState.succeeded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AccountInputField(
            value = inputFormState.ridInput,
            onValueChange = onRidChange,
            label = stringResource(R.string.rid),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                imeAction = ImeAction.Done
            ),
            isError = inputFormState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        AccountInputField(
            value = inputFormState.passwordInput,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.password),
            visualTransformationStatus = if (inputFormState.passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password).copy(
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                val passwordVisibilityIcon =
                    if (inputFormState.passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                val description = if (inputFormState.passwordVisible) "비밀번호 숨기기"
                else "비밀번호 보기"

                IconButton(
                    onClick = onPasswordVisibilityChange,
                    enabled = !accountSubmitState.isSubmitting
                ) {
                    Icon(imageVector = passwordVisibilityIcon, description)
                }
            },
            isError = inputFormState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        AccountInputField(
            value = inputFormState.telInput,
            onValueChange = onTelChange,
            label = stringResource(R.string.tel),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                imeAction = ImeAction.Done
            ),
            isError = inputFormState.fieldErrorStatus,
            enabled = fieldEnabled,
            colors = colors
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(statusAlpha)
                    )
                }
            }
            Button(
                onClick = onButtonClicked,
                enabled = buttonEnabled,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 96.dp)
            ) {
                if (!accountSubmitState.isSubmitting) {
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
    colors: TextFieldColors
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
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun AccountInputFieldSetPreview() {
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

        val mockAccountSubmitState = AccountSubmitState(
            isSubmitting = false,
            failed = false,
            succeeded = false,
            initialStatus = false
        )

        AccountInputFieldSet(
            accountSubmitState = mockAccountSubmitState,
            inputFormState = mockInputForm,
            onRidChange = {},
            onPasswordChange = {},
            onPasswordVisibilityChange = {},
            onTelChange = {},
            onButtonClicked = {},
            buttonLabel = stringResource(R.string.login),
            buttonOnWork = stringResource(R.string.checking),
            isInitialSetup = true,
            buttonEnabled = true
        )
    }
}
