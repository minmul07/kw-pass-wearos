package minmul.kwpass.ui.setting

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.R
import minmul.kwpass.service.KwPassLanguageService
import minmul.kwpass.ui.components.SingleMenu
import minmul.kwpass.ui.theme.KWPassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreenTopBar(
    navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.language)) },
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
        })
}

@Composable
fun LanguageScreen(
    navController: NavController
) {
    val languageOptions = KwPassLanguageService.getLanguageDisplayOptions()
    val currentCode = KwPassLanguageService.getLanguageCode()

    Scaffold(
        topBar = {
            LanguageScreenTopBar(
                navigateUp = {
                    navController.navigateUp()
                })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            itemsIndexed(languageOptions) { index, (code, displayName) ->
                val isFirst = index == 0
                val isLast = index == languageOptions.lastIndex

                SingleMenu(
                    title = displayName,
                    trailingIcon = if (currentCode.startsWith(code)) {
                        Icons.Default.Check
                    } else null,
                    top = isFirst,
                    bottom = isLast,
                    onclick = {
                        KwPassLanguageService.changeAppLanguage(code)
                    }
                )
            }
        }
    }
}


@Preview
@Composable
fun LanguageScreenPreview(
) {
    KWPassTheme {
        LanguageScreen(
            navController = rememberNavController(),
        )
    }
}