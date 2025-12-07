package minmul.kwpass.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import minmul.kwpass.shared.KwuRepository
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

    init {
        startListeningForForcedAccountSync()

        viewModelScope.launch {
            userData.userFlow.collect { (ridOnDisk, passwordOnDisk, telOnDisk) ->

                setData(ridOnDisk, passwordOnDisk, telOnDisk)

                Log.d(
                    "DEBUG_USER",
                    "로드된 정보: 학번=$ridOnDisk, 비번= ${passwordOnDisk.length}자리, 전화=$telOnDisk"
                )

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

    fun sendQrToWatch(qrData: String) {
        viewModelScope.launch {
            try {
                PutDataMapRequest.create("/qr_code").apply {
                    dataMap.putString("qr_data", qrData)
                    dataMap.putLong("timeStamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()
                Log.d("MainViewModel", "QR 전송 성공: $qrData")
            } catch (e: Exception) {
                Log.e("MainViewModel()", "QR 전송 실패", e)
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

//    fun startListeningForRefresh() {
//        viewModelScope.launch {
//            callbackFlow {
//                val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
//                    if (messageEvent.path == "/refresh_qr") {
//                        trySend(Unit)
//                    }
//                }
//                messageClient.addListener(listener)
//                awaitClose { messageClient.removeListener(listener) }
//            }.collect {
//                refreshQR()
//            }
//        }
//    }


    private suspend fun fetchQR(rid: String, password: String, tel: String): String {
        Log.i("fetchQR", "INFO rid: $rid, password: ${password.length}자리, tel: $tel")
        val realRid = "0$rid"
        return kwuRepository.startProcess(rid = realRid, password = password, tel = tel)
    }

    fun saveUserData() {
        if (!uiState.value.isAllValid) {
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
                saveDataOnLocal(
                    uiState.value.savedRid,
                    uiState.value.savedPassword,
                    uiState.value.savedTel
                )
                sendAccountDataToWatch(
                    uiState.value.savedRid,
                    uiState.value.savedPassword,
                    uiState.value.savedTel
                )
            } catch (e: Exception) {
                _toastEvent.send("정보 확인 필요: ${e.message}")
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(
                        fetchingData = false
                    )
                }
            }
        }
    }

    fun refreshQR() {
        if (!uiState.value.isAllValid) {
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
                sendQrToWatch(result)
            } catch (e: Exception) {
                _toastEvent.send(e.message ?: "알 수 없는 오류가 발생했습니다.")
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(
                        savedQR = result,
                        fetchingData = false
                    )
                }
            }
        }
    }

    fun updatePasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(
                passwordVisible = !_uiState.value.passwordVisible,
            )
        }
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            userData.saveUserCredentials(rid, password, tel)
        }
    }

    fun updateRidInput(input: String) {
        val isValid: Boolean = input.length == 10 && input.all { it.isDigit() }
        if (input.length <= 10 && input.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    ridInput = input,
                    isRidValid = isValid
                )
            }
        }
    }

    fun updatePasswordInput(input: String) {
        val isValid: Boolean = validatePassword(input)
        Log.i("isPasswordValid", isValid.toString())
        _uiState.update { currentState ->
            currentState.copy(
                passwordInput = input,
                isPasswordValid = isValid
            )
        }
    }

    fun updateTelInput(input: String) {
        val isValid: Boolean = input.length == 11 && input.all { it.isDigit() }
        if (input.length <= 11 && input.all { it.isDigit() }) {
            _uiState.update { currentState ->
                currentState.copy(
                    telInput = input,
                    isTelValid = isValid
                )
            }
        }
    }

    fun setData(newRid: String, newPassword: String, newTel: String) {
        _uiState.update { currentState ->
            currentState.copy(
                savedRid = newRid,
                savedPassword = newPassword,
                savedTel = newTel,
                savedQR = "",
                ridInput = newRid,
                passwordInput = newPassword,
                telInput = newTel,
                isRidValid = true, // true 보장됨
                isPasswordValid = true, // true 보장됨
                isTelValid = true,  // true 보장됨
                passwordVisible = false,
                fetchingData = false
            )
        }
    }

    fun validatePassword(ps: String): Boolean {
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
}