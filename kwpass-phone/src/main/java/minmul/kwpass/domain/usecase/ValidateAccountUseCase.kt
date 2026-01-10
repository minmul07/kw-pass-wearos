package minmul.kwpass.domain.usecase

import javax.inject.Inject

class ValidateAccountUseCase @Inject constructor() {
    fun isValidPassword(ps: String): Boolean {
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

    fun isValidRid(input: String): Boolean {
        return input.length == 10 && input.all { it.isDigit() }
    }

    fun isValidTel(input: String): Boolean {
        return input.length == 11 && input.all { it.isDigit() }
    }
}