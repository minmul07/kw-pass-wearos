package minmul.kwpass.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.util.Locale

@Composable
fun getLanguageCode(): String {
    val configuration = LocalConfiguration.current

    val currentLocale: Locale =
        ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()

    val languageCode = currentLocale.language
//    val countryCode = currentLocale.country
    return languageCode
}