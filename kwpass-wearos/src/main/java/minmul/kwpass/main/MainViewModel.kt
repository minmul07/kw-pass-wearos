package minmul.kwpass.main

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.UserData
import minmul.kwpass.ui.ScreenStatus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val messageClient: MessageClient,
    private val dataClient: DataClient,
    private val nodeClient: NodeClient,
    private val kwuRepository: KwuRepository,
) : ViewModel() {
    // UISTATE
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // VIBRATION
    private val _isErrorVibrationActive = MutableStateFlow(false)
    val isErrorVibrationActive = _isErrorVibrationActive.asStateFlow()

    // TOAST
    private val _toastEvent = Channel<String>()
    val toastEvent = _toastEvent.receiveAsFlow()

    // QR
    private val qrWriter = MultiFormatWriter()

    private var refreshJob: Job? = null

    init {
        startListeningForAccountSync()

        viewModelScope.launch(Dispatchers.Default) {
            try {
                generateQrBitmapInternal("123")
            } catch (e: Exception) {

            }
        }

        viewModelScope.launch {
            userData.userFlow.distinctUntilChanged().collect { (rid, password, tel) ->
                if (rid.isNotEmpty() && password.isNotEmpty() && tel.isNotEmpty()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            savedRid = rid,
                            savedPassword = password,
                            savedTel = tel,
                            accountDataLoaded = true,
                        )
                    }
                    Log.d(
                        "DEBUG_USER",
                        "로드된 정보: 학번=$rid, 비번= ${password.length}자리, 전화=$tel"
                    )
                    refreshQR()
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.NO_ACCOUNT_DATA_ON_DISK
                        )
                    }
                    requestForcedAccountDataSync()
                    Log.d(
                        "DEBUG_USER",
                        "저장된 정보 없음"
                    )
                }
            }
        }
    }

    fun playErrorVibration() {
        _isErrorVibrationActive.value = true
    }

    fun stopErrorVibration() {
        _isErrorVibrationActive.value = false
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            userData.saveUserCredentials(rid, password, tel)
        }
    }


    fun startListeningForAccountSync() {
        viewModelScope.launch {
            callbackFlow {
                val listener = DataClient.OnDataChangedListener { dataEvents ->
                    dataEvents.forEach { event ->
                        if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/account") {
                            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                            val rid = dataMap.getString("rid") ?: ""
                            val password = dataMap.getString("password") ?: ""
                            val tel = dataMap.getString("tel") ?: ""

                            // 흐름(Flow)으로 데이터 방출
                            trySend(Triple(rid, password, tel))
                        }
                    }
                }

                dataClient.addListener(listener)
                awaitClose { dataClient.removeListener(listener) }
            }.collect { (newRid, newPassword, newTel) ->
                Log.d("MainViewModel", "startListeningForAccountSync()")
                saveDataOnLocal(newRid, newPassword, newTel)
            }
        }
    }

    fun generateQrBitmap(content: String) {
        if (content.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val bitmap: Bitmap? = withContext(Dispatchers.Default) {
                generateQrBitmapInternal(content)
            }

            if (bitmap != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        savedQrBitmap = bitmap,
                        status = ScreenStatus.QR_READY,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun generateQrBitmapInternal(content: String): Bitmap? {
        return try {
            val hint = mapOf(
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix: BitMatrix = qrWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                1, 1,
                hint
            )
            val width = bitMatrix.width
            val height = bitMatrix.height

            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            bmp.setPixels(pixels, 0, width, 0, 0, width, height)
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun requestForcedAccountDataSync() {
        viewModelScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                val phoneNode = nodes.firstOrNull()

                if (phoneNode != null) {
                    messageClient.sendMessage(phoneNode.id, "/refresh", null).await()
                    Log.d("requestRefresh", "새로고침 요청 전송 완료")
                } else {
                    playErrorVibration()
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.NOT_CONNECTED_TO_PHONE
                        )
                    }
                    Log.i("requestRefresh", "연결된 phone 없음")

                }

            } catch (e: Exception) {
                playErrorVibration()
                _uiState.update { currentState ->
                    currentState.copy(
                        status = ScreenStatus.FAILED_TO_GET_ACCOUNT_DATA_FROM_PHONE
                    )
                }
                Log.e("requestRefresh", "데이터 로드 실패", e)
            }

        }
    }

    private suspend fun fetchQR(rid: String, password: String, tel: String): String {
        Log.i("fetchQR", "INFO rid: $rid, password: ${password.length}자리, tel: $tel")
        val realRid = "0$rid"
        return kwuRepository.startProcess(rid = realRid, password = password, tel = tel)
    }

    fun refreshQR() {
        if (!uiState.value.accountDataLoaded) {
            _uiState.update { currentState ->
                currentState.copy(
                    status = ScreenStatus.NO_ACCOUNT_DATA_ON_DISK
                )
            }
        }

        Log.i("refreshQR()", "refreshQR()")
        if (!uiState.value.accountDataLoaded) {
            Log.w("refreshQR()", "데이터 로드 안됨")
            return
        }

        if (refreshJob?.isActive == true) {
            Log.i("refreshQR", "이전 작업 취소")
            refreshJob?.cancel()
        }

        refreshJob = viewModelScope.launch {
            var result: String = ""
            _uiState.update { currentState ->
                currentState.copy(
                    isRefreshing = true,
                    status = ScreenStatus.FETCHING_QR
                )
            }
            try {
                result = fetchQR(
                    uiState.value.savedRid,
                    uiState.value.savedPassword,
                    uiState.value.savedTel
                )
                if (result == "") {
                    throw Exception("Void QR Response. ")
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        savedQR = result,
                        status = ScreenStatus.GENERATING_QR
                    )
                }
                generateQrBitmap(result)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.i("refreshQR", "작업이 취소되었습니다.")
                    throw e
                } else {
                    playErrorVibration()
                    _toastEvent.send(e.message ?: "오류 발생")
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.FAILED_TO_GET_QR,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }


}