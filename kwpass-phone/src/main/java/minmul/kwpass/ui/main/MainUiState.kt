package minmul.kwpass.ui.main

import android.graphics.Bitmap

data class MainUiState(
    // 저장된 사용자 정보
    val savedRid: String = "",
    val savedPassword: String = "",
    val savedTel: String = "",
    val savedQR: String = "",

    // TextField
    val ridInput: String = "",
    val passwordInput: String = "",
    val telInput: String = "",
    val isRidValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isTelValid: Boolean = false,

    //QR
    val qrBitmap: Bitmap? = null,
    val failedToGetQr: Boolean = false,

    // UI상태
    val fieldErrorStatus: Boolean = false,
    val passwordVisible: Boolean = false,
    val fetchingData: Boolean = false,
    val initialStatus: Boolean = true,
    val failedForAccountVerification: Boolean = false,
    val succeededForAccountVerification: Boolean = false,
    val setupFinished: Boolean = false
) {
    val isAllValidInput: Boolean
        get() = isRidValid && isPasswordValid && isTelValid
}