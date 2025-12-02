package minmul.kwpass.main

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

    // UI상태
    val isRidValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isTelValid: Boolean = false,
    val passwordVisible: Boolean = false,
    val fetchingData: Boolean = false,
) {
    val isAllValid: Boolean
        get() = isRidValid && isPasswordValid && isTelValid

}