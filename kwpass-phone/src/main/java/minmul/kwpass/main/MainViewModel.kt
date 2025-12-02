package minmul.kwpass.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import minmul.kwpass.service.KwuRepository
import minmul.kwpass.service.UserData
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userData: UserData,
    private val kwuRepository: KwuRepository
): ViewModel() {

    var ridField by mutableStateOf("")
    var ridFieldEnabled by mutableStateOf(true)
    var isRidValid by mutableStateOf(false)
    var passwordField by mutableStateOf("")
    var passwordFieldEnabled by mutableStateOf(true)
    var isPasswordValid by mutableStateOf(false)
    var telField by mutableStateOf("")
    var telFieldEnabled by mutableStateOf(true)
    var isTelValid by mutableStateOf(false)

    var passwordVisible by mutableStateOf(false)
    var validation by mutableStateOf(false)

    var rid by mutableStateOf("")
    var password by mutableStateOf("")
    var tel by mutableStateOf("")

    private val _toastEvent = Channel<String>()
    val toastEvent = _toastEvent.receiveAsFlow()

    var qr: String by mutableStateOf("")
    var fetchingData by mutableStateOf(false)


    init {
        viewModelScope.launch {
            userData.userFlow.collect { (savedRid, savedPass, savedTel) ->

                setData(savedRid, savedPass, savedTel)
                checkAllValidation()

                Log.d("DEBUG_USER", "로드된 정보: 학번=$savedRid, 비번= ${password.length}자리, 전화=$savedTel")

            }
        }
    }


    private suspend fun fetchQR(rid: String, password: String, tel: String): String {
        Log.i("fetchQR", "INFO rid: $rid, password: ${password.length}자리, tel: $tel")
        val realRid = "0$rid"
        return kwuRepository.startProcess(rid = realRid, password = password, tel = tel)
    }

    fun saveUserData() {
        if (!validation) {
            return
        }

        viewModelScope.launch {
            try {
                fetchingData = true
                val result = fetchQR(ridField, passwordField, telField)

                rid = ridField
                password = passwordField
                tel = telField

                saveDataOnLocal(rid, password, tel)
                qr = result
            } catch (e: Exception) {
                _toastEvent.send("정보 확인 필요: ${e.message}")
            } finally {
                fetchingData = false
            }

        }

    }

    fun refreshQR() {
        if (!validation) {
            return
        }

        viewModelScope.launch {
            try {
                fetchingData = true
                val result = fetchQR(rid, password, tel)
                qr = result
            } catch (e: Exception) {
                _toastEvent.send(e.message ?: "알 수 없는 오류가 발생했습니다.")
            } finally {
                fetchingData = false
            }
        }
    }

    fun saveDataOnLocal(rid: String, password: String, tel: String) {
        viewModelScope.launch {
            userData.saveUserCredentials(rid, password, tel)
        }
    }

    fun checkAllValidation() {
        isRidValid = ridField.length == 10
        isPasswordValid = validatePassword(passwordField)
        isTelValid = telField.length == 11
        validation = isTelValid && isPasswordValid && isRidValid
    }

    fun updateRid(new: String) {
        if (new.length <= 10 && new.all { it.isDigit() }) {
            ridField = new
        }
        isRidValid = ridField.length == 10
        checkAllValidation()
    }

    fun updatePassword(new: String) {
        passwordField = new
        isPasswordValid = validatePassword(new)
        Log.i("isPasswordValid", isPasswordValid.toString())
        checkAllValidation()
    }

    fun updateTel(new: String) {
        if (new.length <= 11 && new.all { it.isDigit() }) {
            telField = new
        }
        isTelValid = telField.length == 11
        checkAllValidation()
    }

    fun setData(newRid: String, newPassword: String, newTel: String) {
        ridField = newRid
        passwordField = newPassword
        telField = newTel

        rid = newRid
        password = newPassword
        tel = newTel
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