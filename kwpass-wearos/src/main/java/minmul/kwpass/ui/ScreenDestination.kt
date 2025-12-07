package minmul.kwpass.ui

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {
    @Serializable
    data object QR : ScreenDestination

    @Serializable
    data object Warning : ScreenDestination
}