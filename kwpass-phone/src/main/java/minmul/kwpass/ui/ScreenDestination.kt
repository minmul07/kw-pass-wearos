package minmul.kwpass.ui

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {
    @Serializable
    data object Landing : ScreenDestination

    @Serializable
    data object Home : ScreenDestination

    @Serializable
    data object Setting : ScreenDestination

    @Serializable
    data object Information : ScreenDestination
}