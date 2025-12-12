package minmul.kwpass.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import minmul.kwpass.R
import minmul.kwpass.ui.components.AccountInputFieldSet
import minmul.kwpass.ui.main.MainUiState

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
            AccountInputFieldSet(
                uiState = uiState,
                onRidChange = onRidChange,
                onPasswordChange = onPasswordChange,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                onTelChange = onTelChange,
                onButtonClicked = onSave,
                buttonLabel = stringResource(R.string.login),
                buttonOnWork = stringResource(R.string.checking),
            )
        }
    }
}
