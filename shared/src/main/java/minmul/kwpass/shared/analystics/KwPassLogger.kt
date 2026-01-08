package minmul.kwpass.shared.analystics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
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
}