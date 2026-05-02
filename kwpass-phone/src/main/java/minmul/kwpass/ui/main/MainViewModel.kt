package minmul.kwpass.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import minmul.kwpass.R
import minmul.kwpass.domain.usecase.SyncWatchUseCase
import minmul.kwpass.domain.usecase.ValidateAccountUseCase
import minmul.kwpass.service.KwPassConst
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.LocalDisk
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.analytics.KwPassLogger
import minmul.kwpass.shared.domain.GetQrCodeUseCase
import minmul.kwpass.ui.UiText
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDisk: LocalDisk,
    private val getQrCodeUseCase: GetQrCodeUseCase,
    private val syncWatchUseCase: SyncWatchUseCase,
    private val validateAccountUseCase: ValidateAccountUseCase,
    private val kwPassLogger: KwPassLogger
) : ViewModel() {

    companion object {
        const val QR_VALID_TIME_SEC = 50
        const val QR_VALID_TIME_MS = QR_VALID_TIME_SEC * 1000L
        private const val PHONE_APP_SOURCE = "phone-app"
        private const val PHONE_WIDGET_SOURCE = "phone-widget"
    }

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()


    private val _toastEvent = Channel<UiText>(Channel.BUFFERED)
    val toastEvent = _toastEvent.receiveAsFlow()

    private val _snackbarEvent = Channel<UiText>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    val isFirstRun: StateFlow<Boolean?> = localDisk.isFirstRun
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private var isAppReadyToRefresh = true
    private var backActionCount = 0

    private var refreshJob: Job? = null

    private var timerJob: Job? = null


    init {
        startListeningForForcedAccountSync()

        viewModelScope.launch {
            combine(
                localDisk.userFlow,
                localDisk.isFirstRun,
                localDisk.qrSize
            ) { user, firstRun, qrSize ->
                Triple(user, firstRun, qrSize)
            }.collect { (user, firstRun, qrSize) ->
                val (rid, password, tel) = user

                Timber.tag("DEBUG_USER").d("로드된 정보: 학번=$rid, 비번= ${password.length}자리, 전화=$tel")
                setDataOnUiState(rid, password, tel)
                updateQrSize(qrSize.toFloat())

                if (isAppReadyToRefresh && !firstRun && validateAccountUseCase.isValidRid(rid)) {
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


    fun startListeningForForcedAccountSync() {
        viewModelScope.launch {
            syncWatchUseCase.setSyncListener()
                .collect {
                    val account = mainUiState.value.accountInfo
                    syncWatchUseCase.sendAccountData(
                        rid = account.rid,
                        password = account.password,
                        tel = account.tel
                    )
                }
        }
    }

    private fun resolvePasswordForSave(inputForm: InputFormState, accountInfo: AccountInfoState): String {
        return inputForm.passwordInput.ifBlank { accountInfo.password }
    }

    private fun hasAccountFormChanges(inputForm: InputFormState, accountInfo: AccountInfoState): Boolean {
        return inputForm.ridInput != accountInfo.rid ||
                inputForm.telInput != accountInfo.tel ||
                inputForm.passwordInput.isNotBlank()
    }

    private fun isAccountFormValidForSave(
        inputForm: InputFormState,
        accountInfo: AccountInfoState
    ): Boolean {
        val passwordForSave = resolvePasswordForSave(inputForm, accountInfo)
        return inputForm.isRidValid &&
                inputForm.isTelValid &&
                validateAccountUseCase.isValidPassword(passwordForSave)
    }

    fun setAccountData() {
        val currentUiState = mainUiState.value
        val inputForm = currentUiState.inputForm
        val accountInfo = currentUiState.accountInfo

        if (!hasAccountFormChanges(inputForm, accountInfo)) {
            return
        }

        if (!isAccountFormValidForSave(inputForm, accountInfo)) {
            _mainUiState.update { currentState ->
                currentState.copy(
                    accountSubmit = AccountSubmitState(
                        failed = true,
                        initialStatus = false
                    ),
                    inputForm = currentState.inputForm.copy(
                        fieldErrorStatus = true
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _mainUiState.update { currentState ->
                currentState.copy(
                    accountSubmit = AccountSubmitState(
                        isSubmitting = true,
                        initialStatus = false,
                    ),
                    inputForm = currentState.inputForm.copy(
                        fieldErrorStatus = false
                    )
                )
            }

            val newRid = inputForm.ridInput
            val newPassword = resolvePasswordForSave(inputForm, accountInfo)
            val newTel = inputForm.telInput

            getQrCodeUseCase(
                rid = newRid,
                password = newPassword,
                tel = newTel,
                source = PHONE_APP_SOURCE,
                useCachedAuthKey = false
            )
                .onSuccess { bitmap ->
                    saveDataOnLocal(newRid, newPassword, newTel)
                    syncWatchUseCase.sendAccountData(newRid, newPassword, newTel)
                    _mainUiState.update { currentState ->
                        currentState.copy(
                            accountDataLoaded = true,
                            process = currentState.process.copy(
                                isFetching = false,
                                fetchFailed = false,
                                fetchSucceeded = true,
                                initialStatus = false,
                                qrBitmap = bitmap,
                                refreshTimeLeft = QR_VALID_TIME_SEC,
                                qrCreationTime = System.currentTimeMillis()
                            ),
                            accountSubmit = AccountSubmitState(
                                succeeded = true,
                                initialStatus = false,
                            ),
                            accountInfo = currentState.accountInfo.copy(
                                rid = newRid,
                                password = newPassword,
                                tel = newTel
                            )
                        )
                    }
                    startRefreshTimer(time = QR_VALID_TIME_SEC)
                }
                .onFailure { e ->
                    if (e is KwPassException) {
                        val uiText = getErrorUiText(e)
                        _toastEvent.send(uiText)
                    } else {
                        _toastEvent.send(UiText.DynamicString(e.message ?: "Unknown Error"))
                    }
                    _mainUiState.update { currentState ->
                        currentState.copy(
                            accountSubmit = AccountSubmitState(
                                failed = true,
                                initialStatus = false,
                            ),
                            inputForm = currentState.inputForm.copy(
                                fieldErrorStatus = true
                            )
                        )
                    }
                }
        }
    }

    fun refreshQR(onWidget: Boolean = false) {
        if (!mainUiState.value.accountInfo.hasValidInfo) {
            Timber.e("isAllValidInput is false")
            return
        }

        val rid = mainUiState.value.accountInfo.rid
        val password = mainUiState.value.accountInfo.password
        val tel = mainUiState.value.accountInfo.tel
        val source = if (onWidget) PHONE_WIDGET_SOURCE else PHONE_APP_SOURCE

        timerJob?.cancel()
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _mainUiState.update { currentState ->
                currentState.copy(
                    process = currentState.process.copy(
                        isFetching = true,
                        initialStatus = false,
                        fetchFailed = false
                    )
                )
            }

            getQrCodeUseCase(rid, password, tel, source)
                .onSuccess { bitmap ->
                    _mainUiState.update { currentState ->
                        currentState.copy(
                            process = currentState.process.copy(
                                fetchFailed = false,
                                fetchSucceeded = true,
                                isFetching = false,
                                qrBitmap = bitmap,
                                refreshTimeLeft = QR_VALID_TIME_SEC,
                                qrCreationTime = System.currentTimeMillis()
                            )
                        )
                    }
                    startRefreshTimer(time = QR_VALID_TIME_SEC)
                }
                .onFailure { e ->
                    _mainUiState.update { currentState ->
                        currentState.copy(
                            process = currentState.process.copy(
                                fetchFailed = true,
                                fetchSucceeded = false,
                                isFetching = false
                            )
                        )
                    }

                    if (e is KwPassException) {
                        val uiText = getErrorUiText(e)
                        if (!onWidget) {
                            _snackbarEvent.send(uiText)
                        } else {
                            _toastEvent.send(uiText)
                        }
                    } else {
                        val message = UiText.DynamicString(e.message ?: "Unknown Error")
                        if (!onWidget) {
                            _snackbarEvent.send(message)
                        } else {
                            _toastEvent.send(message)
                        }
                    }
                    stopRefreshTimer()
                }
        }
    }

    private fun startRefreshTimer(time: Int) {
        timerJob?.cancel()

        _mainUiState.update { currentState ->
            currentState.copy(
                process = currentState.process.copy(
                    refreshTimeLeft = time
                )
            )
        }

        timerJob = viewModelScope.launch {
            while (isActive && mainUiState.value.process.refreshTimeLeft > 0) {
                delay(1000L)
                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            refreshTimeLeft = currentState.process.refreshTimeLeft - 1
                        )
                    )

                }

                if (mainUiState.value.process.refreshTimeLeft == 0) {
                    refreshQR()
                }
            }
        }
    }

    fun resumeRefreshTimer() {
        val qrCreationTime = mainUiState.value.process.qrCreationTime
        if (qrCreationTime == 0L) return

        val qrLifeElapsed = (System.currentTimeMillis() - qrCreationTime)
        Timber.tag("qrLifeElapsed").i("$qrLifeElapsed")

        // 유효기간 지났나?
        if (qrLifeElapsed >= QR_VALID_TIME_MS) {
            refreshQR()
        } else {
            val calculatedCurrentQrTimeLeft = QR_VALID_TIME_SEC - (qrLifeElapsed / 1000).toInt()
            Timber.tag("calculatedCurrentQrTimeLeft").i("$calculatedCurrentQrTimeLeft")
            _mainUiState.update { currentState ->
                currentState.copy(
                    process = currentState.process.copy(
                        refreshTimeLeft = calculatedCurrentQrTimeLeft
                    )
                )
            }
            startRefreshTimer(calculatedCurrentQrTimeLeft)
        }
    }

    fun stopRefreshTimer() {
        timerJob?.cancel()
    }

    fun saveQrSizeOnDisk() {
        viewModelScope.launch {
            localDisk.saveQrSize(mainUiState.value.process.qrSize)
        }
    }

    fun updateQrSize(size: Float) {
        Timber.i("qr size set to ${size}.dp")
        _mainUiState.update { currentState ->
            currentState.copy(
                process = currentState.process.copy(
                    qrSize = size.toInt()
                )
            )
        }
    }

    fun readySampleQrBitmap() {
        if (mainUiState.value.process.sampleQrBitmap == null) {
            viewModelScope.launch {
                _mainUiState.update { currentState ->
                    currentState.copy(
                        process = currentState.process.copy(
                            sampleQrBitmap = QrGenerator.generateQrBitmapInternal(
                                content = KwPassConst.SAMPLE_RESPONSE,
                                margin = 2
                            )
                        )
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRefreshTimer()
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            localDisk.saveUserCredentials(rid, password, tel)
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
            localDisk.finishedInitialSetupProcessedStatus()
        }
    }

    fun clearAccountSubmitState() {
        _mainUiState.update { currentState ->
            currentState.copy(
                accountSubmit = AccountSubmitState()
            )
        }
    }


    fun updateRidInput(input: String) {
        if (input.length <= 10 && input.all { it.isDigit() }) {
            _mainUiState.update { currentState ->
                currentState.copy(
                    inputForm = currentState.inputForm.copy(
                        ridInput = input,
                        isRidValid = validateAccountUseCase.isValidRid(input),
                        fieldErrorStatus = false
                    ),
                    accountSubmit = AccountSubmitState()
                )
            }
        }
    }

    fun updatePasswordInput(input: String) {
        Timber.tag("isPasswordValid").i(validateAccountUseCase.isValidPassword(input).toString())
        _mainUiState.update { currentState ->

            currentState.copy(
                inputForm = currentState.inputForm.copy(
                    passwordInput = input,
                    isPasswordValid = validateAccountUseCase.isValidPassword(input),
                    fieldErrorStatus = false
                ),
                accountSubmit = AccountSubmitState()
            )
        }
    }

    fun updateTelInput(input: String) {
        if (input.length <= 11 && input.all { it.isDigit() }) {
            _mainUiState.update { currentState ->
                currentState.copy(
                    inputForm = currentState.inputForm.copy(
                        telInput = input,
                        isTelValid = validateAccountUseCase.isValidTel(input),
                        fieldErrorStatus = false
                    ),
                    accountSubmit = AccountSubmitState()
                )
            }
        }
    }

    fun removeAuthKeyOnDisk() {
        viewModelScope.launch {
            localDisk.deleteSavedAuthKey()
            _toastEvent.send(UiText.DynamicString("auth 키 제거됨"))
            Timber.i("DEBUG: auth 키 제거됨")
        }
    }

    fun setDataOnUiState(newRid: String, newPassword: String, newTel: String) {
        _mainUiState.update { currentState ->
            currentState.copy(
                accountDataLoaded = true,
                accountInfo = currentState.accountInfo.copy(
                    rid = newRid,
                    password = newPassword,
                    tel = newTel
                ),
                inputForm = currentState.inputForm.copy(
                    ridInput = newRid,
                    passwordInput = "",
                    telInput = newTel,
                    isRidValid = validateAccountUseCase.isValidRid(newRid), // true 보장됨
                    isPasswordValid = false,
                    isTelValid = validateAccountUseCase.isValidTel(newTel),  // true 보장됨
                )
            )
        }
    }

    fun backAction(): Boolean {
        if (backActionCount == 1) {
            return true
        } else {
            viewModelScope.launch {
                _toastEvent.send(UiText.StringResource(R.string.how_to_exit))
            }
            backActionCount++
            return false
        }
    }
}
