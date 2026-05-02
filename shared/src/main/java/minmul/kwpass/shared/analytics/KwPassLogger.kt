package minmul.kwpass.shared.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import minmul.kwpass.shared.KwPassException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KwPassLogger @Inject constructor(
    private val analytics: FirebaseAnalytics
) {
    fun logQrGenerated(source: String) {
        val params = Bundle().apply {
            putString("source", source)
        }
        analytics.logEvent("qr_generated", params)
        Timber.d("logged: qr_generated - $source")
    }

    // qr 만료 시 인증 키 생성으로부터 지난 시간 기록
    fun logAuthKeyExpiredFallback(source: String, cachedElapsedMs: Long) {
        val params = Bundle().apply {
            putString("source", source)
            putLong("cached_elapsed_ms", cachedElapsedMs)
        }
        analytics.logEvent("auth_key_expired_fallback", params)
        Timber.d("logged: auth_key_expired_fallback - $source, cachedElapsedMs=$cachedElapsedMs")
    }

    // qr 요청 -> qr 생성까지의 시간
    fun logQrRequestToRender(
        source: String,
        authKeyReused: Boolean,
        durationMs: Long
    ) {
        val params = Bundle().apply {
            putString("source", source)
            putString("auth_key_reused", authKeyReused.toString())
            putLong("duration_ms", durationMs)
        }
        analytics.logEvent("qr_request_to_render", params)
        Timber.d(
            "logged: qr_request_to_render - $source, authKeyReused=$authKeyReused, durationMs=$durationMs"
        )
    }

    // qr 요청 실패
    fun logQrIssueFailed(source: String, cause: Throwable) {
        val reason = when (cause) {
            is KwPassException.NetworkError -> "network_error"
            is KwPassException.ServerError -> "server_error"
            is KwPassException.AccountError -> "account_error"
            is KwPassException.UnknownError -> "unknown_error"
            else -> "unexpected_error"
        }
        val params = Bundle().apply {
            putString("source", source)
            putString("failure_reason", reason)
            putString("exception_name", cause.javaClass.simpleName)
        }
        analytics.logEvent("qr_issue_failed", params)
        Timber.d(cause, "logged: qr_issue_failed - $source, reason=$reason")
    }
}
