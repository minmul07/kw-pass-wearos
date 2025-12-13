package minmul.kwpass.ui.main

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.UserData
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val kwuRepository: KwuRepository,
    private val dataClient: DataClient,
    private val messageClient: MessageClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()


    private val _toastEvent = Channel<String>()
    val toastEvent = _toastEvent.receiveAsFlow()

    val isFirstRun: StateFlow<Boolean?> = userData.isFirstRun
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private var isAppReadyToRefresh = true


    init {
        startListeningForForcedAccountSync()

        viewModelScope.launch {
            userData.userFlow.collect { (ridOnDisk, passwordOnDisk, telOnDisk) ->
                setDataOnUiState(ridOnDisk, passwordOnDisk, telOnDisk)
                Log.d(
                    "DEBUG_USER",
                    "로드된 정보: 학번=$ridOnDisk, 비번= ${passwordOnDisk.length}자리, 전화=$telOnDisk"
                )
                if (isAppReadyToRefresh && isFirstRun.value == false) {
                    isAppReadyToRefresh = false
                    refreshQR()
                }
            }
        }
    }

    fun sendAccountDataToWatch(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            try {
                val request = PutDataMapRequest.create("/account").apply {
                    dataMap.putString("rid", rid)
                    dataMap.putString("password", password)
                    dataMap.putString("tel", tel)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()

                dataClient.putDataItem(request).await()

                Log.d("MainViewModel", "계정 정보 전송 성공. rid=$rid")
            } catch (e: Exception) {
                Log.e("MainViewModel", "계정 정보 전송 실패", e)
            }
        }
    }


    fun startListeningForForcedAccountSync() {
        viewModelScope.launch {
            callbackFlow {
                val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
                    if (messageEvent.path == "/refresh") {
                        trySend(Unit)
                    }
                }
                messageClient.addListener(listener)
                awaitClose { messageClient.removeListener(listener) }
            }.collect {
                sendAccountDataToWatch(
                    uiState.value.savedRid,
                    uiState.value.savedPassword,
                    uiState.value.savedTel
                )
            }
        }
    }

    fun generateQrBitmap(content: String) {
        if (content.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val bitmap: Bitmap? = withContext(Dispatchers.Default) {
                QrGenerator.generateQrBitmapInternal(content = content, margin = 2)
            }

            if (bitmap != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        qrBitmap = bitmap
                    )
                }
            }
        }
    }

    fun setAccountData() {
        if (!uiState.value.isAllValidInput) {
            _uiState.update { currentState ->
                currentState.copy(
                    fieldErrorStatus = true
                )
            }
            return
        }

        viewModelScope.launch {
            var result: String = ""
            _uiState.update { currentState ->
                currentState.copy(
                    fetchingData = true,
                    initialStatus = false,
                    failedForAccountVerification = false,
                    succeededForAccountVerification = false,
                    fieldErrorStatus = false
                )
            }
            try {
                result = fetchQR(
                    uiState.value.ridInput,
                    uiState.value.passwordInput,
                    uiState.value.telInput
                )
                if (result.isEmpty()) {
                    Log.d("fetchQR", "result = $result")
                    throw Exception("Void QR Response. ")
                }

                // 성공!
                saveDataOnLocal(
                    uiState.value.ridInput,
                    uiState.value.passwordInput,
                    uiState.value.telInput
                )
                sendAccountDataToWatch(
                    uiState.value.ridInput,
                    uiState.value.passwordInput,
                    uiState.value.telInput
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        fetchingData = false,
                        savedRid = uiState.value.ridInput,
                        savedTel = uiState.value.telInput,
                        savedPassword = uiState.value.passwordInput,
                        failedForAccountVerification = false,
                        succeededForAccountVerification = true
                    )
                }
            } catch (e: Exception) {
                _toastEvent.send("정보 확인 필요: ${e.message}")
                _uiState.update { currentState ->
                    currentState.copy(
                        fetchingData = false,
                        failedForAccountVerification = true,
                        succeededForAccountVerification = false,
                        fieldErrorStatus = true,
                    )
                }
            }
        }
    }

    fun refreshQR() {
        if (!uiState.value.isAllValidInput) {
            return
        }

        viewModelScope.launch {
            var result: String = ""
            _uiState.update { currentState ->
                currentState.copy(
                    fetchingData = true
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

                generateQrBitmap(content = result)

                _uiState.update { currentState ->
                    currentState.copy(
                        savedQR = result,
                        failedToGetQr = false
                    )
                }
            } catch (e: Exception) {
                _toastEvent.send(e.message ?: "알 수 없는 오류가 발생했습니다.")
                _uiState.update { currentState ->
                    currentState.copy(
                        failedToGetQr = true
                    )
                }
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(
                        fetchingData = false
                    )
                }
            }
        }
    }

    // qr 코드 반환
    private suspend fun fetchQR(rid: String, password: String, tel: String): String {
        Log.i("fetchQR", "INFO rid: $rid, password: ${password.length}자리, tel: $tel")
        val realRid = "0$rid"
        return kwuRepository.startProcess(rid = realRid, password = password, tel = tel)
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            userData.saveUserCredentials(rid, password, tel)
        }
    }

    fun updatePasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(
                passwordVisible = !_uiState.value.passwordVisible,
            )
        }
    }

    fun completeInitialSetup() {
        viewModelScope.launch {
            userData.finishedInitialSetupProcessedStatus()
        }
    }


    fun updateRidInput(input: String) {
        if (input.length <= 10 && input.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    ridInput = input,
                    isRidValid = isValidRid(input),
                    fieldErrorStatus = false
                )
            }
        }
    }

    fun updatePasswordInput(input: String) {
        Log.i("isPasswordValid", isValidPassword(input).toString())
        _uiState.update { currentState ->
            currentState.copy(
                passwordInput = input,
                isPasswordValid = isValidPassword(input),
                fieldErrorStatus = false
            )
        }
    }

    fun updateTelInput(input: String) {
        if (input.length <= 11 && input.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    telInput = input,
                    isTelValid = isValidTel(input),
                    fieldErrorStatus = false
                )
            }
        }
    }

    fun setDataOnUiState(newRid: String, newPassword: String, newTel: String) {
        _uiState.update { currentState ->
            currentState.copy(
                savedRid = newRid,
                savedPassword = newPassword,
                savedTel = newTel,
                savedQR = "",
                ridInput = newRid,
                passwordInput = newPassword,
                telInput = newTel,
                isRidValid = isValidRid(newRid), // true 보장됨
                isPasswordValid = isValidPassword(newPassword), // true 보장됨
                isTelValid = isValidTel(newTel),  // true 보장됨
                fetchingData = false
            )
        }
    }

    private fun isValidPassword(ps: String): Boolean {
        //첫문자를 영문자로 입력하세요.
        //영대문자(A~Z), 영소문자(a~z), 숫자(0~9) 및 특수문자(32개)
        //중 3종류 이상으로 입력하세요. 8자리 이상
        if (ps.length < 8) {
            return false
        }

        if (!ps[0].isLetter()) {
            return false
        }

        val specialChars = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

        return ps.all { char ->
            char.isLetterOrDigit() || specialChars.contains(char)
        }
    }

    private fun isValidRid(input: String): Boolean {
        return input.length == 10 && input.all { it.isDigit() }
    }

    private fun isValidTel(input: String): Boolean {
        return input.length == 11 && input.all { it.isDigit() }
    }
}