package minmul.kwpass.ui.main

import android.graphics.Bitmap

data class AccountInfoState(
    val rid: String = "",
    val password: String = "",
    val tel: String = "",
) {
    val hasValidInfo: Boolean
        get() = rid.isNotBlank() && password.isNotBlank() && tel.isNotBlank()
}

data class InputFormState(
    val ridInput: String = "",
    val passwordInput: String = "",
    val telInput: String = "",
    val isRidValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isTelValid: Boolean = false,
    val passwordVisible: Boolean = false,
    val fieldErrorStatus: Boolean = false,
) {
    val isAllValidInput: Boolean
        get() = isRidValid && isPasswordValid && isTelValid
}

data class ProcessState(
    val qrBitmap: Bitmap? = null,
    val isFetching: Boolean = false,
    val fetchFailed: Boolean = false, // failedToGetQr, failedForAccountVerification 통합 고려
    val fetchSucceeded: Boolean = false,
    val initialStatus: Boolean = true,
    val refreshTimeLeft: Int = 0,
    val qrCreationTime: Long = 0L
)

data class MainUiState(
    val accountInfo: AccountInfoState = AccountInfoState(),
    val inputForm: InputFormState = InputFormState(),
    val process: ProcessState = ProcessState(),
    val setupFinished: Boolean = false,
)