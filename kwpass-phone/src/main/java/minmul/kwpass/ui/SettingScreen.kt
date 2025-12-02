package minmul.kwpass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import minmul.kwpass.R
import minmul.kwpass.main.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenAppBar(
    navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.setting)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        (R.string.goBack)
                    )
                )
            }
        })
}

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit,
    navController: NavController,
    focusManager: FocusManager
) {

    Scaffold(
        topBar = {
            SettingScreenAppBar(
                navigateUp = {
                    navController.navigateUp()
                    focusManager.clearFocus()
                }, modifier = Modifier
            )
        }

    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.ridInput,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = onRidChange,
                label = {
                    Text(text = stringResource(R.string.rid))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                    imeAction = ImeAction.Done
                ),
                isError = !uiState.isRidValid,
                enabled = !uiState.fetchingData,
                modifier = modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.passwordInput,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = onPasswordChange,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                label = {
                    Text(text = stringResource(R.string.password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password).copy(
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    val image = if (uiState.passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    val description = if (uiState.passwordVisible) "비밀번호 보기"
                    else "비밀번호 숨기기"

                    IconButton(
                        onClick = onPasswordVisibilityChange,
                        enabled = !uiState.fetchingData
                    ) {
                        Icon(imageVector = image, description)
                    }
                },
                isError = !uiState.isPasswordValid,
                enabled = !uiState.fetchingData,
                modifier = modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.telInput,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = onTelChange,
                label = {
                    Text(text = stringResource(R.string.tel))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                    imeAction = ImeAction.Done
                ),
                isError = !uiState.isTelValid,
                enabled = !uiState.fetchingData,
                modifier = modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                Button(
                    onClick = onSave,
                    enabled = uiState.isAllValid && !uiState.fetchingData
                ) {
                    if (!uiState.fetchingData) {
                        Text(text = stringResource(R.string.apply))
                    } else {
                        Text(text = stringResource(R.string.checking))
                    }
                }
            }
        }
    }
}
