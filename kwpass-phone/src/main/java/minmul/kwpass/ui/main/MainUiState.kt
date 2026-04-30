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
    val fetchFailed: Boolean = false,
    val fetchSucceeded: Boolean = false,
    val initialStatus: Boolean = true,
    val refreshTimeLeft: Int = 0,
    val qrCreationTime: Long = 0L,
    val qrSize: Int = 256,
    val sampleQrBitmap: Bitmap? = null
)

data class AccountSubmitState(
    val isSubmitting: Boolean = false,
    val failed: Boolean = false,
    val succeeded: Boolean = false,
    val initialStatus: Boolean = true,
)

data class MainUiState(
    val accountInfo: AccountInfoState = AccountInfoState(),
    val accountDataLoaded: Boolean = false,
    val inputForm: InputFormState = InputFormState(),
    val accountSubmit: AccountSubmitState = AccountSubmitState(),
    val process: ProcessState = ProcessState(),
    val setupFinished: Boolean = false,
)
