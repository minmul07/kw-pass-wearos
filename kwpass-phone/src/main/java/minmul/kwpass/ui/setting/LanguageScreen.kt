package minmul.kwpass.ui.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import minmul.kwpass.R
import minmul.kwpass.service.KwPassLanguageService
import minmul.kwpass.ui.components.KwPassTopAppBar
import minmul.kwpass.ui.components.SingleMenu
import minmul.kwpass.ui.theme.KWPassTheme

@Composable
fun LanguageScreenTopBar(
    navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    KwPassTopAppBar(
        title = stringResource(R.string.language),
        modifier = modifier,
        navigateUp = navigateUp,
    )
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
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 8.dp,
            ),
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
