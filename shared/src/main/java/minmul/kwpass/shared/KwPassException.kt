package minmul.kwpass.shared

sealed class KwPassException : Exception() {
    class NetworkError : KwPassException()

    class ServerError : KwPassException()

    class AccountError : KwPassException()

    class UnknownError : KwPassException()
}