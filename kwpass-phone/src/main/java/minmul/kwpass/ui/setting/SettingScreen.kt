package minmul.kwpass.ui.setting

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import minmul.kwpass.BuildConfig
import minmul.kwpass.R
import minmul.kwpass.service.KwPassLanguageService
import minmul.kwpass.service.KwPassConst
import minmul.kwpass.ui.ScreenDestination
import minmul.kwpass.ui.UiText
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.components.KwPassTopAppBar
import minmul.kwpass.ui.components.SingleMenu
import minmul.kwpass.ui.main.MainUiState
import minmul.kwpass.ui.main.openUri
import minmul.kwpass.ui.theme.KWPassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenAppBar(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    KwPassTopAppBar(
        title = stringResource(R.string.setting),
        modifier = modifier,
        navigateUp = navigateUp,
        scrollBehavior = scrollBehavior,
    )
}

// TODO: QR 페이지로 이동 시 계정 입력 상태 유지되도록 입력 필드와 QR크기의 UiState 조정

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingMainScreen(
    modifier: Modifier = Modifier,
    mainUiState: MainUiState,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit,
    initSampleQr: () -> Unit,
    navController: NavController,
    focusManager: FocusManager,
    context: Context,
    debugAuthKey: () -> Unit
) {
    val hasAccountFormChanges = mainUiState.inputForm.run {
        ridInput != mainUiState.accountInfo.rid ||
                telInput != mainUiState.accountInfo.tel ||
                passwordInput.isNotBlank()
    }
    val hasUsablePasswordForUpdate = mainUiState.inputForm.run {
        if (passwordInput.isBlank()) {
            mainUiState.accountInfo.password.isNotBlank()
        } else {
            isPasswordValid
        }
    }
    val isFormValidForUpdate = mainUiState.inputForm.run {
        hasAccountFormChanges && isRidValid && isTelValid && hasUsablePasswordForUpdate
    }
    val licenseTypography = MaterialTheme.typography
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingScreenAppBar(
                navigateUp = {
                    navController.navigateUp()
                    focusManager.clearFocus()
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(
                start = 0.dp,
                top = paddingValues.calculateTopPadding(),
                end = 0.dp,
                bottom = paddingValues.calculateBottomPadding() + 120.dp,
            )
        ) {
            item {
                SettingSectionTitle(
                    title = stringResource(R.string.account_info),
                )
            }

            item {
                AccountSettingsCard(
                    mainUiState = mainUiState,
                    isFormValidForUpdate = isFormValidForUpdate,
                    onRidChange = onRidChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityChange = onPasswordVisibilityChange,
                    onTelChange = onTelChange,
                    onSave = onSave,
                )
            }

            item {
                SettingSectionTitle(
                    title = stringResource(R.string.setting_options),
                )
            }

            item {
                SingleMenu(
                    imageVector = Icons.Default.QrCodeScanner,
                    title = stringResource(R.string.qrcode_size),
                    bottom = false,
                    onclick = {
                        initSampleQr()
                        navController.navigate(ScreenDestination.QrSize)
                    },
                )
            }

            item {
                SingleMenu(
                    imageVector = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subTitle = KwPassLanguageService.getCurrentLanguageDisplayName(),
                    onclick = {
                        navController.navigate(ScreenDestination.Language)
                    },
                    top = false
                )
            }

            item {
                SettingSectionTitle(
                    title = stringResource(R.string.setting_info),
                )
            }

            item {
                SingleMenu(
                    imageVector = Icons.Default.Android,
                    title = stringResource(R.string.app_version),
                    subTitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    bottom = false,
                    onclick = { context.openUri(KwPassConst.STORE_URI) },
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                )
            }

            item {
                SingleMenu(
                    painter = painterResource(R.drawable.github_mark),
                    title = stringResource(R.string.github),
                    onclick = { context.openUri(KwPassConst.GITHUB_URI) },
                    bottom = false,
                    top = false,
                    iconTint = MaterialTheme.colorScheme.primary,
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                )
            }

            item {
                SingleMenu(
                    imageVector = Icons.Default.Code,
                    title = stringResource(R.string.opensource_licence),
                    top = false,
                    onclick = {
                        val title = UiText.StringResource(R.string.opensource_licence)
                            .asString(context)
                        OssLicensesMenuActivity.setActivityTitle(title)
                        OssLicensesMenuActivity.setTheme(
                            lightColorScheme(),
                            darkColorScheme(),
                            licenseTypography
                        )
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    },
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForwardIos
                )
            }

            if (BuildConfig.DEBUG) {
                item {
                    DebugSettingsActions(
                        debugAuthKey = debugAuthKey,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 16.dp, end = 32.dp, bottom = 6.dp)
    )
}

@Composable
private fun AccountSettingsCard(
    mainUiState: MainUiState,
    isFormValidForUpdate: Boolean,
    onRidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onTelChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AccountInputFieldSet(
                accountSubmitState = mainUiState.accountSubmit,
                inputFormState = mainUiState.inputForm,
                onRidChange = onRidChange,
                onPasswordChange = onPasswordChange,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                onTelChange = onTelChange,
                onButtonClicked = onSave,
                buttonEnabled = isFormValidForUpdate && !mainUiState.accountSubmit.isSubmitting,
                buttonLabel = stringResource(R.string.login),
                buttonOnWork = stringResource(R.string.checking),
                isInitialSetup = false,
            )
        }
    }
}

@Composable
private fun DebugSettingsActions(
    debugAuthKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = {
                throw RuntimeException("Test Crash")
            }
        ) {
            Text("Crashlytics 테스트")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = debugAuthKey
        ) {
            Text("저장된 AuthKey 초기화")
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
            debugAuthKey = {},
            initSampleQr = {}
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
            debugAuthKey = {},
            initSampleQr = {}
        )
    }
}
