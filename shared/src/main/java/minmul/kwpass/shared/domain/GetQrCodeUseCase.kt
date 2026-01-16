package minmul.kwpass.shared.domain

import android.graphics.Bitmap
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.analystics.KwPassLogger
import timber.log.Timber
import javax.inject.Inject

class GetQrCodeUseCase @Inject constructor(
    private val kwuRepository: KwuRepository,
    private val kwPassLogger: KwPassLogger
) {
    suspend operator fun invoke(
        rid: String,
        password: String,
        tel: String,
        source: String
    ): Result<Bitmap> =
        runCatching {
            val realRid = "0$rid"
            val qrString = kwuRepository.startProcess(realRid, password, tel)
            Timber.i("qrString = $qrString")

            if (qrString.isBlank()) throw KwPassException.ServerError()

            kwPassLogger.logQrGenerated(source)
            // 비트맵 생성 로직

            val margin = if (source == "watch") 0 else 2
            QrGenerator.generateQrBitmapInternal(qrString, margin = margin)
                ?: throw KwPassException.UnknownError()
        }
}