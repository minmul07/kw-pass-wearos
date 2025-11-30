package minmul.kwpass

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