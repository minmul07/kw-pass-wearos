package minmul.kwpass.shared.domain

import android.graphics.Bitmap
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.LocalDisk
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.analystics.KwPassLogger
import okio.IOException
import timber.log.Timber
import javax.inject.Inject

class GetQrCodeUseCase @Inject constructor(
    private val kwuRepository: KwuRepository,
    private val kwPassLogger: KwPassLogger,
    private val localDisk: LocalDisk
) {
    suspend operator fun invoke(
        rid: String,
        password: String,
        tel: String,
        source: String
    ): Result<Bitmap> =
        runCatching {
            val realRid = "0$rid"
            val cachedAuthKey = localDisk.getSavedAuthKey()

            val qrString = if (cachedAuthKey != null) {
                Timber.i("저장된 auth 키 발견!")
                runCatching { // 캐시된 auth키로 시도
                    fastGetQr(realRid, cachedAuthKey)
                }.recoverCatching { // 실패? 그럼 auth키 버리고 다시 시도
                    Timber.e("저장된 auth키 만료됨, 재시도")
                    getQrWithoutCachedAuthKey(realRid, password, tel)
                }.getOrThrow()

            } else { // 캐시된 auth키 없음
                Timber.i("저장된 auth 키 없음")
                getQrWithoutCachedAuthKey(realRid, password, tel)
            }


            if (qrString.isBlank()) throw KwPassException.ServerError()

            kwPassLogger.logQrGenerated(source)
            Timber.i("qr created on ${System.currentTimeMillis()}")

            // 비트맵 생성 로직
            val margin = if (source == "watch") 0 else 2
            QrGenerator.generateQrBitmapInternal(qrString, margin = margin)
                ?: throw KwPassException.UnknownError()
        }

    suspend fun getQrWithoutCachedAuthKey(
        rid: String, password: String, tel: String
    ): String {
        try {
            val secretKey = kwuRepository.getSecretKey(rid)

            if (secretKey.isNullOrBlank()) {
                Timber.e("No Secret Key")
                throw KwPassException.ServerError()
            }

            val authKey = kwuRepository.getAuthKey(rid, password, tel, secretKey)
            if (authKey.isNullOrBlank()) {
                Timber.e("No Auth Key")
                throw KwPassException.AccountError()
            }

            localDisk.saveAuthKey(authKey)


            val qrString = kwuRepository.getQrString(rid, authKey)
            if (qrString.isNullOrBlank()) {
                Timber.e("No QR")
                throw KwPassException.ServerError()
            }
            return qrString

        } catch (e: KwPassException) {
            throw e
        } catch (e: IOException) {
            Timber.e(e)
            throw KwPassException.NetworkError()
        } catch (e: Exception) {
            Timber.e(e, "알 수 없는 오류 발생")
            throw KwPassException.UnknownError()
        }
    }

    suspend fun fastGetQr(
        rid: String, authKey: String
    ): String {
        try {
            val qrString = kwuRepository.getQrString(rid, authKey)
            if (qrString.isNullOrBlank()) {
                Timber.e("No QR")
                throw KwPassException.ServerError()
            }
            return qrString
        } catch (e: KwPassException) {
            throw e
        } catch (e: IOException) {
            Timber.e(e)
            throw KwPassException.NetworkError()
        } catch (e: Exception) {
            Timber.e(e, "알 수 없는 오류 발생")
            throw KwPassException.UnknownError()
        }
    }
}