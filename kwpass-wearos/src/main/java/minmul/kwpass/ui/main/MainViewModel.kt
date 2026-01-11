package minmul.kwpass.ui.main

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.UserData
import minmul.kwpass.shared.domain.GetQrCodeUseCase
import minmul.kwpass.ui.ScreenStatus
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val messageClient: MessageClient,
    private val dataClient: DataClient,
    private val nodeClient: NodeClient,
    private val getQrCodeUseCase: GetQrCodeUseCase,
) : ViewModel() {
    private val source: String = "watch"

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
            }
                .distinctUntilChanged()
                .collect { (user, firstRun) ->
                    val (rid, password, tel) = user

                    Timber.d("로드된 정보: 학번=$rid, 비번= ${password.length}자리, 전화=$tel")

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
                        Timber.d("최초 실행")
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
                Timber.d("startListeningForAccountSync()")
                saveDataOnLocal(newRid, newPassword, newTel)
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
                    Timber.tag("requestRefresh").d("새로고침 요청 전송 완료")
                    delay(1000L)
                } else if (!silent) {
                    playErrorVibration()
                    _uiState.update { currentState ->
                        currentState.copy(
                            status = ScreenStatus.NOT_CONNECTED_TO_PHONE
                        )
                    }
                    Timber.tag("requestRefresh").i("연결된 phone 없음")

                }

            } catch (e: Exception) {
                playErrorVibration()
                if (e is ApiException && e.statusCode == WearableStatusCodes.TARGET_NODE_NOT_CONNECTED) {
                    Timber.tag("requestRefresh").d("phone과 연결되어 있지 않음")
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
                    Timber.tag("requestRefresh").e(e, "데이터 로드 실패")
                }
            }

        }
    }

    fun refreshQR() {
        if (!uiState.value.accountDataLoaded) {
            _uiState.update { currentState ->
                currentState.copy(
                    status = ScreenStatus.START
                )
            }
        }

        Timber.i("refreshQR()")
        if (!uiState.value.accountDataLoaded) {
            Timber.w("데이터 로드 안됨")
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isRefreshing = true,
                    status = ScreenStatus.FETCHING_QR
                )
            }

            val rid = uiState.value.savedRid
            val password = uiState.value.savedPassword
            val tel = uiState.value.savedTel

            getQrCodeUseCase(rid, password, tel, source)
                .onSuccess { bitmap ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            savedQrBitmap = bitmap,
                            status = ScreenStatus.QR_READY,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure {
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