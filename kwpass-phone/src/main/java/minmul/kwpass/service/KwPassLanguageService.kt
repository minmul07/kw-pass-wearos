package minmul.kwpass.service

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import minmul.kwpass.R
import timber.log.Timber
import java.util.Locale

object KwPassLanguageService {
    val languageCodeList: List<String> = listOf(
        "ko", "en", "ja", "ru", "vi", "zh-CN", "zh-TW"
    )

    @StringRes
    private fun getNativeNameResource(code: String): Int {
        return when (code) {
            "ko" -> R.string.korean
            "en" -> R.string.english
            "ja" -> R.string.japanese
            "vi" -> R.string.vietnamese
            "ru" -> R.string.russian
            "zh-CN" -> R.string.chinese_simplified
            "zh-TW" -> R.string.chinese_traditional
            else -> R.string.english // 영어를 기본으로
        }
    }

    @Composable
    fun getLanguageDisplayOptions(): List<Pair<String, String>> {
        val currentLocale = getCurrentLocale()

        return languageCodeList.map { code ->
            val displayLocale = when (code) {
                "zh-CN" -> Locale.forLanguageTag("zh-Hans") // 간체
                "zh-TW" -> Locale.forLanguageTag("zh-Hant") // 번체
                else -> Locale.forLanguageTag(code)
            }

            val displayLanguage = if (code.startsWith("zh")) {
                displayLocale.getDisplayName(currentLocale)
            } else {
                displayLocale.getDisplayLanguage(currentLocale)
            }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(currentLocale) else it.toString() }

            val nativeName = stringResource(id = getNativeNameResource(code))
            val formattedString = "$displayLanguage - $nativeName"

            code to formattedString
        }
    }

    @Composable
    private fun getCurrentLocale(): Locale {
        val configuration = LocalConfiguration.current
        return ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    }

    @Composable
    fun getLanguageCode(): String {
        val currentLocale: Locale = getCurrentLocale()
        val languageCode = currentLocale.language
        val countryCode = currentLocale.country
        return if (languageCode == "zh" && countryCode.isNotEmpty()) {
            "$languageCode-$countryCode"
        } else {
            languageCode
        }
    }

    @Composable
    fun getCurrentLanguageDisplayName(): String {
        val currentLocale = getCurrentLocale()
        val displayLanguage = if (getLanguageCode().startsWith("zh")) {
            currentLocale.displayName
        } else {
            currentLocale.displayLanguage
        }

        return displayLanguage.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(currentLocale) else it.toString()
        }
    }


    fun changeAppLanguage(languageTag: String) {
        Timber.tag("KwPassLanguageService").d("app language set to $languageTag")
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
