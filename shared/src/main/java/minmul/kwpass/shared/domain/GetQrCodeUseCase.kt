package minmul.kwpass.shared.domain

import android.graphics.Bitmap
import minmul.kwpass.shared.BuildConfig
import minmul.kwpass.shared.KwPassException
import minmul.kwpass.shared.KwuRepository
import minmul.kwpass.shared.LocalDisk

import minmul.kwpass.shared.NetworkProfilingListener
import minmul.kwpass.shared.QrGenerator
import minmul.kwpass.shared.analystics.KwPassLogger
import okio.IOException
import timber.log.Timber
import javax.inject.Inject

class GetQrCodeUseCase @Inject constructor(
    private val kwuRepository: KwuRepository,
    private val kwPassLogger: KwPassLogger,
    private val localDisk: LocalDisk,
) {
    suspend operator fun invoke(
        rid: String,
        password: String,
        tel: String,
        source: String,
        profileSessionId: String? = null,
    ): Result<Bitmap> {
        return runCatching {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "usecase.getQr.start",
                    "source=$source",
                )
            }

            val realRid = "0$rid"
            val cachedAuthKey = localDisk.getSavedAuthKey()

            val qrString = if (cachedAuthKey != null) {
                if (BuildConfig.DEBUG) {
                    NetworkProfilingListener.logStage(
                        profileSessionId,
                        "usecase.cachedAuthKey.hit",
                        "authKeyLength=${cachedAuthKey.length}",
                    )
                }
                Timber.i("cached auth key found")

                runCatching {
                    fastGetQr(realRid, cachedAuthKey, profileSessionId)
                }.recoverCatching {
                    if (BuildConfig.DEBUG) {
                        NetworkProfilingListener.logStage(
                            profileSessionId,
                            "usecase.cachedAuthKey.fallback",
                            "reason=${it.javaClass.simpleName}:${it.message}",
                        )
                    }
                    Timber.e("cached auth key failed, retrying full auth flow")
                    getQrWithoutCachedAuthKey(realRid, password, tel, profileSessionId)
                }.getOrThrow()
            } else {
                if (BuildConfig.DEBUG) {
                    NetworkProfilingListener.logStage(
                        profileSessionId,
                        "usecase.cachedAuthKey.miss",
                    )
                }
                Timber.i("cached auth key missing")
                getQrWithoutCachedAuthKey(realRid, password, tel, profileSessionId)
            }

            if (qrString.isBlank()) throw KwPassException.ServerError()

            kwPassLogger.logQrGenerated(source)
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "usecase.qrString.ready",
                    "qrLength=${qrString.length}",
                )
            }

            val margin = if (source == "watch") 0 else 2
            val bitmap = QrGenerator.generateQrBitmapInternal(qrString, margin = margin)
                ?: throw KwPassException.UnknownError()

            NetworkProfilingListener.finishSession(
                profileSessionId,
                "usecase.qrBitmap.generated",
                "margin=$margin",
            )

            bitmap
        }.onFailure { throwable ->
            NetworkProfilingListener.finishSession(
                profileSessionId,
                "usecase.getQr.failed",
                "error=${throwable.javaClass.simpleName}:${throwable.message}",
            )
        }
    }

    suspend fun getQrWithoutCachedAuthKey(
        rid: String,
        password: String,
        tel: String,
        profileSessionId: String? = null,
    ): String {
        try {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "usecase.fullAuthFlow.start",
                )
            }

            val secretKey = kwuRepository.getSecretKey(
                rid = rid,
                profileSessionId = profileSessionId,
            )
            if (secretKey.isNullOrBlank()) {
                Timber.e("No Secret Key")
                throw KwPassException.ServerError()
            }

            val authKey = kwuRepository.getAuthKey(
                rid = rid,
                password = password,
                tel = tel,
                secretKey = secretKey,
                profileSessionId = profileSessionId,
            )
            if (authKey.isNullOrBlank()) {
                Timber.e("No Auth Key")
                throw KwPassException.AccountError()
            }

            localDisk.saveAuthKey(authKey)
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "usecase.authKey.cached",
                    "authKeyLength=${authKey.length}",
                )
            }

            val qrString = kwuRepository.getQrString(
                rid = rid,
                authKey = authKey,
                profileSessionId = profileSessionId,
            )
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
            Timber.e(e, "unexpected error while fetching qr")
            throw KwPassException.UnknownError()
        }
    }

    suspend fun fastGetQr(
        rid: String,
        authKey: String,
        profileSessionId: String? = null,
    ): String {
        try {
            if (BuildConfig.DEBUG) {
                NetworkProfilingListener.logStage(
                    profileSessionId,
                    "usecase.fastQrFlow.start",
                    "authKeyLength=${authKey.length}",
                )
            }

            val qrString = kwuRepository.getQrString(
                rid = rid,
                authKey = authKey,
                profileSessionId = profileSessionId,
            )
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
            Timber.e(e, "unexpected error while using cached auth key")
            throw KwPassException.UnknownError()
        }
    }
}
