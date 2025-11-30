package minmul.kwpass.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.R
import minmul.kwpass.main.MainViewModel
import minmul.kwpass.service.UserData

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
    mainViewModel: MainViewModel,
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
                value = mainViewModel.ridField,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { mainViewModel.updateRid(it) },
                label = {
                    Text(text = stringResource(R.string.rid))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                    imeAction = ImeAction.Done
                ),
                isError = !mainViewModel.isRidValid,
                enabled = mainViewModel.ridFieldEnabled,
                modifier = modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = mainViewModel.passwordField,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { mainViewModel.updatePassword(it) },
                visualTransformation = if (mainViewModel.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                label = {
                    Text(text = stringResource(R.string.password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password).copy(
                    imeAction = ImeAction.Next
                ),
                isError = !mainViewModel.isPasswordValid,
                trailingIcon = {
                    val image = if (mainViewModel.passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    val description = if (mainViewModel.passwordVisible) "비밀번호 보기"
                    else "비밀번호 숨기기"

                    IconButton(onClick = {
                        mainViewModel.passwordVisible = !mainViewModel.passwordVisible
                    }
                    ) {
                        Icon(imageVector = image, description)
                    }
                },
                enabled = mainViewModel.passwordFieldEnabled,
                modifier = modifier.fillMaxWidth(),
            )
            OutlinedTextField(

                value = mainViewModel.telField,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { mainViewModel.updateTel(it) },
                label = {
                    Text(text = stringResource(R.string.tel))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(
                    imeAction = ImeAction.Done
                ),
                isError = !mainViewModel.isTelValid,
                enabled = mainViewModel.telFieldEnabled,
                modifier = modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                Button(
                    onClick = {
                        mainViewModel.saveUserData()
                    },
                    enabled = mainViewModel.validation && !mainViewModel.fetchingData
                ) {
                    if (!mainViewModel.fetchingData) {
                        Text(text = stringResource(R.string.apply))
                    } else {
                        Text(text = stringResource(R.string.checking))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingScreenAppBarPreview() {
    SettingScreenAppBar(navigateUp = {})
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun SettingScreenPreview() {
    val mainViewModel = MainViewModel(userData = UserData(LocalContext.current)).apply {
        ridField = "test_rid"
        passwordField = "test_password"
        telField = "010-1234-5678"
    }
    val navController = rememberNavController()
    SettingScreen(
        mainViewModel = mainViewModel,
        navController = navController,
        focusManager = LocalFocusManager.current
    )
}

