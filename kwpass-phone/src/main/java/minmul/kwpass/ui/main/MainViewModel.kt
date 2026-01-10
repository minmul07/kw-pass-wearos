package minmul.kwpass.ui.main

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import minmul.kwpass.R
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.UserData
import minmul.kwpass.shared.analystics.KwPassLogger
import minmul.kwpass.ui.UiText
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val kwuRepository: KwuRepository,
    private val dataClient: DataClient,
    private val messageClient: MessageClient,
    private val kwPassLogger: KwPassLogger
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()


    private val _toastEvent = Channel<UiText>(Channel.BUFFERED)
    val toastEvent = _toastEvent.receiveAsFlow()

    private val _snackbarEvent = Channel<UiText>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    val isFirstRun: StateFlow<Boolean?> = userData.isFirstRun
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private var isAppReadyToRefresh = true
    private var backActionCount = 0

    private var refreshJob: Job? = null


    init {
        startListeningForForcedAccountSync()

        viewModelScope.launch {
            combine(userData.userFlow, userData.isFirstRun) { user, firstRun ->
                Pair(user, firstRun)
            }.collect { (user, firstRun) ->
                val (rid, password, tel) = user

                Timber.tag("DEBUG_USER").d("로드된 정보: 학번=$rid, 비번= ${password.length}자리, 전화=$tel")
                setDataOnUiState(rid, password, tel)

                if (isAppReadyToRefresh && !firstRun && isValidRid(rid)) {
                    isAppReadyToRefresh = false
                    refreshQR()
                }
            }

        }
    }

    private fun getErrorUiText(e: KwPassException): UiText {
        return when (e) {
            is KwPassException.NetworkError -> UiText.StringResource(R.string.error_network)
            is KwPassException.ServerError -> UiText.StringResource(R.string.error_server)
            is KwPassException.AccountError -> UiText.StringResource(R.string.error_account)
            is KwPassException.UnknownError -> UiText.StringResource(R.string.error_unknown)
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

                Timber.d("계정 정보 전송 성공. rid=$rid")
            } catch (e: Exception) {
                Timber.tag("계정 정보 전송 실패").e(e)
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
                    mainUiState.value.accountInfo.rid,
                    mainUiState.value.accountInfo.password,
                    mainUiState.value.accountInfo.tel
                )
            }
        }
    }

    private suspend fun generateQrBitmap(content: String, scaled: Boolean): Bitmap? {
        if (content.isEmpty()) {
            return null
        }

        kwPassLogger.logQrGenerated("phone")
        return withContext(Dispatchers.Default) {
            QrGenerator.generateQrBitmapInternal(
                content = content,
                margin = 2,
                size = if (scaled) 400 else 1
            )
        }
    }

    fun setAccountData() {
        if (!mainUiState.value.inputForm.isAllValidInput) {
            _mainUiState.update { currentState ->
                currentState.copy(
                    inputForm = currentState.inputForm.copy(
                        fieldErrorStatus = true
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            var result: String
            _mainUiState.update { currentState ->
                currentState.copy(
                    process = currentState.process.copy(
                        isFetching = true,
                        initialStatus = false,
                        fetchFailed = false,
                        fetchSucceeded = false,
                    ),
                    inputForm = currentState.inputForm.copy(
                        fieldErrorStatus = false
                    )
                )
            }

            val newRid = mainUiState.value.inputForm.ridInput
            val newPassword = mainUiState.value.inputForm.passwordInput
            val newTel = mainUiState.value.inputForm.telInput

            try {
                result = fetchQR(newRid, newPassword, newTel)
                if (result.isEmpty()) {
                    Timber.tag("fetchQR").d("result = $result")
                    throw Exception("Void QR Response. ")
                }

                // 성공!
                saveDataOnLocal(newRid, newPassword, newTel)
                sendAccountDataToWatch(newRid, newPassword, newTel)
                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            isFetching = false,
                            fetchFailed = false,
                            fetchSucceeded = true
                        ),
                        accountInfo = currentState.accountInfo.copy(
                            rid = newRid,
                            password = newPassword,
                            tel = newTel
                        )
                    )
                }
            } catch (e: KwPassException) {
                val uiText = getErrorUiText(e)
                _toastEvent.send(uiText)

                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            isFetching = false,
                            fetchFailed = true,
                            fetchSucceeded = false
                        ),
                        inputForm = currentState.inputForm.copy(
                            fieldErrorStatus = true
                        )
                    )
                }
            } catch (e: Exception) {
                _snackbarEvent.send(UiText.DynamicString(e.message ?: "Unknown Error"))
                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            isFetching = false,
                            fetchFailed = true,
                            fetchSucceeded = false
                        ),
                        inputForm = currentState.inputForm.copy(
                            fieldErrorStatus = true
                        )
                    )
                }
            }
        }
    }

    fun refreshQR(scaled: Boolean = false) {
        if (!mainUiState.value.inputForm.isAllValidInput) {
            return
        }

        val rid = mainUiState.value.accountInfo.rid
        val password = mainUiState.value.accountInfo.password
        val tel = mainUiState.value.accountInfo.tel


        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            var result: String
            _mainUiState.update { currentState ->
                currentState.copy(
                    process = currentState.process.copy(
                        isFetching = true
                    )
                )
            }
            try {
                result = fetchQR(rid, password, tel)
                if (result == "") {
                    throw Exception("Void QR Response. ")
                }

                val qrBitmap = generateQrBitmap(content = result, scaled = scaled)

                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            fetchFailed = false,
                            qrBitmap = qrBitmap,
                            qrString = result
                        )
                    )
                }
            } catch (e: KwPassException) {
                val uiText = getErrorUiText(e)
                _snackbarEvent.send(uiText)

                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            isFetching = true
                        )
                    )
                }
            } catch (e: Exception) {
                _snackbarEvent.send(UiText.DynamicString(e.message ?: "Unknown Error"))
            } finally {
                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            isFetching = false
                        )
                    )
                }
            }
        }
    }

    // qr 코드 반환
    private suspend fun fetchQR(rid: String, password: String, tel: String): String {
        Timber.tag("fetchQR").i("INFO rid: $rid, password: ${password.length}자리, tel: $tel")
        val realRid = "0$rid"
        return kwuRepository.startProcess(rid = realRid, password = password, tel = tel)
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            userData.saveUserCredentials(rid, password, tel)
        }
    }

    fun updatePasswordVisibility() {
        _mainUiState.update { currentState ->
            currentState.copy(
                inputForm = currentState.inputForm.copy(
                    passwordVisible = !currentState.inputForm.passwordVisible
                )
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
            _mainUiState.update { currentState ->
                currentState.copy(
                    inputForm = currentState.inputForm.copy(
                        ridInput = input,
                        isRidValid = isValidRid(input),
                        fieldErrorStatus = false
                    )
                )
            }
        }
    }

    fun updatePasswordInput(input: String) {
        Timber.tag("isPasswordValid").i(isValidPassword(input).toString())
        _mainUiState.update { currentState ->

            currentState.copy(
                inputForm = currentState.inputForm.copy(
                    passwordInput = input,
                    isPasswordValid = isValidPassword(input),
                    fieldErrorStatus = false
                )
            )
        }
    }

    fun updateTelInput(input: String) {
        if (input.length <= 11 && input.all { it.isDigit() }) {
            _mainUiState.update { currentState ->
                currentState.copy(
                    inputForm = currentState.inputForm.copy(
                        telInput = input,
                        isTelValid = isValidTel(input),
                        fieldErrorStatus = false
                    )
                )
            }
        }
    }

    fun setDataOnUiState(newRid: String, newPassword: String, newTel: String) {
        _mainUiState.update { currentState ->
            currentState.copy(
                accountInfo = currentState.accountInfo.copy(
                    rid = newRid,
                    password = newPassword,
                    tel = newTel
                ),
                inputForm = currentState.inputForm.copy(
                    ridInput = newRid,
                    passwordInput = "",
                    telInput = newTel,
                    isRidValid = isValidRid(newRid), // true 보장됨
                    isPasswordValid = isValidPassword(newPassword), // true 보장됨
                    isTelValid = isValidTel(newTel),  // true 보장됨
                ),
                process = currentState.process.copy(
                    isFetching = false
                )
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

    fun backAction(): Boolean {
        if (backActionCount == 1) {
            return true
        } else {
            backActionCount++
            return false
        }
    }
}