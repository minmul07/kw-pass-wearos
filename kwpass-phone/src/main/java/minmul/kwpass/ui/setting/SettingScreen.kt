package minmul.kwpass.ui.setting

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import minmul.kwpass.BuildConfig
import minmul.kwpass.R
import minmul.kwpass.service.KwPassLanguageService
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.components.SingleMenu
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.main.goToGithub
import minmul.kwpass.ui.theme.KWPassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenAppBar(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.setting)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
        }
    )
}

@Composable
fun SettingMainScreen(
    modifier: Modifier = Modifier,
    mainUiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit,
    navController: NavController,
    focusManager: FocusManager,
    context: Context,
    debugAuthKey: () -> Unit
) {
    val isFormValidForUpdate = mainUiState.inputForm.run {
        isRidValid && isTelValid && (isPasswordValid || passwordInput.isBlank())
    }

    Scaffold(
        topBar = {
            SettingScreenAppBar(
                navigateUp = {
                    navController.navigateUp()
                    focusManager.clearFocus()
                }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {

            Card(
                modifier = Modifier
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.account_info),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AccountInputFieldSet(
                        processState = mainUiState.process,
                        inputFormState = mainUiState.inputForm,
                        onRidChange = onRidChange,
                        onPasswordChange = onPasswordChange,
                        onPasswordVisibilityChange = onPasswordVisibilityChange,
                        onTelChange = onTelChange,
                        onButtonClicked = onSave,
                        buttonEnabled = isFormValidForUpdate && !mainUiState.process.isFetching,
                        buttonLabel = stringResource(R.string.login),
                        buttonOnWork = stringResource(R.string.checking),
                        isInitialSetup = false,
                    )
                }
            }

            SingleMenu(
                imageVector = Icons.Default.Language,
                title = stringResource(R.string.language),
                subTitle = KwPassLanguageService.getCurrentLanguageDisplayName(), // io block?
                onclick = {
                    navController.navigate(ScreenDestination.Language)
                }
            )

            SingleMenu(
                imageVector = Icons.Default.Android,
                title = stringResource(R.string.app_version),
                subTitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                bottom = false
            )
            SingleMenu(
                painter = painterResource(R.drawable.github_mark),
                title = stringResource(R.string.github),
                onclick = { context.goToGithub() },
                bottom = false,
                top = false,
                trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
            )
            SingleMenu(
                imageVector = Icons.Default.Code,
                title = stringResource(R.string.opensource_licence),
                top = false,
                onclick = {
                    val intent = Intent(context, OssLicensesMenuActivity::class.java)
                    // 앱의 타이틀을 변경하고 싶다면 추가
                    intent.putExtra("title", "오픈소스 라이선스")
                    context.startActivity(intent)
                },
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForwardIos
            )

            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        throw RuntimeException("Test Crash")
                    }
                ) {
                    Text("Crashlytics 테스트")
                }

                Button(
                    onClick = {
                        debugAuthKey()
                    }
                ) {
                    Text("저장된 AuthKey 초기화")
                }
            }
            Spacer(
                modifier = Modifier.height(120.dp)
            )
        }
    }
}

@Preview
@Composable
fun SettingMainScreenPreview() {
    KWPassTheme {
        SettingMainScreen(
            mainUiState = MainUiState(),
            onRidChange = { },
            onPasswordChange = { },
            onPasswordVisibilityChange = { },
            onTelChange = { },
            onSave = {},
            navController = rememberNavController(),
            focusManager = LocalFocusManager.current,
            context = LocalContext.current,
            debugAuthKey = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DarkSettingMainScreenPreview() {
    KWPassTheme {
        SettingMainScreen(
            mainUiState = MainUiState(),
            onRidChange = { },
            onPasswordChange = { },
            onPasswordVisibilityChange = { },
            onTelChange = { },
            onSave = {},
            navController = rememberNavController(),
            focusManager = LocalFocusManager.current,
            context = LocalContext.current,
            debugAuthKey = {}
        )
    }
}