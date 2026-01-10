package minmul.kwpass.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import minmul.kwpass.R
import minmul.kwpass.domain.usecase.SyncWatchUseCase
import minmul.kwpass.domain.usecase.ValidateAccountUseCase
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.UserData
import minmul.kwpass.shared.analystics.KwPassLogger
import minmul.kwpass.shared.domain.GetQrCodeUseCase
import minmul.kwpass.ui.UiText
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val getQrCodeUseCase: GetQrCodeUseCase,
    private val syncWatchUseCase: SyncWatchUseCase,
    private val validateAccountUseCase: ValidateAccountUseCase,
    private val kwPassLogger: KwPassLogger
) : ViewModel() {
    private val source: String = "phone"

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

            getQrCodeUseCase(newRid, newPassword, newTel, source)
                .onSuccess {
                    saveDataOnLocal(newRid, newPassword, newTel)
                    syncWatchUseCase.sendAccountData(newRid, newPassword, newTel)
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

    fun refreshQR() {
        if (!mainUiState.value.inputForm.isAllValidInput) {
            return
        }

        val rid = mainUiState.value.accountInfo.rid
        val password = mainUiState.value.accountInfo.password
        val tel = mainUiState.value.accountInfo.tel

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _mainUiState.update { currentState ->
                currentState.copy(
                    process = currentState.process.copy(
                        isFetching = true,
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
                                qrBitmap = bitmap
                            )
                        )
                    }
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
                        _snackbarEvent.send(uiText)
                    } else {
                        _snackbarEvent.send(UiText.DynamicString(e.message ?: "Unknown Error"))
                    }
                }
        }
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
                        isRidValid = validateAccountUseCase.isValidRid(input),
                        fieldErrorStatus = false
                    )
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
                        isTelValid = validateAccountUseCase.isValidTel(input),
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
                    isRidValid = validateAccountUseCase.isValidRid(newRid), // true 보장됨
                    isPasswordValid = validateAccountUseCase.isValidPassword(newPassword), // true 보장됨
                    isTelValid = validateAccountUseCase.isValidTel(newTel),  // true 보장됨
                ),
                process = currentState.process.copy(
                    isFetching = false
                )
            )
        }
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