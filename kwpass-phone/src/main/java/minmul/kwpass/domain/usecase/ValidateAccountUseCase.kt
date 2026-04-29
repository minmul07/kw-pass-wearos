package minmul.kwpass.domain.usecase

import javax.inject.Inject

class ValidateAccountUseCase @Inject constructor() {
    fun isValidPassword(ps: String): Boolean {
        return ps.length >= 8 && ps.none { it.isWhitespace() }
    }

    fun isValidRid(input: String): Boolean {
        return input.length == 10 && input.all { it.isDigit() }
    }

    fun isValidTel(input: String): Boolean {
        return input.length == 11 && input.all { it.isDigit() }
    }
}
