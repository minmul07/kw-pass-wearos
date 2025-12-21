package minmul.kwpass.main

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.WearableStatusCodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.QrGenerator
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

    // QR
    private var refreshJob: Job? = null

    init {
        startListeningForAccountSync()

        viewModelScope.launch(Dispatchers.Default) {
            try {
                QrGenerator.generateQrBitmapInternal("123")
            } catch (e: Exception) {

            }
        }

        viewModelScope.launch {
            combine(userData.userFlow, userData.isFirstRun) { user, firstRun ->
                Pair(user, firstRun)
            }.collect { (user, firstRun) ->
                val (rid, password, tel) = user

                Log.d(
                    "DEBUG_USER",
                    "로드된 정보: 학번=$rid, 비번= ${password.length}자리, 전화=$tel"
                )

                if (rid.isNotEmpty() && password.isNotEmpty() && tel.isNotEmpty()) { // 데이터 로드 성공
                    setUserDataOnUiState(rid, password, tel)
                    refreshQR()
                } else { // 최초 실행, 휴대폰에 계정 설정 완료되지 않음
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.START
                        )
                    }
                    requestForcedAccountDataSync(silent = true)
                    Log.d("DEBUG_USER", "최초 실행")
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
            setUserDataOnUiState(rid, password, tel)
        }
    }

    fun setUserDataOnUiState(rid: String, password: String, tel: String) {
        _uiState.update { currentState ->
            currentState.copy(
                savedRid = rid,
                savedPassword = password,
                savedTel = tel,
                accountDataLoaded = true
            )
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
                QrGenerator.generateQrBitmapInternal(content)
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


    fun requestForcedAccountDataSync(silent: Boolean) {
        viewModelScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                val phoneNode = nodes.firstOrNull()

                if (phoneNode != null) {
                    messageClient.sendMessage(phoneNode.id, "/refresh", null).await()
                    Log.d("requestRefresh", "새로고침 요청 전송 완료")
                } else if (!silent) {
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
                if (e is ApiException && e.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                    Log.d("requestRefresh", "phone과 연결되어 있지 않음")
                    if (!silent) {
                        playErrorVibration()
                        _uiState.update { currentState ->
                            currentState.copy(
                                status = ScreenStatus.NOT_CONNECTED_TO_PHONE
                            )
                        }
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.FAILED_TO_GET_ACCOUNT_DATA_FROM_PHONE
                        )
                    }
                    Log.e("requestRefresh", "데이터 로드 실패", e)
                }
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
                    status = ScreenStatus.START
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