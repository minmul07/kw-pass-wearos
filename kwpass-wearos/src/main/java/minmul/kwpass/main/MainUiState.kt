package minmul.kwpass.main

import android.graphics.Bitmap
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import minmul.kwpass.ui.ScreenStatus

data class MainUiState(
    // 저장된 사용자 정보
    val savedRid: String = "",
    val savedPassword: String = "",
    val savedTel: String = "",
    val savedQR: String = "",
    val savedQrBitmap: Bitmap? = null,
    val savedQrTime: Long = 0L,
    val status: ScreenStatus = ScreenStatus.START,

    val accountDataLoaded: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val allDataReady: Boolean =
        savedRid.isNotEmpty() && savedPassword.isNotEmpty() && savedTel.isNotEmpty()
}

class MainUiStateProvider : PreviewParameterProvider<MainUiState> {
    override val values: Sequence<MainUiState> = ScreenStatus.entries.asSequence().map { status ->
        // 각 status를 가진 MainUiState 객체 생성
        MainUiState(
            status = status,
            // 필요하다면 QR이 보이는 상태를 위해 더미 비트맵 등을 추가 설정 가능
            savedQrBitmap = null
        )
    }
}